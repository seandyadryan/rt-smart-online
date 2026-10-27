# RT Smart PRO IPL & News

Developer: **PT Deploy Dulu Pulang Nanti**

Package: `com.deploydulupulangnanti.rtsmartpro`

Versi rilis: 1.0.2 (3)

Email dukungan: seandyadryan2@gmail.com

## Berkas listing

Isi `deskripsi-singkat.txt` dan `deskripsi-lengkap.txt` sudah berbahasa Indonesia. Gunakan `icon-512.png` sebagai ikon listing dan `feature-1024x500.png` sebagai gambar fitur. Folder `phone` berisi lima gambar dengan bingkai telepon dan penjelasan fitur. Folder `desktop`, `tablet-10`, dan `tablet-7` masing-masing berisi empat gambar. Seluruh screenshot promosi berukuran 1080×1920, rasio 9:16, format PNG, dan diperiksa lewat `asset-manifest.json`. Aset desktop memperlihatkan jendela Android berorientasi potret dengan layout adaptif layar besar.

Tampilan di dalam bingkai berasal dari aplikasi Android yang dijalankan. Data contoh ditandai pada aset. Ganti screenshot bila fitur atau tampilan aplikasi berubah. Referensi screenshot pengguna tidak terlampir pada percakapan, sehingga tata letak promosi dibuat dari desain aplikasi sendiri.

Video `RT-Smart-PRO-promo-id.mp4`: 1920×1080, H.264/AAC, 30 detik, teks Indonesia, tanpa musik berhak cipta dan tanpa suara narasi. Unggah ke YouTube sebagai publik atau tidak publik, nonaktifkan iklan dan pembatasan usia, lalu masukkan URL-nya di Play Console. Video hanya dibuat lokal; belum diunggah ke YouTube.

## URL publik

- Privasi: https://rtsmartpro.amarlo.online/privacy/
- Penghapusan akun: https://rtsmartpro.amarlo.online/delete-account/
- Dukungan: seandyadryan2@gmail.com

Sumber HTML kebijakan berada di `backend/public/privacy/index.html`.

## Pengisian Data safety dan akses peninjau

Tinjau pilihan Play Console berdasarkan implementasi dan operasional saat rilis. Aplikasi mengirim nama, nomor HP, alamat rumah, ID akun, catatan IPL dan konfirmasi transfer, laporan warga, serta log aktivitas ke server untuk fungsi akun dan administrasi RT. Foto opsional pada kabar RT dan laporan dikirim ke server hanya ketika pengguna memilih serta mengirimnya. Deklarasikan foto sebagai data yang dikumpulkan untuk fungsi aplikasi, bersifat opsional. Photo Picker memberikan akses hanya ke foto yang dipilih dan aplikasi tidak meminta akses galeri secara menyeluruh. Tidak ada SDK iklan atau analytics pihak ketiga. Aplikasi tidak meminta lokasi, kontak, kamera, mikrofon, akses SMS, atau advertising ID. Koneksi data menggunakan HTTPS. Permintaan penghapusan tersedia lewat aplikasi dan situs publik.

Jika Twilio Verify diaktifkan, nomor HP diproses penyedia OTP. Perbarui pernyataan Data safety sesuai konfigurasi akhirnya. Kolom bebas laporan dan referensi pembayaran dapat berisi informasi yang diketik pengguna; masukkan jenis data yang relevan pada deklarasi Anda.

Buat akun peninjau terpisah sebelum pengajuan. Berikan nomor HP dan PIN akun tersebut pada bagian App access secara privat, beserta langkah login. Jangan gunakan akun pengurus utama. Review Google Play, pengaturan harga aplikasi berbayar, negara distribusi, rating konten, deklarasi target audiens, detail badan usaha, dan persetujuan publikasi tetap dilakukan pemilik Play Console.

Aplikasi tidak menjalankan pembelian digital dalam aplikasi. IPL dibayarkan melalui transfer bank di luar aplikasi dan diverifikasi pengurus. Harga jual aplikasi di Play Store ditetapkan melalui Play Console.

## Rilis

Unggah berkas `.aab` bertanda tangan dari folder `release` lokal. Keystore upload ada di `D:\KEYSTORE\rtsmartpro.jks`, alias `rtsmartpro`. Password dan keystore tidak disimpan di repository. Aktifkan Play App Signing dan simpan cadangan keystore secara aman.

Workflow GitHub hanya `workflow_dispatch`, tidak berjalan otomatis saat push. Pilih Actions → Android • signed release manual untuk build berikutnya; Backend • deploy manual untuk deployment berikutnya. Workflow Android menggunakan GitHub hosted runner. Deployment memakai runner ARM64 di server Ubuntu.

Referensi resmi: [aset listing](https://support.google.com/googleplay/android-developer/answer/9866151), [penghapusan akun](https://support.google.com/googleplay/android-developer/answer/13327111).
