"""Idempotent initial setup. Prints the random initial PIN only at first creation."""
import os, secrets
from uuid import uuid4
from datetime import date
from pathlib import Path
from app import db, pin_hash, normalize_phone

def bootstrap():
    with db() as c:
        c.execute(Path(__file__).with_name('schema.sql').read_text())
        phone=normalize_phone(os.environ['ADMIN_PHONE'])
        if c.execute('SELECT id FROM residents WHERE phone=%s',(phone,)).fetchone():
            print('Pengurus sudah ada; tidak mengubah PIN atau data.'); return
        cid,aid=uuid4(),uuid4()
        pin=''.join(secrets.choice('0123456789') for _ in range(6))
        c.execute('INSERT INTO communities(id,name) VALUES(%s,%s)',(cid,os.environ['COMMUNITY_NAME']))
        c.execute('INSERT INTO residents(id,community_id,name,phone,address,role,pin_hash) VALUES(%s,%s,%s,%s,%s,%s,%s)',(aid,cid,os.environ['ADMIN_NAME'],phone,'Pengurus RT','admin',pin_hash(pin)))
        if os.getenv('SEED_DEMO')=='true':
            for i,name in enumerate(['Budi Santoso','Siti Rahmawati','Andi Pratama','Dewi Lestari','Rizky Saputra','Rina Wulandari'],1):
                rid=uuid4()
                c.execute("INSERT INTO residents(id,community_id,name,address,role,demo) VALUES(%s,%s,%s,%s,'resident',true)",(rid,cid,name+' (Contoh)',f'Blok A / {i:02d}'))
                c.execute("INSERT INTO invoices(id,community_id,resident_id,title,period,amount,due_date) VALUES(%s,%s,%s,'IPL bulanan • Data contoh',%s,150000,%s)",(uuid4(),cid,rid,date.today().strftime('%Y-%m'),date.today().replace(day=28)))
            c.execute("INSERT INTO news(id,community_id,title,body,category,author_id) VALUES(%s,%s,%s,%s,'Informasi',%s)",(uuid4(),cid,'Selamat datang di lingkungan yang lebih terhubung','Ini adalah berita contoh untuk melihat tampilan aplikasi. Pengurus dapat menghapusnya dan menerbitkan informasi resmi RT. Data warga bertanda Contoh adalah data dummy, bukan warga asli.',aid))
            c.execute("INSERT INTO invoices(id,community_id,resident_id,title,period,amount,due_date) VALUES(%s,%s,%s,'IPL bulanan • Data contoh',%s,150000,%s)",(uuid4(),cid,aid,date.today().strftime('%Y-%m'),date.today().replace(day=28)))
        print('INITIAL_PIN='+pin)

if __name__=='__main__': bootstrap()
