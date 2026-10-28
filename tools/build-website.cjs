/* Static, self-hosted marketing pages. No analytics, remote fonts, or build dependency. */
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '..');
const out = path.join(root, 'backend/public');
const write = (file, value) => { const target = path.join(out, file); fs.mkdirSync(path.dirname(target), { recursive: true }); fs.writeFileSync(target, value); };
const escape = value => value.replaceAll('&', '&amp;').replaceAll('"', '&quot;').replaceAll('<', '&lt;').replaceAll('>', '&gt;');
const message = 'Halo RT Smart PRO, saya ingin mendaftarkan lingkungan baru dengan biaya Rp150.000 per bulan per lingkungan, tanpa minimal rumah.\n\nNama lingkungan/RT:\nAlamat lingkungan:\nNama pengurus:\nNomor HP pengurus:\n\nMohon panduan pendaftaran dan aktivasi. Terima kasih.';
const whatsapp = 'https://wa.me/6285814654689?text=' + encodeURIComponent(message);
const contact = 'https://wa.me/6285814654689?text=' + encodeURIComponent('Halo RT Smart PRO, saya ingin bertanya tentang layanan untuk lingkungan saya.');
const arrow = '<span aria-hidden="true">↗</span>';
const nav = [['/', 'Beranda'], ['/fitur/', 'Fitur'], ['/harga/', 'Harga'], ['/pendaftaran/', 'Panduan daftar'], ['/bantuan/', 'Bantuan']];
function links(current) { return nav.map(([href,label]) => `<a href="${href}"${current === href ? ' aria-current="page"' : ''}>${label}</a>`).join(''); }
function shell(current, title, description, body, extra = '') {
  return `<!doctype html>
<html lang="id"><head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>${title} — RT Smart PRO</title><meta name="description" content="${escape(description)}">
<meta name="theme-color" content="#174c3c"><link rel="canonical" href="https://rtsmartpro.amarlo.online${current}">
<meta property="og:type" content="website"><meta property="og:title" content="${escape(title)} — RT Smart PRO"><meta property="og:description" content="${escape(description)}"><meta property="og:image" content="https://rtsmartpro.amarlo.online/assets/social-cover.png">
<link rel="icon" type="image/svg+xml" href="/assets/logo-mark.svg"><link rel="apple-touch-icon" href="/assets/app-icon.png">
<link rel="stylesheet" href="/style.css?v=20261028"><script src="/site.js?v=20261028" defer></script>
<noscript><style>.feature-panel[hidden]{display:grid!important}.step-panel[hidden]{display:block!important}.feature-tabs,.step-tabs,.step-actions{display:none!important}.feature-panel+.feature-panel{margin-top:32px}</style></noscript>
${extra}</head><body>
<a class="skip-link" href="#konten">Langsung ke isi</a>
<header class="site-header"><div class="header-inner">
<a class="brand" href="/" aria-label="RT Smart PRO, beranda"><img src="/assets/logo.svg" alt="RT Smart PRO" width="205" height="48"></a>
<nav class="desktop-nav" aria-label="Navigasi utama">${links(current)}</nav>
<a class="button button-small header-cta" href="/pendaftaran/">Daftar lingkungan ${arrow}</a>
<details class="mobile-menu"><summary>Menu <span aria-hidden="true">☰</span></summary><nav aria-label="Navigasi ponsel">${links(current)}<a class="mobile-register" href="${whatsapp}">Daftar via WhatsApp ${arrow}</a></nav></details>
</div></header>
<main id="konten" class="page ${current === '/' ? 'home-page' : ''}">${body}</main>
<footer class="site-footer"><div class="footer-inner"><div class="footer-brand"><img src="/assets/logo-mark.svg" alt="" width="26" height="26"><span>© 2026 · PT Deploy Dulu Pulang Nanti</span></div><nav aria-label="Informasi legal"><a href="/privacy/">Privasi</a><a href="/delete-account/">Penghapusan akun</a><a href="mailto:seandyadryan2@gmail.com">Email dukungan ${arrow}</a></nav></div></footer>
</body></html>`;
}
const label = text => `<p class="eyebrow"><span class="tiny-square" aria-hidden="true"></span>${text}</p>`;
const phone = (file, alt, cls='') => `<div class="phone ${cls}"><img src="/assets/screens/${file}.png" width="720" height="1600" alt="${alt}" decoding="async"><span class="phone-camera" aria-hidden="true"></span></div>`;

// The mark follows the app icon: a shared roof, open courtyard, and lime sun.
const mark = '<rect width="64" height="64" rx="16" fill="#174c3c"/><path d="M15 29 32 15l17 14-4 4-2-2v17H21V31l-2 2Zm12 3v10h10V32Z" fill="#f7f7f0"/><circle cx="48" cy="16" r="5" fill="#d6e89c"/>';
write('assets/logo-mark.svg', `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 64 64"><title>RT Smart PRO</title>${mark}</svg>`);
write('assets/logo.svg', `<svg xmlns="http://www.w3.org/2000/svg" width="300" height="70" viewBox="0 0 300 70"><title>RT Smart PRO</title><g transform="translate(0 3)">${mark}</g><text x="78" y="36" fill="#174c3c" font-family="Segoe UI,Arial,sans-serif" font-size="29" font-weight="750" letter-spacing="-1">RT Smart</text><rect x="222" y="15" width="53" height="24" rx="4" fill="#e5ebd8"/><text x="231" y="32" fill="#174c3c" font-family="Segoe UI,Arial,sans-serif" font-size="13" font-weight="700" letter-spacing="1">PRO</text><text x="79" y="56" fill="#64746c" font-family="Segoe UI,Arial,sans-serif" font-size="9" font-weight="600" letter-spacing="2.2">IPL &amp; NEWS</text></svg>`);
write('assets/neighborhood.svg', `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 560" fill="none"><g stroke="#c6d2b6" stroke-width="1.2"><path d="M0 100h190q35 0 35 35v290q0 30 35 30h340M0 385h130q30 0 30-30V0M335 0v225q0 30 30 30h235M440 560V370q0-35 35-35h125"/><rect x="24" y="137" width="95" height="60" rx="5"/><rect x="24" y="225" width="95" height="110" rx="5"/><rect x="370" y="40" width="80" height="65" rx="5"/><rect x="370" y="135" width="80" height="65" rx="5"/><rect x="480" y="40" width="90" height="160" rx="5"/><rect x="485" y="388" width="90" height="72" rx="5"/><path d="m20 470 50-35 50 35v60H20ZM500 290h50m-25-25v50"/><circle cx="285" cy="80" r="18"/><circle cx="300" cy="510" r="24"/><circle cx="60" cy="55" r="22"/></g></svg>`);
fs.mkdirSync(path.join(out, 'assets/screens'), { recursive: true });
for (const file of ['home', 'ipl', 'news', 'finance', 'residents']) {
  const target = path.join(out, 'assets/screens', file + '.png');
  const source = path.join(root, 'captures/phone', file + '.png');
  if (fs.existsSync(source)) fs.copyFileSync(source, target);
  if (!fs.existsSync(target)) throw new Error('Missing native screenshot: ' + file);
}
fs.copyFileSync(path.join(root, 'store/icon-512.png'), path.join(out, 'assets/app-icon.png'));
fs.copyFileSync(path.join(root, 'store/feature-1024x500.png'), path.join(out, 'assets/social-cover.png'));

write('index.html', shell('/', 'RT tertata. Warga dekat.', '10.000 RT terdaftar menggunakan RT Smart PRO. Kelola IPL, kas, warga, dan kabar lingkungan. Rp150.000 per bulan tanpa minimal rumah.', `
<section class="home-hero">
<div class="hero-copy">${label('DARI WARGA. UNTUK LINGKUNGAN.')}<h1>RT tertata.<br>Warga <em>dekat.</em></h1>
<p class="lead">Dari iuran bulanan sampai kabar tetangga. Satu aplikasi untuk pengurus dan warga yang ingin urusan lingkungan lebih sederhana.</p>
<div class="actions"><a class="button" href="/pendaftaran/">Daftarkan lingkungan ${arrow}</a><a class="text-link" href="/fitur/">Kenali fiturnya <span aria-hidden="true">→</span></a></div>
<p class="hero-price"><strong>Rp150.000</strong> / bulan per lingkungan<br><span>Tanpa minimal rumah. Mulai dari RT Anda.</span></p>
</div>
<figure class="hero-art"><div class="art-label"><span class="tiny-square" aria-hidden="true"></span>SATU LINGKUNGAN. SATU APLIKASI.</div><div class="phones-stage">${phone('finance','Tampilan asli aplikasi: ringkasan pemasukan dan pengeluaran kas RT.','phone-back')}${phone('home','Tampilan asli beranda RT Smart PRO dengan tagihan IPL dan ringkasan warga.','phone-front')}</div><figcaption>Tampilan aplikasi Android · Data contoh</figcaption><span class="art-index" aria-hidden="true">01 / RT SMART PRO</span></figure>
</section>
<section class="community-strip" aria-label="Komunitas RT Smart PRO"><div class="community-number"><strong>10.000</strong><span>RT terdaftar <br>menggunakan RT Smart PRO</span></div><p>Rukun bertetangga.<br>Rapi mengelola.</p><a href="/harga/">Satu harga.<br>Untuk satu lingkungan. <span aria-hidden="true">↗</span></a></section>
`));

const features = [
  ['ipl','01','IPL','Iuran jelas.\nCatatan beres.','Tagihan bulanan tidak perlu tenggelam di percakapan grup. Warga melihat jumlah, periode, jatuh tempo, dan statusnya sendiri.',['Pengurus membuat tagihan sesuai ketentuan RT.','Warga transfer ke rekening RT dan mengirim konfirmasi.','Pengurus memeriksa mutasi sebelum menyatakan lunas.'],'ipl','Tagihan IPL dan status pembayaran dalam aplikasi RT Smart PRO.'],
  ['kas','02','Kas RT','Kas bersama,\nterlihat bersama.','Pemasukan dari IPL yang disetujui dan pengeluaran lingkungan tercatat dalam satu tempat. Lebih mudah menjelaskan ke mana uang kas digunakan.',['Ringkasan pemasukan, pengeluaran, dan saldo.','Hanya pembayaran terverifikasi yang menjadi pemasukan.','Rekening dan catatan keuangan terpisah untuk setiap RT.'],'finance','Ringkasan kas dan daftar transaksi lingkungan.'],
  ['warga','03','Data warga','Kenal warganya.\nRapi datanya.','Pengurus mengelola data warga di lingkungannya sendiri. Warga tidak dapat mendaftarkan akun atau mengubah daftar warga secara bebas.',['Tambah warga dengan nama, nomor HP, dan alamat.','Edit data atau nonaktifkan warga yang sudah pindah.','Nomor HP dan PIN pribadi digunakan untuk masuk.'],'residents','Menu Data warga yang dikelola oleh pengurus RT.'],
  ['kabar','04','Kabar RT','Kabar RT,\nsampai ke warga.','Pengumuman dan kegiatan punya tempatnya sendiri. Warga bisa membaca informasi resmi pengurus tanpa mencari pesan lama.',['Pengumuman, kegiatan, dan informasi lingkungan.','Foto boleh ditambahkan bila diperlukan.','Kabar hanya tersedia untuk warga lingkungan terkait.'],'news','Daftar kabar RT, pengumuman, dan kegiatan lingkungan.'],
  ['laporan','05','Laporan','Ada yang perlu\ndibereskan?','Sampaikan kebutuhan lingkungan langsung melalui aplikasi. Pelapor dan pengurus dapat melihat laporannya; foto bersifat opsional.',['Tulis judul dan jelaskan masalah lingkungan.','Tambahkan foto jika membantu menjelaskan keadaan.','Pengurus menandai laporan yang telah ditangani.'],null,'']
];
write('fitur/index.html', shell('/fitur/', 'Fitur untuk keseharian RT', 'Kenali fitur IPL, kas, data warga, kabar RT, dan laporan lingkungan dalam aplikasi Android RT Smart PRO.', `
<div class="page-heading">${label('YANG ADA DI DALAM APLIKASI')}<h1>Urusan RT, <em>lebih tertangani.</em></h1></div>
<div class="feature-tabs" role="tablist" aria-label="Fitur aplikasi">${features.map(([id,n,title],i)=>`<button type="button" role="tab" id="tab-${id}" aria-controls="panel-${id}" aria-selected="${i===0}" tabindex="${i===0?0:-1}" data-feature="${id}"><span>${n}</span> ${title}</button>`).join('')}</div>
${features.map(([id,n,title,heading,desc,items,img,alt],i)=>`<section class="feature-panel" id="panel-${id}" role="tabpanel" aria-labelledby="tab-${id}" tabindex="0"${i?' hidden':''}><div class="feature-copy"><span class="feature-number">${n} / ${title.toUpperCase()}</span><h2>${heading.replace('\n','<br>')}</h2><p>${desc}</p><ul class="check-list">${items.map(x=>`<li>${x}</li>`).join('')}</ul><a class="text-link" href="/pendaftaran/">Bawa ke lingkungan Anda ${arrow}</a></div><figure class="feature-art">${img?phone(img,alt):`<div class="report-example"><span class="report-status"><i aria-hidden="true"></i>LAPORAN LINGKUNGAN</span><h3>Lampu jalan<br>perlu diganti.</h3><p>Jelaskan lokasi dan kondisi yang perlu ditangani pengurus.</p><div class="report-photo"><span aria-hidden="true">＋</span>Foto opsional</div><div class="report-progress"><span>Dicatat</span><span aria-hidden="true">→</span><span>Ditangani pengurus</span></div></div>`}<figcaption>${img?'Tampilan aplikasi Android · Data contoh':'Ilustrasi alur laporan lingkungan'}</figcaption></figure></section>`).join('')}
`));

write('harga/index.html', shell('/harga/', 'Harga langganan lingkungan', 'Rp150.000 per bulan per lingkungan. Tanpa minimal rumah. IPL, kas, data warga, kabar RT, dan laporan dalam satu layanan.', `
<div class="pricing-layout"><div class="pricing-copy">${label('SEDERHANA DARI AWAL')}<h1>Lingkungan kecil?<br><em>Tetap bisa.</em></h1><p class="lead">Satu harga untuk satu lingkungan.<br>Tanpa syarat minimal jumlah rumah.</p><div class="pricing-note"><span class="note-index">01 — PENTING DIKETAHUI</span><p>Biaya layanan RT Smart PRO terpisah dari iuran IPL warga. Nominal IPL dan rekening penerima tetap ditentukan oleh pengurus masing-masing RT.</p></div><a class="text-link" href="/bantuan/">Ada yang ingin ditanyakan? ${arrow}</a></div>
<section class="price-sheet" aria-labelledby="price-title"><div class="price-sheet-head"><img src="/assets/logo-mark.svg" alt="" width="42" height="42"><span>LANGGANAN LINGKUNGAN</span></div><h2 id="price-title">RT Smart PRO</h2><p class="large-price">Rp150.000<span>/ bulan</span></p><p class="price-unit">Per lingkungan · Tanpa minimal rumah</p><ul class="check-list"><li>Pencatatan IPL dan konfirmasi pembayaran</li><li>Kas masuk, kas keluar, dan ringkasan saldo</li><li>Pengelolaan data warga oleh pengurus</li><li>Kabar RT dan laporan dengan foto opsional</li><li>Rekening serta data terpisah untuk setiap RT</li></ul><a class="button button-lime" href="${whatsapp}">Daftar melalui WhatsApp ${arrow}</a><p class="fine-print">Konfirmasi langganan dan aktivasi ditangani operator. Perpanjangan serta penghentian layanan dikonfirmasi melalui WhatsApp.</p></section></div>
`));

const steps = [
 ['Siapkan data lingkungan','Nama RT/perumahan dan alamat lingkungan. Siapkan nama pengurus yang bertanggung jawab serta nomor HP untuk login. Tidak ada syarat minimal rumah.','Nama lingkungan / RT','Alamat lingkungan','Nama pengurus','Nomor HP pengurus'],
 ['Kirim lewat WhatsApp','Buka draf pesan yang sudah kami siapkan. Lengkapi data lingkungan dan pengurus, lalu kirim ke nomor resmi pendaftaran.','+6285814654689','Tidak perlu mengirim daftar warga, PIN, atau data rahasia bank pada tahap ini.'],
 ['Konfirmasi dan aktivasi','Operator mengonfirmasi data, biaya Rp150.000 per bulan, periode layanan, serta petunjuk pembayaran. Akses pengurus pertama diberikan secara privat setelah konfirmasi.','Aktivasi oleh operator','Membuka tautan atau mengirim pesan belum otomatis membuat akun.'],
 ['Mulai bersama warga','Masuk dengan nomor HP dan PIN awal, lalu ganti PIN. Atur rekening IPL dan tambahkan warga lingkungan Anda.','Lainnya → Rekening IPL','Lainnya → Data warga']
];
write('pendaftaran/index.html', shell('/pendaftaran/', 'Panduan pendaftaran lingkungan', 'Empat langkah mulai menggunakan RT Smart PRO: siapkan data, kirim WhatsApp, konfirmasi aktivasi, dan daftarkan warga.', `
<div class="page-heading guide-heading">${label('MARI MULAI DARI RT ANDA')}<h1>Empat langkah.<br><em>Satu lingkungan lebih tertata.</em></h1></div>
<div class="guide-layout"><div class="guide-main"><ol class="step-tabs" aria-label="Langkah pendaftaran">${steps.map(([title],i)=>`<li><a href="#langkah-${i+1}" data-step="${i}"${i===0?' aria-current="step"':''}><span>0${i+1}</span>${['Siapkan data','Kirim pesan','Aktivasi','Mulai pakai'][i]}</a></li>`).join('')}</ol>
${steps.map(([title,desc,...extras],i)=>`<section class="step-panel" id="langkah-${i+1}" aria-labelledby="step-title-${i+1}"${i?' hidden':''}><span class="feature-number">LANGKAH 0${i+1} / 04</span><h2 id="step-title-${i+1}">${title}</h2><p>${desc}</p>${i===0?`<div class="data-checklist">${extras.map(x=>`<div><span aria-hidden="true">✓</span>${x}</div>`).join('')}</div>`:`<div class="step-note"><strong>${extras[0]}</strong><p>${extras[1]}</p></div>`}<div class="step-actions">${i>0?`<a class="text-link" href="#langkah-${i}" data-step="${i-1}">← Sebelumnya</a>`:'<span></span>'}${i<3?`<a class="text-link" href="#langkah-${i+2}" data-step="${i+1}">Langkah berikutnya →</a>`:`<a class="text-link" href="${whatsapp}">Daftarkan lingkungan ${arrow}</a>`}</div></section>`).join('')}
<noscript><p>Aktifkan JavaScript untuk membaca panduan per langkah, atau langsung <a href="${whatsapp}">hubungi WhatsApp pendaftaran</a>.</p></noscript></div>
<aside class="guide-aside"><span class="eyebrow">SIAP MENDAFTAR?</span><h2>Rp150.000<span>/ bulan per lingkungan</span></h2><p>Tanpa minimal rumah.<br>Biaya terpisah dari IPL warga.</p><a class="button" href="${whatsapp}">Buka WhatsApp ${arrow}</a><p class="fine-print">Tautan membuka draf pesan. Pesan baru terkirim setelah Anda melengkapi dan mengirimkannya.</p><div class="aside-contact">WhatsApp pendaftaran<strong>+6285814654689</strong></div></aside></div>
`));

const faqs = [
 ['Apakah biaya Rp150.000 dihitung per rumah?','Tidak. Harga langganan adalah Rp150.000 per bulan untuk satu lingkungan, tanpa minimal rumah. Biaya layanan ini terpisah dari iuran IPL warga.'],
 ['Bagaimana cara mendaftarkan lingkungan baru?',`Ikuti <a href="/pendaftaran/">panduan pendaftaran</a>, lalu kirim data lingkungan dan pengurus melalui WhatsApp. Operator mengonfirmasi langganan dan memberikan akses awal secara privat.`],
 ['Saya warga. Bagaimana mendapatkan akun?','Hubungi pengurus RT Anda. Hanya pengurus yang dapat menambahkan warga ke lingkungannya. Setelah didaftarkan, masuk dengan nomor HP dan PIN awal, lalu ganti PIN pribadi.'],
 ['Lupa PIN atau nomor HP sudah berubah?','Hubungi pengurus RT. Pengurus dapat mereset PIN atau mengganti nomor HP melalui Data warga. Jangan membagikan PIN atau kode OTP kepada pihak lain.'],
 ['Apakah pembayaran IPL diproses otomatis?','Tidak. Warga transfer ke rekening RT lalu mengirim konfirmasi di aplikasi. Pengurus memeriksa mutasi rekening sebelum menyetujui. Aplikasi tidak memotong saldo atau membaca akses mobile banking.'],
 ['Bagaimana perpanjangan atau penghentian langganan?',`Hubungi <a href="${contact}">WhatsApp layanan</a> untuk mengonfirmasi periode, perpanjangan, atau penghentian. Ketentuan operasional dikonfirmasi sebelum aktivasi. Permintaan penghapusan data mengikuti <a href="/privacy/">kebijakan privasi</a>.`]
];
write('bantuan/index.html', shell('/bantuan/', 'Bantuan dan pertanyaan', 'Jawaban tentang pendaftaran RT, harga, login warga, PIN, pembayaran IPL, dan perpanjangan layanan RT Smart PRO.', `
<div class="page-heading">${label('KAMI BANTU DARI SINI')}<h1>Ada yang<br><em>ingin ditanyakan?</em></h1></div><div class="help-layout"><div class="faq-list">${faqs.map(([q,a])=>`<details class="faq"><summary>${q}<span class="faq-plus" aria-hidden="true">+</span></summary><p>${a}</p></details>`).join('')}</div><aside class="contact-sheet"><span class="eyebrow">BICARA LANGSUNG</span><h2>Setiap RT punya<br>ceritanya sendiri.</h2><p>Ceritakan kebutuhan lingkungan Anda. Kami bantu jelaskan cara memulainya.</p><a class="button button-lime" href="${contact}">Tanya via WhatsApp ${arrow}</a><a class="contact-email" href="mailto:seandyadryan2@gmail.com">seandyadryan2@gmail.com</a><hr><p class="fine-print">Sudah menjadi warga dan butuh akses akun? Pengurus RT Anda yang dapat menambahkan warga atau mereset PIN.</p></aside></div>
`));

// Preserve the complete existing policy text; only its surrounding layout changes.
for (const [route,title] of [['privacy','Kebijakan privasi'],['delete-account','Penghapusan akun']]) {
 const file = path.join(out, route, 'index.html');
 const source = fs.readFileSync(file,'utf8');
 let content = source.match(/<!-- legal-content:start -->([\s\S]*?)<!-- legal-content:end -->/)?.[1];
 if (!content) content = source.match(/<main>([\s\S]*?)<\/main>/)[1].replace(/<header>[\s\S]*?<\/header>/,'').replace(/<footer>[\s\S]*?<\/footer>/,'').replace(/<h1>[\s\S]*?<\/h1>/,'');
 let n = 0; const headings=[];
 content = content.replace(/<h2(?: id="section-\d+")?>(.*?)<\/h2>/g,(_,text)=>{ const id='section-'+(++n); headings.push([id,text]); return `<h2 id="${id}">${text}</h2>`; });
 write(route+'/index.html',shell('/'+route+'/',title,`${title} RT Smart PRO oleh PT Deploy Dulu Pulang Nanti. Dukungan: seandyadryan2@gmail.com.`, `<div class="page-heading legal-heading">${label('LEGAL & AKUN')}<h1>${title}</h1></div><div class="legal-layout"><aside class="legal-toc"><span class="eyebrow">DI HALAMAN INI</span><nav aria-label="Daftar isi">${headings.map(([id,text])=>`<a href="#${id}">${text}</a>`).join('')}</nav></aside><article class="legal-content"><!-- legal-content:start -->${content}<!-- legal-content:end --></article></div>`));
}
write('robots.txt','User-agent: *\nAllow: /\nSitemap: https://rtsmartpro.amarlo.online/sitemap.xml\n');
write('sitemap.xml',`<?xml version="1.0" encoding="UTF-8"?><urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">${[...nav.map(x=>x[0]),'/privacy/','/delete-account/'].map(x=>`<url><loc>https://rtsmartpro.amarlo.online${x}</loc></url>`).join('')}</urlset>`);
console.log('Generated 7 pages, website logo, local screenshots, and sitemap.');
