import sharp from 'sharp';
import { readFile, writeFile, mkdir, stat } from 'node:fs/promises';
import { spawnSync } from 'node:child_process';
import ffmpeg from 'ffmpeg-static';
import path from 'node:path';
const root=path.resolve(import.meta.dirname,'..');
const out=path.join(root,'store');
const ink='#213b32', pine='#174c3c', cream='#f7f7f0', lime='#d6e89c';
const esc=s=>s.replaceAll('&','&amp;').replaceAll('<','&lt;');
const text=(s,x,y,size,color=ink,family='Segoe UI',weight=400)=>`<text x="${x}" y="${y}" font-family="${family}" font-size="${size}" fill="${color}" font-weight="${weight}">${esc(s)}</text>`;
const svg=(w,h,body)=>Buffer.from(`<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}" viewBox="0 0 ${w} ${h}">${body}</svg>`);
const logo=(x,y,s)=>`<g transform="translate(${x} ${y}) scale(${s/108})"><rect width="108" height="108" rx="25" fill="${pine}"/><path d="M26 49L54 25L82 49L76 56L73 53V79H35V53L32 56ZM45 53V69H63V53Z" fill="${cream}"/><circle cx="79" cy="27" r="9" fill="${lime}"/></g>`;
const features=[
 {file:'home',tag:'01 / BERANDA',title:['Lingkungan rukun,','hidup lebih ringan.'],desc:'Tagihan dan kabar RT, dekat dalam genggaman.'},
 {file:'ipl',tag:'02 / IURAN LINGKUNGAN',title:['Iuran tertib.','Lingkungan terawat.'],desc:'Lihat tagihan dan kirim konfirmasi transfer.'},
 {file:'news',tag:'03 / KABAR RT',title:['Kabar baik dimulai','dari tetangga.'],desc:'Pengumuman dan kegiatan resmi dari pengurus.'},
 {file:'finance',tag:'04 / TRANSPARANSI KAS',title:['Kas terbuka.','Warga percaya.'],desc:'Pantau pemasukan dan pengeluaran bersama.'},
 {file:'residents',tag:'05 / RUANG PENGURUS',title:['Kenal warganya.','Terhubung semuanya.'],desc:'Pendaftaran warga dikelola oleh pengurus RT.'}
];
async function embed(file,x,y,w,h,r=0){
 const image=await sharp(file).resize(w,h,{fit:'contain',background:cream}).png().toBuffer();
 return `<defs><clipPath id="screen"><rect x="${x}" y="${y}" width="${w}" height="${h}" rx="${r}"/></clipPath></defs><image x="${x}" y="${y}" width="${w}" height="${h}" clip-path="url(#screen)" href="data:image/png;base64,${image.toString('base64')}"/>`;
}
await mkdir(out,{recursive:true});
await sharp(svg(512,512,`<rect width="512" height="512" fill="${pine}"/>${logo(0,0,512)}`)).flatten().png().toFile(path.join(out,'icon-512.png'));
await writeFile(path.join(out,'icon-source.svg'),svg(512,512,logo(0,0,512)));
if(process.argv.includes('--branding-only')) { console.log('Created app icon.'); process.exit(0); }
for (const device of ['phone','tablet-7','tablet-10','desktop']) {
 for (const f of features.slice(0,device==='phone'?5:4)) {
  const source=path.join(root,'captures',device,f.file+'.png');
  const stats=await sharp(source).resize(80,80).stats();
  if(stats.channels.slice(0,3).every(c=>c.mean<12)) throw new Error(`Blank native screenshot rejected: ${device}/${f.file}`);
 }
}
for (let idx=0;idx<features.length;idx++) {
 const f=features[idx];
 await mkdir(path.join(out,'phone'),{recursive:true});
 let body=`<rect width="1080" height="1920" fill="${cream}"/><path d="M0 1270Q540 1000 1080 1320V1920H0Z" fill="#e7eddd"/>`;
 body+=logo(68,56,52)+text('RT Smart PRO',137,91,27,pine,'Segoe UI',650)+text(f.tag,70,176,19,'#687970','Segoe UI',600);
 body+=text(f.title[0],67,257,61,ink,'Georgia')+text(f.title[1],67,331,61,ink,'Georgia')+text(f.desc,70,390,23,'#687970');
 body+=`<rect x="211" y="449" width="658" height="1412" rx="65" fill="#cbd2c4"/><rect x="221" y="441" width="638" height="1405" rx="60" fill="#20372e"/><rect x="230" y="450" width="620" height="1387" rx="51" fill="#0c1b14"/>`;
 body+=await embed(path.join(root,'captures','phone',f.file+'.png'),242,462,596,1363,43);
 body+=`<rect x="472" y="472" width="136" height="19" rx="9" fill="#213b32"/>`;
 body+=text('Tampilan aplikasi • Data contoh',70,1890,16,'#687970');
 await sharp(svg(1080,1920,body)).flatten().png().toFile(path.join(out,'phone',`${idx+1}-${f.file}.png`));
}
for (const device of ['tablet-7','tablet-10','desktop']) {
 await mkdir(path.join(out,device),{recursive:true});
 for (let idx=0;idx<4;idx++) {
  const f=features[idx],w=1080,h=1920;
  let body=`<rect width="${w}" height="${h}" fill="${cream}"/>`;
  body+=logo(60,45,54)+text('RT Smart PRO',133,82,28,pine,'Segoe UI',650);
   body+=text(f.title[0],60,225,62,ink,'Georgia')+text(f.title[1],60,300,62,ink,'Georgia')+text(f.desc,60,359,24,'#687970');
   body+=`<rect x="67" y="450" width="946" height="1376" rx="45" fill="#213b32"/>`;
   body+=await embed(path.join(root,'captures',device,f.file+'.png'),89,472,902,1332,25);
   body+=text(`Tampilan ${device==='desktop'?'jendela Android di layar besar':device==='tablet-7'?'tablet 7 inci':'tablet 10 inci'} • Data contoh`,60,1882,17,'#687970');
  await sharp(svg(w,h,body)).flatten().png().toFile(path.join(out,device,`${idx+1}-${f.file}.png`));
 }
}
let feature=`<rect width="1024" height="500" fill="${pine}"/><circle cx="970" cy="450" r="300" fill="#245943"/>`;
feature+=logo(56,45,58)+text('RT Smart PRO',131,83,28,cream,'Segoe UI',650)+text('IPL & NEWS',58,154,13,lime,'Segoe UI',600);
feature+=text('Tetangga dekat.',54,225,52,cream,'Georgia')+text('Urusan beres.',54,288,52,cream,'Georgia')+text('Iuran • Kabar RT • Kas lingkungan',58,348,20,lime);
feature+=text('Tampilan aplikasi menggunakan data contoh',58,459,12,lime);
feature+=`<rect x="676" y="38" width="244" height="530" rx="28" fill="#0e291d"/>`+await embed(path.join(root,'captures','phone','home.png'),685,47,226,510,21);
await sharp(svg(1024,500,feature)).flatten().png().toFile(path.join(out,'feature-1024x500.png'));
await mkdir(path.join(out,'video-slides'),{recursive:true});
for(let idx=0;idx<5;idx++) {
 const f=features[idx];
 let b=`<rect width="1920" height="1080" fill="${idx%2===0?pine:cream}"/>`;
 const color=idx%2===0?cream:ink,muted=idx%2===0?lime:'#687970';
 b+=logo(100,70,75)+text('RT Smart PRO',198,119,36,color,'Segoe UI',650)+text(f.tag,103,287,23,muted,'Segoe UI',650);
 b+=text(f.title[0],99,400,79,color,'Georgia')+text(f.title[1],99,502,79,color,'Georgia')+text(f.desc,104,599,29,muted);
 b+=text('IPL & NEWS  /  PT Deploy Dulu Pulang Nanti',105,961,21,muted);
 b+=`<rect x="1240" y="54" width="422" height="970" rx="45" fill="#102a1e"/>`+await embed(path.join(root,'captures','phone',f.file+'.png'),1252,66,398,946,33);
 b+=text('Tampilan aplikasi • Data contoh',1250,1056,16,muted);
 await sharp(svg(1920,1080,b)).flatten().png().toFile(path.join(out,'video-slides',`${idx+1}.png`));
}
const video=path.join(out,'RT-Smart-PRO-promo-id.mp4');
const result=spawnSync(ffmpeg,['-y','-framerate','1/6','-i',path.join(out,'video-slides','%d.png'),'-f','lavfi','-i','anullsrc=channel_layout=stereo:sample_rate=48000','-vf','fps=30,format=yuv420p','-c:v','libx264','-preset','medium','-crf','20','-c:a','aac','-shortest','-movflags','+faststart',video],{encoding:'utf8',maxBuffer:10*1024*1024});
if(result.status!==0) throw new Error(result.stderr);
const manifest={generatedAt:new Date().toISOString(),source:'Actual native Android screenshots captured from the running app; sample residents are labelled.',assets:[]};
for(const dir of ['phone','tablet-7','tablet-10','desktop'])for(let i=0;i<(dir==='phone'?5:4);i++){
 const file=`${dir}/${i+1}-${features[i].file}.png`,p=path.join(out,file),meta=await sharp(p).metadata(),s=await stat(p);
 if(s.size>8*1024*1024 || ![9/16,16/9].some(r=>Math.abs(meta.width/meta.height-r)<.001))throw new Error('Invalid screenshot '+file);
 manifest.assets.push({file,width:meta.width,height:meta.height,bytes:s.size});
}
await writeFile(path.join(out,'asset-manifest.json'),JSON.stringify(manifest,null,2));
console.log('Created icon, feature graphic, 17 screenshots and 30-second MP4.');
