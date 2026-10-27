# Perbaikan validasi API 1.0.2 — APK tetap 1.0.1

- Pemeriksaan PostgreSQL produksi mengonfirmasi tabel `photos` sudah tersedia. Tidak diperlukan perubahan skema atau pembaruan database manual untuk perbaikan ini.
- Log produksi menunjukkan penolakan validasi HTTP 422 pada pengiriman kabar RT. Log lama tidak menyimpan detail kolom atau nilai yang ditolak, sehingga penyebab kolom spesifik pada percobaan lama tidak dapat dipastikan.
- Respons validasi kini mengirim teks Indonesia yang menyebut kolom dan batasannya. Kompatibel dengan penanganan error APK yang sudah terpasang. Log hanya berisi nama kolom dan jenis validasi, tanpa PIN, isi laporan, maupun gambar.
- Kategori kabar RT dinormalisasi untuk variasi huruf besar/kecil dan spasi. Judul serta isi dibersihkan dari spasi di tepi; batas minimal judul 3 karakter dan isi 10 karakter tetap berlaku. Foto tetap opsional.
- 17 pengujian backend kandidat lulus. Uji HTTPS produksi membuktikan respons validasi terbaca, publikasi tanpa foto berhasil, dan upload foto tetap berfungsi. Seluruh data uji sementara dibersihkan. Kode Android tidak berubah sehingga tidak memerlukan APK baru.

## Pemeriksaan rilis 1.0.1 (2) — foto opsional

- Foto opsional tersedia pada form kabar RT dan laporan lingkungan. Pengguna dapat memilih, melihat pratinjau, mengganti, menghapus sebelum kirim, dan memperbesar foto yang telah dimuat. Form tetap dapat dikirim tanpa foto; kesalahan pengiriman mempertahankan isi form.
- Backend kandidat: 14 pengujian integrasi lulus pada database terpisah. Mencakup publikasi tanpa foto, validasi ukuran/format, normalisasi orientasi dan pembuangan EXIF, akses lintas RT, pembatasan foto laporan bagi pelapor/pengurus, serta penghapusan foto saat berita dihapus.
- Uji HTTPS produksi dengan RT sementara: kirim tanpa foto berhasil; upload gambar dengan payload melebihi 128 KB berhasil melewati reverse proxy; JPEG dapat diambil dengan sesi dan ditolak tanpa sesi. RT, berita, foto, dan sesi uji sudah dihapus.
- `lintRelease assembleRelease bundleRelease :app:compileDebugAndroidTestKotlin`: berhasil. Lint 0 error. APK diverifikasi tanda tangan dan alignment 16 KB; sertifikat sama dengan rilis 1.0.0. Photo Picker baru belum diuji melalui interaksi manual pada perangkat; integrasi native telah dikompilasi dan kontrak upload diuji di API.
- API, migrasi tabel `photos`, kebijakan privasi, serta batas request 4 MB sudah diterapkan di server. Foto disimpan dalam PostgreSQL dan ikut cadangan basis data. Konten lama tanpa foto tetap didukung.

## Arsip pemeriksaan rilis 1.0.0

- Backend FastAPI/PostgreSQL: 8 pengujian integrasi lulus pada database terpisah `rtsmartpro_test`. Mencakup autentikasi, pembatasan login, otorisasi pengurus, isolasi RT, tagihan, verifikasi pembayaran, kas, berita, laporan, dan pencabutan sesi.
- Build lokal `lintRelease assembleRelease bundleRelease`: berhasil. Lint: 0 error, 13 warning. Rilis standar tidak memakai minifikasi.
- Kompilasi source uji Android `:app:compileDebugAndroidTestKotlin`: berhasil. Pengujian instrumentasi otomatis tidak dijalankan ulang; pemeriksaan layar rilis dilakukan melalui UI pada emulator.
- APK rilis: tanda tangan dan zip alignment 16 KB diverifikasi. Library native bawaan memiliki ELF LOAD alignment 16 KB. Package `com.deploydulupulangnanti.rtsmartpro`, versionCode 1, versionName 1.0.0, minSdk 26, targetSdk 36.
- Login melalui API produksi pada APK bertanda tangan berhasil. Navigasi dan screenshot diperiksa pada emulator Android lokal. Ini adalah smoke test melalui UI; pengujian instrumentasi otomatis belum dinyatakan lulus.
- Sebanyak 17 layar native berhasil diambil dan diperiksa secara visual: 5 ponsel serta masing-masing 4 tablet 7 inci, tablet 10 inci, dan jendela layar besar. Semua aset screenshot final berupa PNG 1080×1920, rasio 9:16, di bawah batas 8 MB. Gambar fitur 1024×500 dan ikon 512×512 juga diperiksa.
- Video promosi: 30 detik, 1920×1080, 30 fps, H.264 dengan audio AAC senyap. Deskripsi singkat 65 karakter dan deskripsi lengkap 2267 karakter, sesuai batas listing yang diminta.
- HTTPS health, halaman privasi, dan penghapusan akun dapat diakses. PostgreSQL hanya mendengarkan pada loopback server. Cadangan database berhasil dipulihkan ke database pemeriksaan terpisah.
- Runner Ubuntu ARM64 terdaftar sebagai layanan. Workflow Android dan backend hanya berjalan secara manual; push tidak memicu build.
- Profil Navicat `RT Smart PRO` disiapkan dengan SSH tunnel. Pembukaan koneksi melalui GUI belum terverifikasi karena helper GUI tidak merespons. Password database dan detail akses tersedia hanya di berkas lokal privat.

## Konfigurasi operasional

Data warga dan transaksi awal adalah data contoh sesuai permintaan pemilik. Rekening dan nominal IPL dikelola per RT. Login nomor HP memakai PIN; integrasi OTP Twilio tersedia tetapi belum aktif karena provider belum dikonfigurasi. Video diserahkan sebagai berkas lokal untuk diunggah pemilik ke YouTube. Publikasi, peninjauan, serta harga jual di Play Console dikelola pemilik.

Kunci SSH, keystore, password, PIN, dan data akses tidak disimpan di Git. Simpan cadangan keystore upload di lokasi aman sebelum publikasi pertama.
