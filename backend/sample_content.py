"""Owner-authorized demo data. Never creates phone numbers or bank accounts."""
import os
from app import db, normalize_phone
from uuid import uuid4
from datetime import date

with db() as c:
    admin=c.execute('SELECT * FROM residents WHERE phone=%s',(normalize_phone(os.environ['ADMIN_PHONE']),)).fetchone()
    if not admin: raise SystemExit('Bootstrap required')
    cid=admin['community_id']
    # Demo invoices alone are marked paid; no actual resident payments are modified.
    demos=c.execute('SELECT id FROM residents WHERE community_id=%s AND demo ORDER BY name LIMIT 3',(cid,)).fetchall()
    for r in demos:
        c.execute("UPDATE invoices SET status='paid',paid_at=now(),reference='SIMULASI • bukan transaksi nyata' WHERE resident_id=%s AND title LIKE '%%Data contoh%%'",(r['id'],))
    for title,amount in [('Perawatan taman • Contoh',90000),('Lampu jalan • Contoh',65000),('Perlengkapan kebersihan • Contoh',45000)]:
        if not c.execute('SELECT id FROM expenses WHERE community_id=%s AND title=%s',(cid,title)).fetchone():
            c.execute('INSERT INTO expenses VALUES(%s,%s,%s,%s,%s,%s)',(uuid4(),cid,title,amount,date.today(),admin['id']))
    for title,body,category in [
        ('Pagi yang bersih, lingkungan yang nyaman','DATA CONTOH. Contoh pengumuman kerja bakti lingkungan. Pengurus dapat menggantinya dengan jadwal, titik kumpul, dan perlengkapan yang benar sebelum dibagikan kepada warga.','Kegiatan'),
        ('Iuran teratur untuk kebutuhan bersama','DATA CONTOH. Informasi contoh mengenai pencatatan IPL. Warga dapat melihat tagihan pada menu IPL. Rekening tujuan transfer harus diisi pengurus dengan rekening asli yang telah disepakati.','Pengumuman')
    ]:
        if not c.execute('SELECT id FROM news WHERE community_id=%s AND title=%s',(cid,title)).fetchone():
            c.execute('INSERT INTO news(id,community_id,title,body,category,author_id) VALUES(%s,%s,%s,%s,%s,%s)',(uuid4(),cid,title,body,category,admin['id']))
print('Demo content ready. No real payments or phone numbers created.')
