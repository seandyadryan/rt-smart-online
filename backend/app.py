import os, re, hashlib, hmac, secrets, base64
import binascii, warnings
import logging
from io import BytesIO
from PIL import Image, ImageOps, UnidentifiedImageError
from datetime import datetime, timezone, timedelta, date
from uuid import UUID, uuid4
from contextlib import asynccontextmanager
from pathlib import Path
import httpx
import psycopg
from psycopg.rows import dict_row
from psycopg.errors import UniqueViolation
from fastapi import FastAPI, Depends, HTTPException, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse, Response
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, Field, field_validator

def db():
    return psycopg.connect(os.environ['DATABASE_URL'], row_factory=dict_row)

def digest(value):
    return hashlib.sha256(value.encode()).hexdigest()

def pin_hash(pin):
    salt = secrets.token_bytes(16)
    return base64.b64encode(salt + hashlib.scrypt(pin.encode(), salt=salt, n=16384, r=8, p=1)).decode()

def check_pin(pin, encoded):
    if not encoded:
        return False
    raw = base64.b64decode(encoded)
    return hmac.compare_digest(raw[16:], hashlib.scrypt(pin.encode(), salt=raw[:16], n=16384, r=8, p=1))

def normalize_phone(value):
    value = re.sub(r'[\s()-]', '', value)
    if value.startswith('0'): value = '+62' + value[1:]
    elif value.startswith('62'): value = '+' + value
    if not re.fullmatch(r'\+628\d{8,11}', value):
        raise ValueError('Gunakan nomor HP Indonesia yang valid, contoh 08xxxxxxxxxx.')
    return value

@asynccontextmanager
async def lifespan(app):
    with db() as c: c.execute(Path(__file__).with_name('schema.sql').read_text())
    yield

app = FastAPI(title='RT Smart PRO', version='1.0.3', lifespan=lifespan, docs_url=None, redoc_url=None, openapi_url=None)
bearer = HTTPBearer(auto_error=False)
MAX_PHOTO_BYTES = 2 * 1024 * 1024
MAX_PHOTO_BASE64 = 4 * ((MAX_PHOTO_BYTES + 2) // 3)
Image.MAX_IMAGE_PIXELS = 20_000_000


def decode_photo(value):
    if not value:
        return None
    try:
        raw = base64.b64decode(value, validate=True)
        if len(raw) > MAX_PHOTO_BYTES:
            raise ValueError('Photo too large')
        with warnings.catch_warnings():
            warnings.simplefilter('error', Image.DecompressionBombWarning)
            with Image.open(BytesIO(raw)) as source:
                if source.format not in ('JPEG', 'PNG', 'WEBP'):
                    raise ValueError('Unsupported image')
                source.load()
                image = ImageOps.exif_transpose(source)
                image.thumbnail((1600, 1600))
                # Re-encode pixels only: filenames, EXIF/GPS and other source metadata are discarded.
                pixels = Image.new('RGB', image.size, 'white')
                if 'A' in image.getbands():
                    rgba = image.convert('RGBA')
                    pixels.paste(rgba, mask=rgba.getchannel('A'))
                else:
                    pixels.paste(image.convert('RGB'))
                output = BytesIO()
                pixels.save(output, format='JPEG', quality=85)
                if output.tell() > MAX_PHOTO_BYTES:
                    raise ValueError('Photo too large')
                return output.getvalue()
    except (ValueError, binascii.Error, OSError, UnidentifiedImageError,
            Image.DecompressionBombWarning, Image.DecompressionBombError):
        raise HTTPException(400, 'Foto tidak valid. Gunakan JPEG, PNG, atau WebP maksimal 2 MB.')


def attach_photo(c, user, data, *, news_id=None, report_id=None):
    if data is not None:
        c.execute('INSERT INTO photos(id,community_id,news_id,report_id,data) VALUES(%s,%s,%s,%s,%s)',
                  (uuid4(), user['community_id'], news_id, report_id, data))


@app.exception_handler(RequestValidationError)
async def invalid_request(request, exc):
    labels = {'title':'Judul', 'body':'Isi', 'category':'Kategori', 'name':'Nama', 'address':'Alamat',
              'phone':'Nomor HP', 'code':'PIN atau kode OTP', 'current_pin':'PIN saat ini',
              'new_pin':'PIN baru', 'period':'Periode', 'amount':'Nominal', 'due_date':'Jatuh tempo',
              'date':'Tanggal', 'reference':'Referensi transfer', 'bank':'Nama bank',
              'account':'Nomor rekening', 'holder':'Nama pemilik rekening', 'photo_base64':'Foto'}
    labels['body'] = 'Isi kabar RT' if request.url.path == '/api/v1/news' else 'Isi laporan' if request.url.path == '/api/v1/reports' else 'Isi'
    messages, errors = [], []
    for error in exc.errors():
        location = error.get('loc') or ('data',)
        field = str(location[-1])
        code = error.get('type', 'invalid')
        context = error.get('ctx') or {}
        label = labels.get(field, 'Data')
        if field == 'photo_base64':
            message = 'Foto terlalu besar. Ukuran foto yang dikirim maksimal 2 MB.' if code == 'string_too_long' else 'Foto tidak valid. Pilih ulang foto Anda.'
        elif code == 'missing': message = f'{label} wajib diisi.'
        elif code == 'string_too_short': message = f'{label} minimal {context.get("min_length")} karakter.'
        elif code == 'string_too_long': message = f'{label} maksimal {context.get("max_length")} karakter.'
        elif field == 'category': message = 'Pilih kategori Pengumuman, Kegiatan, atau Informasi.'
        elif field == 'phone': message = 'Gunakan nomor HP Indonesia yang valid, contoh 08xxxxxxxxxx.'
        elif field in ('code','current_pin','new_pin'): message = f'{label} harus berupa 6 digit angka.'
        elif field == 'period': message = 'Periode harus berupa YYYY-MM, contoh 2026-10.'
        elif field in ('date','due_date'): message = f'{label} harus berupa YYYY-MM-DD.'
        elif field == 'amount': message = 'Nominal harus berupa angka lebih dari 0 dan maksimal Rp100.000.000.'
        elif code == 'json_invalid': message = 'Format data belum dapat dibaca. Silakan coba lagi.'
        else: message = f'Periksa {label.lower()}. Formatnya belum sesuai.'
        messages.append(message)
        errors.append({'field':field, 'code':code})
    # Do not return or log rejected values: they may contain PINs, photos or report details.
    route = getattr(request.scope.get('route'), 'path', '/unknown')
    logging.getLogger('uvicorn.error').warning('Validation rejected: route=%s fields=%s', route,
                                             ','.join(f'{item["field"]}:{item["code"]}' for item in errors))
    return JSONResponse(status_code=422, content={'detail':'\n'.join(dict.fromkeys(messages)), 'errors':errors})

@app.exception_handler(UniqueViolation)
async def duplicate(request, exc):
    return JSONResponse(status_code=409, content={'detail': 'Data sudah terdaftar. Periksa nomor HP atau periode tagihan.'})

def audit(c, user, action, entity=''):
    c.execute('INSERT INTO audit_logs(actor_id,action,entity_id) VALUES(%s,%s,%s)', (user['id'], action, str(entity)))

def current(auth: HTTPAuthorizationCredentials = Depends(bearer)):
    if not auth: raise HTTPException(401, 'Silakan masuk terlebih dahulu.')
    with db() as c:
        user = c.execute('SELECT r.* FROM sessions s JOIN residents r ON r.id=s.resident_id WHERE s.token_hash=%s AND s.expires_at>now() AND r.active', (digest(auth.credentials),)).fetchone()
    if not user: raise HTTPException(401, 'Sesi berakhir. Silakan masuk kembali.')
    return user

def admin(user=Depends(current)):
    if user['role'] != 'admin': raise HTTPException(403, 'Hanya pengurus RT yang memiliki akses.')
    return user

def limit(key, maximum=8, minutes=15):
    with db() as c:
        row = c.execute('''INSERT INTO auth_limits(key,count) VALUES(%s,1) ON CONFLICT(key) DO UPDATE SET
        count=CASE WHEN auth_limits.window_start < now()-(%s * interval '1 minute') THEN 1 ELSE auth_limits.count+1 END,
        window_start=CASE WHEN auth_limits.window_start < now()-(%s * interval '1 minute') THEN now() ELSE auth_limits.window_start END RETURNING count''', (digest(key),minutes,minutes)).fetchone()
    if row['count'] > maximum: raise HTTPException(429, 'Terlalu banyak percobaan. Coba lagi dalam 15 menit.')

class Phone(BaseModel):
    phone: str = Field(max_length=24)
    @field_validator('phone')
    @classmethod
    def valid(cls,v): return normalize_phone(v)

class Login(Phone):
    code: str = Field(min_length=6,max_length=6,pattern=r'^\d{6}$')

def session_for(user):
    token=secrets.token_urlsafe(48)
    with db() as c:
        c.execute('DELETE FROM sessions WHERE expires_at<now()')
        c.execute('INSERT INTO sessions VALUES(%s,%s,%s)',(digest(token),user['id'],datetime.now(timezone.utc)+timedelta(days=14)))
        audit(c,user,'login')
    return {'token':token}

@app.get('/api/v1/health')
def health():
    with db() as c: c.execute('SELECT 1')
    return {'status':'ok','service':'RT Smart PRO','database':'postgresql'}

@app.get('/api/v1/config')
def config():
    return {'otp_enabled':bool(os.getenv('TWILIO_VERIFY_SERVICE_SID')), 'pin_enabled':True}

@app.post('/api/v1/auth/pin')
def login(body:Login, request:Request):
    limit('pin-phone:'+body.phone)
    limit('pin-ip:'+request.headers.get('x-real-ip',request.client.host),40)
    with db() as c: user=c.execute('SELECT * FROM residents WHERE phone=%s AND active',(body.phone,)).fetchone()
    # Always perform a password KDF, including unknown phone numbers.
    valid=check_pin(body.code,user['pin_hash'] if user and user['pin_hash'] else DUMMY_HASH)
    if not user or not valid: raise HTTPException(401,'Nomor HP atau PIN tidak sesuai.')
    return session_for(user)

DUMMY_HASH=pin_hash(secrets.token_hex(16))

def twilio(path,data):
    sid=os.getenv('TWILIO_VERIFY_SERVICE_SID')
    if not sid: raise HTTPException(503,'OTP belum diaktifkan. Gunakan PIN dari pengurus RT.')
    try:
        response=httpx.post('https://verify.twilio.com/v2/Services/'+sid+'/'+path,data=data,
            auth=(os.environ['TWILIO_ACCOUNT_SID'],os.environ['TWILIO_AUTH_TOKEN']),timeout=20)
        if response.status_code>=400: raise HTTPException(503,'Layanan OTP belum dapat memproses permintaan.')
        return response.json()
    except httpx.HTTPError: raise HTTPException(503,'Layanan OTP sedang tidak tersedia.')

@app.post('/api/v1/auth/otp/request')
def request_otp(body:Phone,request:Request):
    limit('otp:'+body.phone,3)
    limit('otp-ip:'+request.headers.get('x-real-ip',request.client.host),15)
    if not os.getenv('TWILIO_VERIFY_SERVICE_SID'): raise HTTPException(503,'OTP belum diaktifkan. Gunakan PIN dari pengurus RT.')
    with db() as c: user=c.execute('SELECT id FROM residents WHERE phone=%s AND active',(body.phone,)).fetchone()
    if user: twilio('Verifications',{'To':body.phone,'Channel':'sms','Locale':'id'})
    return {'message':'Jika nomor terdaftar, kode verifikasi akan dikirim.'}

@app.post('/api/v1/auth/otp/verify')
def verify_otp(body:Login,request:Request):
    limit('verify:'+body.phone)
    limit('verify-ip:'+request.headers.get('x-real-ip',request.client.host),40)
    result=twilio('VerificationCheck',{'To':body.phone,'Code':body.code})
    with db() as c: user=c.execute('SELECT * FROM residents WHERE phone=%s AND active',(body.phone,)).fetchone()
    if result.get('status')!='approved' or not user: raise HTTPException(401,'Kode verifikasi tidak sesuai atau telah kedaluwarsa.')
    return session_for(user)

@app.post('/api/v1/auth/logout')
def logout(auth:HTTPAuthorizationCredentials=Depends(bearer),user=Depends(current)):
    with db() as c: c.execute('DELETE FROM sessions WHERE token_hash=%s',(digest(auth.credentials),))
    return {'message':'Anda telah keluar.'}

@app.get('/api/v1/me')
def me(user=Depends(current)):
    with db() as c:
        community=c.execute('SELECT * FROM communities WHERE id=%s',(user['community_id'],)).fetchone()
        deletion=c.execute('SELECT id FROM deletion_requests WHERE resident_id=%s',(user['id'],)).fetchone()
    return {'id':user['id'],'name':user['name'],'phone':user['phone'],'role':user['role'],'address':user['address'],'community':community,'deletion_requested':bool(deletion)}

@app.get('/api/v1/dashboard')
def dashboard(user=Depends(current)):
    with db() as c:
        stats=c.execute('''SELECT count(*) FILTER(WHERE status='unpaid') AS unpaid_count,
          coalesce(sum(amount) FILTER(WHERE status='unpaid'),0) AS unpaid_amount,
          count(*) FILTER(WHERE status='pending') AS pending_count FROM invoices WHERE resident_id=%s''',(user['id'],)).fetchone()
        stats['resident_count']=c.execute('SELECT count(*) AS n FROM residents WHERE community_id=%s AND active',(user['community_id'],)).fetchone()['n']
        stats['pending_review']=c.execute("SELECT count(*) AS n FROM invoices WHERE community_id=%s AND status='pending'",(user['community_id'],)).fetchone()['n'] if user['role']=='admin' else 0
    return stats

class Resident(Phone):
    name:str=Field(min_length=2,max_length=100)
    address:str=Field(min_length=2,max_length=200)

@app.get('/api/v1/residents')
def residents(user=Depends(admin)):
    with db() as c: return c.execute('SELECT id,name,phone,address,role,demo,active FROM residents WHERE community_id=%s AND active ORDER BY name LIMIT 1000',(user['community_id'],)).fetchall()

@app.post('/api/v1/residents',status_code=201)
def add_resident(body:Resident,user=Depends(admin)):
    pin=''.join(secrets.choice('0123456789') for _ in range(6)); rid=uuid4()
    with db() as c:
        c.execute('INSERT INTO residents(id,community_id,name,phone,address,role,pin_hash) VALUES(%s,%s,%s,%s,%s,%s,%s)',(rid,user['community_id'],body.name,body.phone,body.address,'resident',pin_hash(pin)))
        audit(c,user,'resident.create',rid)
    return {'id':rid,'initial_pin':pin,'message':'Sampaikan PIN ini secara pribadi. Warga dapat menggantinya setelah masuk.'}


class ResidentUpdate(BaseModel):
    name:str=Field(min_length=2,max_length=100)
    address:str=Field(min_length=2,max_length=200)
    phone:str|None=Field(default=None,max_length=24)

    @field_validator('name','address',mode='before')
    @classmethod
    def trim_text(cls,value):
        return value.strip() if isinstance(value,str) else value

    @field_validator('phone')
    @classmethod
    def valid_phone(cls,value):
        return normalize_phone(value) if value and value.strip() else None


@app.put('/api/v1/residents/{rid}')
def edit_resident(rid:UUID,body:ResidentUpdate,user=Depends(admin)):
    initial_pin=None
    with db() as c:
        row=c.execute("SELECT * FROM residents WHERE id=%s AND community_id=%s AND role='resident' FOR UPDATE",(rid,user['community_id'])).fetchone()
        if not row or not row['active']: raise HTTPException(404,'Warga tidak ditemukan.')
        if not row['demo'] and not body.phone: raise HTTPException(422,'Nomor HP wajib diisi untuk akun warga.')
        credentials_changed=body.phone!=row['phone'] or bool(body.phone and not row['pin_hash'])
        encoded=row['pin_hash']
        if credentials_changed and body.phone:
            initial_pin=''.join(secrets.choice('0123456789') for _ in range(6))
            encoded=pin_hash(initial_pin)
        c.execute('UPDATE residents SET name=%s,address=%s,phone=%s,pin_hash=%s,demo=%s WHERE id=%s',
                  (body.name,body.address,body.phone,encoded,row['demo'] and not body.phone,rid))
        if credentials_changed: c.execute('DELETE FROM sessions WHERE resident_id=%s',(rid,))
        audit(c,user,'resident.update',rid)
    result={'message':'Data warga berhasil diperbarui.'}
    if initial_pin: result['initial_pin']=initial_pin
    return result


@app.delete('/api/v1/residents/{rid}')
def remove_resident(rid:UUID,user=Depends(admin)):
    with db() as c:
        row=c.execute("SELECT active FROM residents WHERE id=%s AND community_id=%s AND role='resident' FOR UPDATE",(rid,user['community_id'])).fetchone()
        if not row: raise HTTPException(404,'Warga tidak ditemukan.')
        if row['active']:
            c.execute('UPDATE residents SET active=false,phone=NULL,pin_hash=NULL WHERE id=%s',(rid,))
            c.execute('DELETE FROM sessions WHERE resident_id=%s',(rid,))
            audit(c,user,'resident.remove',rid)
    return {'message':'Warga dihapus dari daftar aktif dan akses akunnya dicabut. Riwayat IPL tetap disimpan untuk pertanggungjawaban kas.'}

@app.post('/api/v1/residents/{rid}/reset-pin')
def reset_pin(rid:UUID,user=Depends(admin)):
    pin=''.join(secrets.choice('0123456789') for _ in range(6))
    with db() as c:
        row=c.execute("UPDATE residents SET pin_hash=%s WHERE id=%s AND community_id=%s AND role='resident' AND active RETURNING id",(pin_hash(pin),rid,user['community_id'])).fetchone()
        if not row: raise HTTPException(404,'Warga tidak ditemukan.')
        c.execute('DELETE FROM sessions WHERE resident_id=%s',(rid,)); audit(c,user,'resident.reset_pin',rid)
    return {'initial_pin':pin}

class ChangePin(BaseModel):
    current_pin:str=Field(pattern=r'^\d{6}$')
    new_pin:str=Field(pattern=r'^\d{6}$')

@app.post('/api/v1/me/pin')
def change_pin(body:ChangePin,user=Depends(current)):
    limit('change-pin:'+str(user['id']))
    if not check_pin(body.current_pin,user['pin_hash']): raise HTTPException(401,'PIN saat ini tidak sesuai.')
    with db() as c:
        c.execute('UPDATE residents SET pin_hash=%s WHERE id=%s',(pin_hash(body.new_pin),user['id']))
        c.execute('DELETE FROM sessions WHERE resident_id=%s',(user['id'],)); audit(c,user,'pin.change')
    return {'message':'PIN diganti. Silakan masuk kembali.'}

class Invoice(BaseModel):
    title:str=Field(min_length=3,max_length=100)
    period:str=Field(pattern=r'^20\d{2}-(0[1-9]|1[0-2])$')
    amount:int=Field(gt=0,le=100000000)
    due_date:date
    resident_id:UUID|None=None

@app.get('/api/v1/invoices')
def invoices(user=Depends(current)):
    with db() as c:
        return c.execute('''SELECT i.*,r.name AS resident_name FROM invoices i JOIN residents r ON r.id=i.resident_id
          WHERE i.community_id=%s AND (%s='admin' OR i.resident_id=%s) ORDER BY i.due_date DESC,r.name LIMIT 500''',(user['community_id'],user['role'],user['id'])).fetchall()

@app.post('/api/v1/invoices',status_code=201)
def create_invoices(body:Invoice,user=Depends(admin)):
    with db() as c:
        rows=c.execute('SELECT id FROM residents WHERE community_id=%s AND active AND (%s::uuid IS NULL OR id=%s)',(user['community_id'],body.resident_id,body.resident_id)).fetchall()
        if not rows: raise HTTPException(404,'Warga tidak ditemukan.')
        for row in rows:
            c.execute('''INSERT INTO invoices(id,community_id,resident_id,title,period,amount,due_date) VALUES(%s,%s,%s,%s,%s,%s,%s)
            ON CONFLICT(resident_id,period,title) DO NOTHING''',(uuid4(),user['community_id'],row['id'],body.title,body.period,body.amount,body.due_date))
        audit(c,user,'invoice.batch',body.period)
    return {'message':'Tagihan berhasil dibuat tanpa menggandakan periode yang sama.'}

class Payment(BaseModel):
    reference:str=Field(min_length=5,max_length=200)

@app.post('/api/v1/invoices/{iid}/confirm')
def confirm(iid:UUID,body:Payment,user=Depends(current)):
    with db() as c:
        row=c.execute("UPDATE invoices SET status='pending',reference=%s WHERE id=%s AND resident_id=%s AND community_id=%s AND status='unpaid' RETURNING id",(body.reference,iid,user['id'],user['community_id'])).fetchone()
        if not row: raise HTTPException(409,'Tagihan tidak tersedia atau sudah dikonfirmasi.')
        audit(c,user,'payment.submit',iid)
    return {'message':'Konfirmasi diterima. Pengurus akan memeriksa mutasi rekening.'}

class Review(BaseModel):
    approve:bool

@app.post('/api/v1/invoices/{iid}/review')
def review(iid:UUID,body:Review,user=Depends(admin)):
    with db() as c:
        row=c.execute("UPDATE invoices SET status=%s,paid_at=CASE WHEN %s THEN now() ELSE NULL END WHERE id=%s AND community_id=%s AND status='pending' RETURNING id",('paid' if body.approve else 'unpaid',body.approve,iid,user['community_id'])).fetchone()
        if not row: raise HTTPException(409,'Konfirmasi sudah diproses atau tidak ditemukan.')
        audit(c,user,'payment.approve' if body.approve else 'payment.reject',iid)
    return {'message':'Status pembayaran diperbarui.'}

class PhotoPost(BaseModel):
    photo_base64: str | None = Field(default=None, max_length=MAX_PHOTO_BASE64)

    @field_validator('title', 'body', mode='before', check_fields=False)
    @classmethod
    def trim_text(cls, value):
        return value.strip() if isinstance(value, str) else value


class News(PhotoPost):
    title:str=Field(min_length=3,max_length=140)
    body:str=Field(min_length=10,max_length=10000)
    category:str=Field(pattern=r'^(Pengumuman|Kegiatan|Informasi)$')

    @field_validator('category', mode='before')
    @classmethod
    def normalize_category(cls, value):
        if isinstance(value, str):
            value = value.strip()
            return {item.casefold():item for item in ('Pengumuman','Kegiatan','Informasi')}.get(value.casefold(), value)
        return value

@app.get('/api/v1/news')
def news(user=Depends(current)):
    with db() as c: return c.execute('SELECT n.*,r.name AS author,f.id AS photo_id FROM news n JOIN residents r ON r.id=n.author_id LEFT JOIN photos f ON f.news_id=n.id WHERE n.community_id=%s ORDER BY n.created_at DESC LIMIT 200',(user['community_id'],)).fetchall()

@app.post('/api/v1/news',status_code=201)
def publish(body:News,user=Depends(admin)):
    if body.photo_base64: limit('photo-upload:'+str(user['id']),40)
    photo = decode_photo(body.photo_base64)
    nid=uuid4()
    with db() as c:
        c.execute('INSERT INTO news(id,community_id,title,body,category,author_id) VALUES(%s,%s,%s,%s,%s,%s)',(nid,user['community_id'],body.title,body.body,body.category,user['id'])); audit(c,user,'news.publish',nid)
        attach_photo(c, user, photo, news_id=nid)
    return {'id':nid}

@app.delete('/api/v1/news/{nid}')
def delete_news(nid:UUID,user=Depends(admin)):
    with db() as c:
        row=c.execute('DELETE FROM news WHERE id=%s AND community_id=%s RETURNING id',(nid,user['community_id'])).fetchone()
        if not row: raise HTTPException(404,'Berita tidak ditemukan.')
        audit(c,user,'news.delete',nid)
    return {'message':'Berita dihapus.'}

@app.get('/api/v1/finance')
def finance(user=Depends(current)):
    with db() as c:
        income=c.execute("SELECT coalesce(sum(amount),0) AS n FROM invoices WHERE community_id=%s AND status='paid'",(user['community_id'],)).fetchone()['n']
        expenses=c.execute('SELECT id,title,amount,date FROM expenses WHERE community_id=%s ORDER BY date DESC LIMIT 500',(user['community_id'],)).fetchall()
        total=c.execute('SELECT coalesce(sum(amount),0) AS n FROM expenses WHERE community_id=%s',(user['community_id'],)).fetchone()['n']
    return {'income':income,'expense':total,'balance':income-total,'expenses':expenses}

class Expense(BaseModel):
    title:str=Field(min_length=3,max_length=140)
    amount:int=Field(gt=0,le=100000000)
    date:date

@app.post('/api/v1/expenses',status_code=201)
def expense(body:Expense,user=Depends(admin)):
    eid=uuid4()
    with db() as c:
        c.execute('INSERT INTO expenses VALUES(%s,%s,%s,%s,%s,%s)',(eid,user['community_id'],body.title,body.amount,body.date,user['id'])); audit(c,user,'expense.create',eid)
    return {'id':eid}

class Report(PhotoPost):
    title:str=Field(min_length=3,max_length=140)
    body:str=Field(min_length=10,max_length=3000)

@app.get('/api/v1/reports')
def reports(user=Depends(current)):
    with db() as c: return c.execute("SELECT p.*,r.name AS resident_name,f.id AS photo_id FROM reports p JOIN residents r ON r.id=p.resident_id LEFT JOIN photos f ON f.report_id=p.id WHERE p.community_id=%s AND (%s='admin' OR p.resident_id=%s) ORDER BY p.created_at DESC LIMIT 300",(user['community_id'],user['role'],user['id'])).fetchall()

@app.post('/api/v1/reports',status_code=201)
def report(body:Report,user=Depends(current)):
    if body.photo_base64: limit('photo-upload:'+str(user['id']),40)
    photo = decode_photo(body.photo_base64)
    rid=uuid4()
    with db() as c:
        c.execute('INSERT INTO reports(id,community_id,resident_id,title,body) VALUES(%s,%s,%s,%s,%s)',(rid,user['community_id'],user['id'],body.title,body.body)); audit(c,user,'report.create',rid)
        attach_photo(c, user, photo, report_id=rid)
    return {'id':rid}


@app.get('/api/v1/photos/{pid}')
def photo(pid: UUID, user=Depends(current)):
    with db() as c:
        row = c.execute('''SELECT f.data FROM photos f LEFT JOIN reports p ON p.id=f.report_id
            WHERE f.id=%s AND f.community_id=%s AND
            (f.news_id IS NOT NULL OR %s='admin' OR p.resident_id=%s)''',
            (pid, user['community_id'], user['role'], user['id'])).fetchone()
    if not row:
        raise HTTPException(404, 'Foto tidak ditemukan.')
    return Response(bytes(row['data']), media_type='image/jpeg',
                    headers={'Cache-Control':'private, no-store', 'X-Content-Type-Options':'nosniff'})

@app.post('/api/v1/reports/{rid}/resolve')
def resolve(rid:UUID,user=Depends(admin)):
    with db() as c:
        row=c.execute("UPDATE reports SET status='resolved' WHERE id=%s AND community_id=%s RETURNING id",(rid,user['community_id'])).fetchone()
        if not row: raise HTTPException(404,'Laporan tidak ditemukan.')
        audit(c,user,'report.resolve',rid)
    return {'message':'Laporan ditandai selesai.'}

class Bank(BaseModel):
    bank:str=Field(min_length=2,max_length=60)
    account:str=Field(min_length=5,max_length=40,pattern=r'^\d+$')
    holder:str=Field(min_length=2,max_length=100)

@app.put('/api/v1/community/bank')
def bank(body:Bank,user=Depends(admin)):
    with db() as c:
        c.execute('UPDATE communities SET bank=%s,account=%s,holder=%s WHERE id=%s',(body.bank,body.account,body.holder,user['community_id'])); audit(c,user,'bank.update')
    return {'message':'Rekening IPL diperbarui.'}

@app.post('/api/v1/me/deletion-request')
def request_deletion(user=Depends(current)):
    with db() as c:
        c.execute('INSERT INTO deletion_requests(id,resident_id) VALUES(%s,%s) ON CONFLICT(resident_id) DO NOTHING',(uuid4(),user['id'])); audit(c,user,'account.deletion_request')
    return {'message':'Permintaan penghapusan dicatat. Hubungi seandyadryan2@gmail.com untuk tindak lanjut maksimal 30 hari.'}

app.mount('/',StaticFiles(directory=Path(__file__).with_name('public'),html=True),name='public')
