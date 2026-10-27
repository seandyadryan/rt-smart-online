# Pengelolaan beberapa RT

Setiap akun terikat ke satu lingkungan RT. Rekening, nominal tagihan, warga, berita, laporan, dan kas mengikuti RT akun tersebut. Pengurus tidak dapat membaca atau mengubah data RT lain melalui API.

## Pengurus RT baru

Operator server membuat pengurus pertama untuk setiap RT. Tidak ada pendaftaran pengurus terbuka di aplikasi. Gunakan nomor HP baru yang belum terdaftar:

```bash
cd /opt/rtsmartpro
sudo docker compose exec -T \
  -e ADMIN_NAME='Nama Pengurus Baru' \
  -e ADMIN_PHONE='085812345678' \
  -e COMMUNITY_NAME='RT 02 / Nama Perumahan' \
  -e SEED_DEMO=false api python bootstrap.py
```

PIN awal acak hanya tampil sekali. Sampaikan secara privat kepada pengurus, lalu minta ia mengganti PIN lewat aplikasi. Menjalankan ulang dengan nomor yang sudah terdaftar tidak mengganti PIN atau data. Contoh nomor di atas harus diganti sebelum menjalankan perintah.

## Rekening dan IPL

1. Pengurus login, buka **Lainnya → Rekening IPL**, kemudian isi bank, nomor rekening, dan nama pemilik rekening milik RT tersebut.
2. Buka **Lainnya → Data warga** untuk mendaftarkan warga. Pengurus menyerahkan nomor login dan PIN awal secara privat.
3. Dari beranda, pilih **Buat IPL**, kemudian isi judul, periode, nominal, dan jatuh tempo. Nominal diisi pengurus untuk RT-nya sendiri. Periode yang sama tidak menggandakan tagihan warga.
4. Warga melakukan transfer dan mengirim konfirmasi. Pengurus memeriksa mutasi bank sebelum menyetujui. Aplikasi tidak menarik uang atau memverifikasi mutasi bank secara otomatis.
5. Hanya pembayaran disetujui yang masuk pemasukan kas. Catat pengeluaran melalui menu pengurus.

## Data contoh dan permintaan penghapusan

Data awal bertanda **Contoh** adalah simulasi. Selesaikan pengujian sebelum memakai kas untuk pencatatan operasional. Jangan memperlakukan pembayaran simulasi sebagai penerimaan bank.

Permintaan penghapusan akun masuk ke tabel `deletion_requests` untuk diproses operator sesuai kebijakan privasi. Penghapusan dan anonimisasi data keuangan dilakukan dengan pemeriksaan manual serta cadangan terlebih dahulu. Pantau email dukungan dan permintaan akun secara berkala.
