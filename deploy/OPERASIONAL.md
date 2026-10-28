# Pengelolaan beberapa RT

Setiap akun terikat ke satu lingkungan RT. Rekening, nominal tagihan, warga, berita, laporan, dan kas mengikuti RT akun tersebut. Pengurus tidak dapat membaca atau mengubah data RT lain melalui API.

## Pengurus RT baru

Pendaftaran lingkungan dimulai dari kartu **Daftarkan lingkungan baru** di layar login atau https://rtsmartpro.amarlo.online/pendaftaran/. Harga layanan Rp150.000 per bulan per lingkungan, tanpa minimal rumah; terpisah dari IPL. Tombol membuka draf WhatsApp ke +6285814654689. Konfirmasi langganan, periode, pembayaran, perpanjangan/penghentian, dan aktivasi ditangani operator secara manual; belum ada penagihan berulang atau masa aktif otomatis di database.

Operator server membuat pengurus pertama setelah konfirmasi pendaftaran. Warga tetap hanya dapat ditambahkan oleh pengurus RT. Gunakan nomor HP baru yang belum terdaftar:

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

Di **Lainnya → Data warga**, pilih **Edit** untuk memperbarui nama, nomor HP, dan alamat. Nomor HP yang berubah menghasilkan PIN baru sekali tampil dan mencabut sesi lama. Data contoh boleh diedit tanpa nomor HP; mengisi nomor HP mengubahnya menjadi akun warga dan menghasilkan PIN awal.

Pilih **Hapus**, lalu konfirmasi, untuk mengeluarkan warga dari daftar aktif. Akses login dicabut dan warga tidak lagi mendapat tagihan periode baru. Tagihan, pembayaran lama, dan kas tetap tersimpan. Nomor HP dilepas agar dapat didaftarkan kembali; akun baru tidak memperoleh riwayat akun sebelumnya. Penghapusan ini bukan prosedur penghapusan seluruh data pribadi; permintaan privasi tetap diproses sesuai kebijakan.

Data awal bertanda **Contoh** adalah simulasi. Selesaikan pengujian sebelum memakai kas untuk pencatatan operasional. Jangan memperlakukan pembayaran simulasi sebagai penerimaan bank.

Permintaan penghapusan akun masuk ke tabel `deletion_requests` untuk diproses operator sesuai kebijakan privasi. Penghapusan dan anonimisasi data keuangan dilakukan dengan pemeriksaan manual serta cadangan terlebih dahulu. Pantau email dukungan dan permintaan akun secara berkala.
