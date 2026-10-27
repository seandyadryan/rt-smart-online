# Pemeriksaan rilis 1.0.0

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
