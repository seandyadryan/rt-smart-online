import os
from uuid import uuid4
from datetime import date
import pytest
from fastapi.testclient import TestClient
from app import app, db, pin_hash, session_for, normalize_phone

@pytest.fixture(autouse=True)
def clean(monkeypatch):
    for name in ('TWILIO_ACCOUNT_SID','TWILIO_AUTH_TOKEN','TWILIO_VERIFY_SERVICE_SID'):
        monkeypatch.setenv(name,'')
    assert os.environ['DATABASE_URL'].split('/')[-1]=='rtsmartpro_test', 'Tests must use a separate test database'
    with db() as c:
        c.execute(open('schema.sql').read())
        c.execute('TRUNCATE deletion_requests,audit_logs,reports,expenses,news,invoices,sessions,auth_limits,residents,communities CASCADE')
    yield

@pytest.fixture
def actors():
    users=[]
    with db() as c:
        for index,role in enumerate(['admin','resident','admin']):
            cid = users[0]['community_id'] if index==1 else uuid4()
            if index!=1: c.execute('INSERT INTO communities(id,name) VALUES(%s,%s)',(cid,'Test RT'))
            user={'id':uuid4(),'community_id':cid,'role':role,'phone':f'+628123450000{index}'}
            c.execute('INSERT INTO residents(id,community_id,name,phone,address,role,pin_hash) VALUES(%s,%s,%s,%s,%s,%s,%s)',(user['id'],cid,'Test',user['phone'],'A1',role,pin_hash('123456')))
            users.append(user)
    for user in users: user['headers']={'Authorization':'Bearer '+session_for(user)['token']}
    return users

client=TestClient(app)

def test_phone_normalization():
    assert normalize_phone('085812345678')=='+6285812345678'
    with pytest.raises(ValueError): normalize_phone('123')

def test_login_and_logout(actors):
    a=actors[0]
    assert client.post('/api/v1/auth/pin',json={'phone':a['phone'],'code':'000000'}).status_code==401
    response=client.post('/api/v1/auth/pin',json={'phone':a['phone'],'code':'123456'})
    assert response.status_code==200
    headers={'Authorization':'Bearer '+response.json()['token']}
    assert client.get('/api/v1/me',headers=headers).json()['role']=='admin'
    assert client.post('/api/v1/auth/logout',headers=headers).status_code==200
    assert client.get('/api/v1/me',headers=headers).status_code==401

def test_only_admin_can_add_residents(actors):
    admin,resident,foreign=actors
    data={'name':'Warga Baru','phone':'081234567890','address':'Blok B1'}
    assert client.post('/api/v1/residents',json=data).status_code==401
    assert client.post('/api/v1/residents',json=data,headers=resident['headers']).status_code==403
    response=client.post('/api/v1/residents',json=data,headers=admin['headers'])
    assert response.status_code==201 and len(response.json()['initial_pin'])==6
    assert len(client.get('/api/v1/residents',headers=foreign['headers']).json())==1
    assert client.post('/api/v1/residents',json=data,headers=admin['headers']).status_code==409

def test_invoice_authorization_and_payment(actors):
    a,r,f=actors
    data={'title':'IPL Oktober','period':'2026-10','amount':150000,'due_date':str(date.today()),'resident_id':str(r['id'])}
    assert client.post('/api/v1/invoices',json=data,headers=a['headers']).status_code==201
    client.post('/api/v1/invoices',json=data,headers=a['headers'])
    rows=client.get('/api/v1/invoices',headers=r['headers']).json()
    assert len(rows)==1
    iid=rows[0]['id']
    assert client.get('/api/v1/invoices',headers=f['headers']).json()==[]
    assert client.post(f'/api/v1/invoices/{iid}/confirm',json={'reference':'Transfer Bank'},headers=f['headers']).status_code==409
    assert client.post(f'/api/v1/invoices/{iid}/confirm',json={'reference':'Transfer Bank'},headers=r['headers']).status_code==200
    assert client.post(f'/api/v1/invoices/{iid}/review',json={'approve':True},headers=r['headers']).status_code==403
    assert client.post(f'/api/v1/invoices/{iid}/review',json={'approve':True},headers=f['headers']).status_code==409
    assert client.post(f'/api/v1/invoices/{iid}/review',json={'approve':True},headers=a['headers']).status_code==200
    assert client.post(f'/api/v1/invoices/{iid}/review',json={'approve':True},headers=a['headers']).status_code==409
    assert client.get('/api/v1/finance',headers=r['headers']).json()['income']==150000

def test_news_report_privacy(actors):
    a,r,f=actors
    data={'title':'Kerja bakti','body':'Kerja bakti bersama hari Minggu.','category':'Kegiatan'}
    assert client.post('/api/v1/news',json=data,headers=r['headers']).status_code==403
    assert client.post('/api/v1/news',json=data,headers=a['headers']).status_code==201
    assert len(client.get('/api/v1/news',headers=r['headers']).json())==1
    assert client.get('/api/v1/news',headers=f['headers']).json()==[]
    report=client.post('/api/v1/reports',json={'title':'Lampu mati','body':'Lampu di jalan blok A mati.'},headers=r['headers']).json()
    assert client.get('/api/v1/reports',headers=f['headers']).json()==[]
    assert client.post(f'/api/v1/reports/{report["id"]}/resolve',headers=f['headers']).status_code==404
    assert client.post(f'/api/v1/reports/{report["id"]}/resolve',headers=a['headers']).status_code==200

def test_pin_rate_limit_and_otp_disabled(actors):
    phone=actors[0]['phone']
    for _ in range(8): assert client.post('/api/v1/auth/pin',json={'phone':phone,'code':'000000'}).status_code==401
    assert client.post('/api/v1/auth/pin',json={'phone':phone,'code':'123456'}).status_code==429
    assert client.post('/api/v1/auth/otp/request',json={'phone':phone}).status_code==503
    assert client.post('/api/v1/auth/otp/verify',json={'phone':phone,'code':'123456'}).status_code==503

def test_pin_change_revokes_sessions_and_deletion(actors):
    a,r,f=actors
    assert client.post('/api/v1/me/deletion-request',headers=r['headers']).status_code==200
    assert client.get('/api/v1/me',headers=r['headers']).json()['deletion_requested'] is True
    assert client.post('/api/v1/me/pin',headers=r['headers'],json={'current_pin':'123456','new_pin':'654321'}).status_code==200
    assert client.get('/api/v1/me',headers=r['headers']).status_code==401
    assert client.post('/api/v1/auth/pin',json={'phone':r['phone'],'code':'654321'}).status_code==200

def test_finance_expenses_authorized(actors):
    a,r,f=actors
    data={'title':'Perbaikan lampu','amount':75000,'date':str(date.today())}
    assert client.post('/api/v1/expenses',json=data,headers=r['headers']).status_code==403
    assert client.post('/api/v1/expenses',json=data,headers=a['headers']).status_code==201
    assert client.get('/api/v1/finance',headers=r['headers']).json()['expense']==75000
    assert client.get('/api/v1/finance',headers=f['headers']).json()['expense']==0
    data['amount']=-1
    assert client.post('/api/v1/expenses',json=data,headers=a['headers']).status_code==422
