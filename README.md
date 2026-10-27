# RT Smart PRO — IPL & News

Aplikasi Android Kotlin native/Jetpack Compose dengan API Python FastAPI dan PostgreSQL. Seluruh koneksi aplikasi menuju **https://rtsmartpro.amarlo.online/**. Package: `com.deploydulupulangnanti.rtsmartpro`.

![RT Smart PRO IPL & News](store/feature-1024x500.png)

## Fitur

- Login nomor HP Indonesia + PIN 6 digit, penyimpanan sesi lewat Android Keystore, opsi Twilio Verify OTP setelah konfigurasi.
- Hanya pengurus RT dapat mendaftarkan warga dan membuat PIN awal. Warga dapat mengganti PIN. Reset PIN mencabut sesi lama.
- Tagihan IPL per periode, konfirmasi transfer manual, persetujuan/penolakan pengurus, dan riwayat status. Pembuatan periode yang sama tidak menggandakan tagihan.
- Berita/pengumuman RT, ringkasan kas dari pembayaran terverifikasi, pencatatan pengeluaran, laporan warga dan status tindak lanjut.
- Satu foto opsional pada kabar RT dan laporan lingkungan, dengan pratinjau, ganti/hapus sebelum dikirim, serta tampilan foto yang dapat diperbesar. Tanpa foto tetap dapat diterbitkan. Photo Picker hanya memberikan akses ke gambar yang dipilih pengguna.
- Isolasi data antar-RT di API, audit aktivitas, pembatasan percobaan login di PostgreSQL, pencabutan sesi saat logout.
- Layout ponsel/tablet/layar lebar, UI bahasa Indonesia, halaman kebijakan privasi serta pengajuan penghapusan akun.

## Android

JDK 17+, Android SDK 36. Gradle 8.14, AGP 8.13.1, Kotlin 2.2.20, Compose BOM 2025.09.01. Versi stabil dipilih agar sesuai dengan SDK dan toolchain yang telah tersedia. Minimum Android 8 (API 26), target API 36.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:RTSMART_KEYSTORE = 'D:\KEYSTORE\rtsmartpro.jks'
# Isi RTSMART_STORE_PASSWORD dan RTSMART_KEY_PASSWORD dari penyimpanan rahasia.
.\gradlew.bat assembleDebug lintRelease assembleRelease bundleRelease
```

Tanpa variabel signing, gunakan `assembleDebug` untuk pengembangan. Release memerlukan keystore. Tidak ada endpoint emulator atau staging yang disisipkan ke aplikasi.

Build rilis standar tidak menjalankan minifikasi agar dapat dibangun pada mesin dengan sumber daya terbatas. Tambahkan `-PoptimizeRelease=true` untuk minifikasi dan penyusutan resource opsional. Kedua konfigurasi memakai signing dan fitur produksi yang sama.

## Server yang dipasang

Project: `/opt/rtsmartpro` di Ubuntu ARM64. API Docker mendengarkan hanya pada `127.0.0.1:38081`. PostgreSQL Docker mendengarkan hanya pada `127.0.0.1:35432`. Caddy existing menangani TLS dan reverse proxy lewat jaringan `ai-chat_default`. Domain publik dipakai untuk API dan halaman privasi. Port PostgreSQL tidak dipublikasikan ke internet.

```bash
cd /opt/rtsmartpro
sudo docker compose ps
curl -f https://rtsmartpro.amarlo.online/api/v1/health
bash deploy/test.sh
```

Bootstrap menggunakan variabel `ADMIN_NAME`, `ADMIN_PHONE`, `COMMUNITY_NAME`, `SEED_DEMO`. Hanya bootstrap pertama mencetak PIN acak. Tidak ada default PIN produksi. Data dummy diberi nama/label Contoh dan tidak memiliki nomor HP untuk login. `backend/sample_content.py` menambah konten simulasi yang sudah diotorisasi pemilik; tidak dijalankan otomatis pada pembaruan layanan.

## OTP

Tambahkan `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, dan `TWILIO_VERIFY_SERVICE_SID` ke `.env` server, lalu `sudo docker compose up -d`. Restart aplikasi agar membaca konfigurasi. Selama provider belum aktif, API OTP mengembalikan 503 dan UI menggunakan PIN. Tidak ada bypass OTP atau kode tetap. Twilio dan biaya pengiriman harus diaktifkan pemilik akun provider. Lihat [dokumentasi Twilio Verify](https://www.twilio.com/docs/verify/api/verification).

## Pengujian

`backend/test_app.py` menguji autentikasi, pembatasan login, akses pengurus, isolasi data antar-RT, invoice idempotent, perpindahan status pembayaran, kas, berita, laporan, pencabutan sesi dan penghapusan akun. Pengujian hanya berjalan pada database `rtsmartpro_test`, terpisah dari database operasional.

Screenshot dan smoke test Android dijalankan pada emulator lokal. Sumber screenshot asli disimpan lokal dalam `captures` (diabaikan Git); aset final di `store`. Generator aset: `npm ci --prefix tools` lalu `node tools/assets.mjs` setelah screenshot tersedia.

## Operasional

Foto disimpan sebagai JPEG dalam PostgreSQL sehingga ikut dalam cadangan pg_dump. Backend memvalidasi JPEG/PNG/WebP maksimal 2 MB, memperkecil gambar, dan membuang metadata EXIF/GPS. Android menyiapkan foto secara otomatis sebelum upload. Endpoint foto membutuhkan sesi: foto kabar dapat dilihat warga RT yang sama, sedangkan foto laporan hanya oleh pelapor dan pengurus RT-nya. Migrasi tabel `photos` otomatis saat API dimulai; data lama tanpa foto tetap didukung. Reverse proxy menerima request maksimal 4 MB untuk payload gambar terenkode.

Lihat [panduan pengelolaan beberapa RT](deploy/OPERASIONAL.md) untuk membuat pengurus RT baru, mengatur rekening masing-masing RT, serta menjalankan alur IPL. Hasil pemeriksaan rilis tersedia di [VERIFIKASI.md](VERIFIKASI.md).

Cadangan pg_dump berjalan setiap hari pukul 02.00 WIB melalui systemd timer, retensi 14 hari di `/opt/rtsmartpro/backups`. Sebelum deploy, workflow membuat cadangan. Cadangan ini berada pada server yang sama; pindahkan salinan ke penyimpanan terpisah jika dibutuhkan pemulihan saat server hilang. Pemulihan dump dilakukan ke database baru terlebih dahulu, kemudian verifikasi sebelum mengganti database aktif.

Permintaan penghapusan tercatat di tabel `deletion_requests`; operator menindaklanjuti melalui dukungan dan menghapus/menganonimkan data sesuai kebijakan. Tinjau daftar ini dan kotak email dukungan secara berkala. Data keuangan yang dipertahankan harus tidak lagi mengandung identitas atau referensi transfer pribadi setelah penghapusan.

## GitHub Actions

Kedua workflow berjalan manual untuk menghindari penggunaan runner saat push. Runner `rtsmartpro-ubuntu-arm64` terdaftar sebagai layanan Ubuntu. Tidak ada trigger `pull_request`. Android memakai hosted runner dengan tiga GitHub Actions secrets: `RTSMART_KEYSTORE_BASE64`, `RTSMART_STORE_PASSWORD`, `RTSMART_KEY_PASSWORD`.

Upload key, PIN pengurus, password DB, kunci SSH, token runner, dan profil Navicat privat tidak masuk Git. Lihat `store/PANDUAN-PLAY-CONSOLE.md` untuk penyerahan listing.
