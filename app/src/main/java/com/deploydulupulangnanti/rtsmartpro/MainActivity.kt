package com.deploydulupulangnanti.rtsmartpro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.json.JSONObject
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Pine = Color(0xFF174C3C)
private val Cream = Color(0xFFF7F7F0)
private val Ink = Color(0xFF213B32)
private val Muted = Color(0xFF687970)
private val Lime = Color(0xFFD6E89C)
private val Line = Color(0xFFE2E6DB)
private val Pale = Color(0xFFEEF1E7)
fun rupiah(value: Long) = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply { maximumFractionDigits = 0 }.format(value)
private fun money(o: JSONObject, key: String) = rupiah(o.optLong(key))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fallbackBar = android.graphics.Color.rgb(23,76,60)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT,fallbackBar), navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT,fallbackBar))
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Pine, onPrimary = Color.White, primaryContainer = Lime, onPrimaryContainer = Pine, secondary = Pine, onSecondary = Color.White, secondaryContainer = Lime, onSecondaryContainer = Pine, tertiary = Pine, onTertiary = Color.White, tertiaryContainer = Pale, onTertiaryContainer = Ink, background = Cream, surface = Cream, surfaceVariant = Pale, onBackground = Ink, onSurface = Ink, onSurfaceVariant = Muted, outline = Muted, outlineVariant = Line, surfaceTint = Pine)) { RTApp() } }
    }
}

@Composable
private fun Brand(compact: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(if (compact) 36.dp else 48.dp).clip(RoundedCornerShape(13.dp)).background(Pine), contentAlignment = Alignment.Center) {
            Icon(AppIcons.HolidayVillage, "RT Smart PRO", tint = Lime, modifier = Modifier.size(if (compact) 24.dp else 31.dp))
        }
        Column {
            Text("RT Smart", fontSize = if (compact) 19.sp else 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.7).sp)
            Text("PRO  /  IPL & NEWS", fontSize = 9.sp, color = Muted, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
    }
}

private data class Destination(val title: String, val icon: ImageVector)
private val destinations = listOf(Destination("Beranda", AppIcons.Home), Destination("IPL", AppIcons.ReceiptLong), Destination("Kabar RT", AppIcons.Newspaper), Destination("Kas", AppIcons.AccountBalanceWallet), Destination("Lainnya", AppIcons.GridView))
private data class Field(val key: String, val label: String, val value: String = "", val numeric: Boolean = false, val multiline: Boolean = false, val required: Boolean = true)
private data class Form(val title: String, val subtitle: String, val fields: List<Field>, val button: String = "Simpan", val photo: Boolean = false, val submit: (Map<String,String>, () -> Unit) -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RTApp(vm: AppModel = viewModel()) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    var sub by rememberSaveable { mutableStateOf("") }
    var form by remember { mutableStateOf<Form?>(null) }
    var article by remember { mutableStateOf<JSONObject?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(vm.message) { vm.message?.let { snackbar.showSnackbar(it); vm.message = null } }
    val navigate: (Int) -> Unit = { page = it; sub = "" }
    Scaffold(containerColor = Cream, snackbarHost = { SnackbarHost(snackbar) }, bottomBar = {
        if (vm.me != null) NavigationBar(containerColor = Cream, tonalElevation = 0.dp) {
            destinations.forEachIndexed { i,d -> NavigationBarItem(selected = page == i, onClick = { navigate(i) }, icon = { Icon(d.icon,d.title) }, label = { Text(d.title, fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(indicatorColor = Lime, selectedIconColor = Pine)) }
        }
    }) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            if (vm.loading) Box(Modifier.fillMaxSize(),contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else if (vm.me == null) LoginScreen(vm)
            else {
                val wide = maxWidth >= 840.dp
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                    if (wide) Column(Modifier.width(250.dp).fillMaxHeight().background(Pale).padding(24.dp),verticalArrangement = Arrangement.spacedBy(22.dp)) {
                        Brand(); Spacer(Modifier.height(24.dp)); Headline("Tetangga dekat.\nUrusan beres.",32)
                        Text("Satu tempat untuk iuran, kabar, dan kebutuhan lingkungan Anda.", color = Muted, lineHeight = 24.sp)
                        Spacer(Modifier.weight(1f)); Text(vm.me?.getJSONObject("community")?.s("name") ?: "",fontWeight = FontWeight.Bold)
                        Text("Terhubung dengan RT Anda",color = Muted,fontSize = 12.sp)
                    }
                    Column(Modifier.widthIn(max = if (wide) 800.dp else 680.dp).weight(1f,fill = false).fillMaxHeight()) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp,vertical = 13.dp),verticalAlignment = Alignment.CenterVertically) {
                            Brand(true); Spacer(Modifier.weight(1f))
                            IconButton(onClick = { vm.refresh() },enabled = !vm.busy) { Icon(AppIcons.Refresh,"Muat ulang") }
                            Box(Modifier.size(38.dp).clip(CircleShape).background(Lime),contentAlignment = Alignment.Center) { Text(vm.me!!.s("name").take(1),fontWeight = FontWeight.Bold) }
                        }
                        if (vm.busy) LinearProgressIndicator(Modifier.fillMaxWidth(),color = Pine,trackColor = Pale)
                        when (page) {
                            0 -> Home(vm,navigate,{ form = invoiceForm(vm) },{ article = it })
                            1 -> Invoices(vm,{ form = it },{ form = invoiceForm(vm) })
                            2 -> NewsScreen(vm,{ article = it },{ form = newsForm(vm) })
                            3 -> FinanceScreen(vm,{ form = expenseForm(vm) })
                            4 -> when (sub) {
                                "warga" -> Residents(vm,{ sub = "" },{ form = it })
                                "laporan" -> Reports(vm,{ sub = "" },{ form = reportForm(vm) })
                                else -> More(vm,{ sub = it },{ form = it },{ url -> runCatching { context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url))) }.onFailure { vm.message = "Aplikasi untuk membuka tautan belum tersedia. Hubungi seandyadryan2@gmail.com." } })
                            }
                        }
                    }
                }
            }
        }
    }
    form?.let { f -> FormDialog(f,vm,{ form = null }) }
    article?.let { n -> AlertDialog(onDismissRequest = { article = null }, title = { Text(n.s("title"),fontFamily = FontFamily.Serif,fontSize = 26.sp) }, text = { Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement = Arrangement.spacedBy(15.dp)) { Badge(n.s("category")); PostPhoto(vm,n.s("photo_id"),"Foto kabar RT"); Text(n.s("body"),lineHeight = 24.sp); Text("Diterbitkan oleh ${n.s("author")}",color = Muted,fontSize = 12.sp) } },confirmButton = { TextButton(onClick = { article = null }) { Text("Tutup") } },dismissButton = { if (vm.isAdmin) TextButton(onClick = { vm.mutate("news/${n.s("id")}",method = "DELETE"); article = null }) { Text("Hapus berita") } }) }
    vm.notice?.let { text -> AlertDialog(onDismissRequest = { vm.notice = null },title = { Text("RT Smart PRO") },text = { Text(text,lineHeight = 24.sp) },confirmButton = { TextButton(onClick = { vm.notice = null }) { Text("Mengerti") } }) }
}

@Composable
private fun LoginScreen(vm: AppModel) {
    var phone by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf(false) }
    var sent by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp,vertical = 24.dp),horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.widthIn(max = 470.dp),verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Brand(); Spacer(Modifier.height(22.dp))
            Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(24.dp)).background(Pine)) {
                Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 26.dp),verticalAlignment = Alignment.Bottom,horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(AppIcons.Park,null,tint = Lime,modifier = Modifier.size(72.dp))
                    Icon(AppIcons.HolidayVillage,null,tint = Cream,modifier = Modifier.size(112.dp))
                    Icon(AppIcons.Park,null,tint = Lime,modifier = Modifier.size(56.dp))
                }
                Text("RUKUN ITU DEKAT",Modifier.padding(20.dp),color = Lime,fontSize = 10.sp,letterSpacing = 2.sp)
            }
            Headline("Lingkungan rukun,\nhidup lebih ringan.",34)
            Text("Masuk untuk melihat iuran dan kabar dari RT Anda. Nomor HP didaftarkan oleh pengurus.",color = Muted,lineHeight = 22.sp)
            OutlinedTextField(phone,{ phone = it },label = { Text("Nomor HP") },placeholder = { Text("08xxxxxxxxxx") },singleLine = true,keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone,imeAction = ImeAction.Next),modifier = Modifier.fillMaxWidth(),shape = RoundedCornerShape(14.dp))
            if (!otp || sent) OutlinedTextField(code,{ code = it.filter(Char::isDigit).take(6) },label = { Text(if (otp) "Kode OTP" else "PIN 6 digit") },singleLine = true,visualTransformation = PasswordVisualTransformation(),keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),modifier = Modifier.fillMaxWidth(),shape = RoundedCornerShape(14.dp))
            Button(onClick = { if (otp && !sent) vm.requestOtp(phone) { sent = true } else vm.login(phone,code,otp) },enabled = !vm.busy && phone.length >= 10 && (otp && !sent || code.length == 6),modifier = Modifier.fillMaxWidth().height(54.dp),shape = RoundedCornerShape(14.dp)) { if (vm.busy) CircularProgressIndicator(Modifier.size(20.dp),color = Color.White,strokeWidth = 2.dp) else Text(if (otp && !sent) "Kirim kode OTP" else "Masuk ke lingkungan Anda",fontWeight = FontWeight.SemiBold) }
            if (vm.otpEnabled) TextButton(onClick = { otp = !otp; sent = false; code = "" },modifier = Modifier.fillMaxWidth()) { Text(if (otp) "Gunakan PIN pengurus" else "Masuk dengan OTP SMS") }
            else Text("Belum punya PIN atau lupa PIN? Hubungi pengurus RT Anda.",color = Muted,fontSize = 12.sp,lineHeight = 18.sp)
            EnvironmentRegistration { url ->
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                    .onFailure { vm.message = "Tautan belum dapat dibuka. Hubungi WhatsApp +6285814654689 atau kunjungi rtsmartpro.amarlo.online/pendaftaran/." }
            }
            TextButton(onClick = {
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://rtsmartpro.amarlo.online/privacy/"))) }
                    .onFailure { vm.message = "Tautan belum dapat dibuka. Kunjungi rtsmartpro.amarlo.online/privacy/ melalui browser." }
            }) { Text("Kebijakan privasi",fontSize = 12.sp) }
            Text("PT Deploy Dulu Pulang Nanti",fontSize = 11.sp,color = Muted)
        }
    }
}

@Composable private fun Headline(text: String,size: Int = 30) { Text(text,fontFamily = FontFamily.Serif,fontSize = size.sp,lineHeight = (size + 5).sp,color = Ink,letterSpacing = (-0.6).sp) }
@Composable private fun Eyebrow(text: String) { Text(text.uppercase(Locale.forLanguageTag("id-ID")),fontSize = 10.sp,color = Muted,letterSpacing = 1.8.sp,fontWeight = FontWeight.SemiBold) }
@Composable private fun Badge(text: String, warm: Boolean = false) { Text(text,Modifier.clip(RoundedCornerShape(7.dp)).background(if (warm) Color(0xFFF2E5CD) else Pale).padding(horizontal = 9.dp,vertical = 5.dp),color = if (warm) Color(0xFF765321) else Pine,fontSize = 10.sp,fontWeight = FontWeight.Medium) }
@Composable private fun Section(title: String,action: String? = null,onClick: () -> Unit = {}) { Row(Modifier.fillMaxWidth(),verticalAlignment = Alignment.CenterVertically) { Text(title,fontSize = 18.sp,fontWeight = FontWeight.SemiBold,modifier = Modifier.weight(1f)); if (action != null) TextButton(onClick = onClick) { Text(action,fontSize = 12.sp) } } }
@Composable private fun Empty(icon: ImageVector,title: String,body: String) { Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Pale).padding(26.dp),horizontalAlignment = Alignment.CenterHorizontally,verticalArrangement = Arrangement.spacedBy(10.dp)) { Icon(icon,null,tint = Muted,modifier = Modifier.size(32.dp)); Text(title,fontWeight = FontWeight.SemiBold); Text(body,color = Muted,fontSize = 13.sp,lineHeight = 20.sp) } }
@Composable private fun Paper(modifier: Modifier = Modifier,content: @Composable ColumnScope.() -> Unit) { Column(modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).border(1.dp,Line,RoundedCornerShape(18.dp)).padding(18.dp),verticalArrangement = Arrangement.spacedBy(12.dp),content = content) }

@Composable private fun Home(vm: AppModel,navigate: (Int) -> Unit,createInvoice: () -> Unit,openNews: (JSONObject) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(),contentPadding = PaddingValues(22.dp),verticalArrangement = Arrangement.spacedBy(22.dp)) {
        item { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Eyebrow(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy",Locale.forLanguageTag("id-ID")))); Headline("Halo, ${vm.me!!.s("name").substringBefore(" ")}.\nApa kabar hari ini?",31); Text(vm.me!!.getJSONObject("community").s("name"),color = Muted,fontSize = 12.sp); if (vm.isAdmin) Badge("PENGURUS RT") } }
        item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp)).background(Pine).padding(23.dp),verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(AppIcons.ReceiptLong,null,tint = Lime,modifier = Modifier.size(21.dp)); Spacer(Modifier.width(9.dp)); Text("Iuran lingkungan Anda",color = Cream,fontSize = 13.sp); Spacer(Modifier.weight(1f)); Text("IPL",color = Lime,fontSize = 11.sp,fontWeight = FontWeight.Bold) }
                Text(money(vm.dashboard,"unpaid_amount"),color = Color.White,fontSize = 35.sp,fontWeight = FontWeight.SemiBold,letterSpacing = (-1).sp)
                Text(if (vm.dashboard.optInt("unpaid_count") > 0) "${vm.dashboard.optInt("unpaid_count")} tagihan menunggu pembayaran" else "Tidak ada tagihan yang belum dibayar",color = Color(0xFFCCDCD2),fontSize = 12.sp)
                Button(onClick = { navigate(1) },colors = ButtonDefaults.buttonColors(containerColor = Lime,contentColor = Pine),shape = RoundedCornerShape(11.dp),modifier = Modifier.fillMaxWidth().height(47.dp)) { Text("Lihat tagihan IPL",fontWeight = FontWeight.SemiBold); Spacer(Modifier.weight(1f)); Icon(AppIcons.ArrowForward,null,modifier = Modifier.size(19.dp)) }
            }
        }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Stat("Warga terdaftar",vm.dashboard.optInt("resident_count").toString(),AppIcons.People,Modifier.weight(1f)); Stat(if (vm.isAdmin) "Perlu diperiksa" else "Menunggu verifikasi",vm.dashboard.optInt(if (vm.isAdmin) "pending_review" else "pending_count").toString(),AppIcons.TaskAlt,Modifier.weight(1f)) } }
        item { Section("Dekat dengan lingkungan") }
        item { Row(Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) { Quick("Kabar RT",AppIcons.Newspaper) { navigate(2) }; Quick("Kas RT",AppIcons.AccountBalanceWallet) { navigate(3) }; Quick("Laporan",AppIcons.Campaign) { navigate(4) }; Quick(if (vm.isAdmin) "Buat IPL" else "Akun",if (vm.isAdmin) AppIcons.AddCard else AppIcons.Person) { if (vm.isAdmin) createInvoice() else navigate(4) } } }
        item { Section("Kabar dari RT","Lihat semua") { navigate(2) } }
        if (vm.news.isEmpty()) item { Empty(AppIcons.Newspaper,"Belum ada kabar","Pengumuman dan kegiatan RT akan muncul di sini.") }
        items(vm.news.take(2),key = { it.s("id") }) { n -> NewsCard(vm,n) { openNews(n) } }
        item { Text("Dari warga, untuk lingkungan yang lebih baik.",color = Muted,fontSize = 11.sp,modifier = Modifier.padding(bottom = 10.dp)) }
    }
}
@Composable private fun Stat(title: String,value: String,icon: ImageVector,modifier: Modifier) { Paper(modifier) { Icon(icon,null,tint = Pine,modifier = Modifier.size(22.dp)); Text(value,fontSize = 28.sp,fontWeight = FontWeight.SemiBold); Text(title,fontSize = 11.sp,color = Muted) } }
@Composable private fun Quick(title: String,icon: ImageVector,onClick: () -> Unit) { Column(Modifier.widthIn(min = 62.dp).clickable(onClick = onClick).padding(vertical = 4.dp),horizontalAlignment = Alignment.CenterHorizontally,verticalArrangement = Arrangement.spacedBy(9.dp)) { Box(Modifier.size(55.dp).clip(RoundedCornerShape(16.dp)).background(Pale),contentAlignment = Alignment.Center) { Icon(icon,title,tint = Pine) }; Text(title,fontSize = 11.sp,color = Ink) } }

private fun invoiceForm(vm: AppModel) = Form("Terbitkan tagihan IPL","Tagihan diterbitkan untuk seluruh warga aktif. Periode yang sama tidak digandakan.",listOf(Field("title","Nama tagihan","IPL bulanan"),Field("period","Periode (YYYY-MM)",LocalDate.now().toString().take(7)),Field("amount","Nominal rupiah",numeric = true),Field("due_date","Jatuh tempo (YYYY-MM-DD)",LocalDate.now().plusDays(7).toString())),"Terbitkan tagihan") { v,done -> vm.mutate("invoices",JSONObject(v).put("amount",v["amount"]?.toLongOrNull() ?: 0),after = done) }
private fun newsForm(vm: AppModel) = Form("Tulis kabar RT","Bagikan informasi resmi untuk warga lingkungan Anda.",listOf(Field("title","Judul berita"),Field("category","Kategori: Pengumuman / Kegiatan / Informasi","Pengumuman"),Field("body","Isi berita",multiline = true)),"Terbitkan",photo = true) { v,done -> vm.mutate("news",JSONObject(v),after = done) }
private fun expenseForm(vm: AppModel) = Form("Catat pengeluaran","Pengeluaran akan tampil pada laporan kas yang dapat dilihat warga.",listOf(Field("title","Keperluan pengeluaran"),Field("amount","Nominal rupiah",numeric = true),Field("date","Tanggal (YYYY-MM-DD)",LocalDate.now().toString()))) { v,done -> vm.mutate("expenses",JSONObject(v).put("amount",v["amount"]?.toLongOrNull() ?: 0),after = done) }
private fun reportForm(vm: AppModel) = Form("Sampaikan laporan","Laporkan kebutuhan atau masalah lingkungan kepada pengurus RT.",listOf(Field("title","Judul laporan"),Field("body","Ceritakan lokasi dan kejadiannya",multiline = true)),"Kirim laporan",photo = true) { v,done -> vm.mutate("reports",JSONObject(v),after = done) }

@Composable private fun Invoices(vm: AppModel,show: (Form) -> Unit,create: () -> Unit) {
    var filter by rememberSaveable { mutableStateOf("Semua") }
    val filters = listOf("Semua","Belum bayar","Verifikasi","Lunas")
    val status = mapOf("Belum bayar" to "unpaid","Verifikasi" to "pending","Lunas" to "paid")
    val rows = vm.invoices.filter { filter == "Semua" || it.s("status") == status[filter] }
    val clipboard = LocalClipboardManager.current
    LazyColumn(Modifier.fillMaxSize(),contentPadding = PaddingValues(22.dp),verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Column(verticalArrangement = Arrangement.spacedBy(9.dp)) { Eyebrow("Iuran Pengelolaan Lingkungan"); Headline("Iuran tertib.\nLingkungan terawat."); Text(if (vm.isAdmin) "Kelola tagihan dan periksa konfirmasi warga." else "Pantau tagihan dan riwayat pembayaran Anda.",fontSize = 13.sp,color = Muted) } }
        item { val bank = vm.me!!.getJSONObject("community"); Paper { Row(verticalAlignment = Alignment.CenterVertically) { Icon(AppIcons.AccountBalance,null,tint = Pine); Spacer(Modifier.width(10.dp)); Text("Rekening IPL",fontWeight = FontWeight.SemiBold) }; if (bank.s("account").isBlank()) Text("Rekening belum diatur oleh pengurus RT.",fontSize = 12.sp,color = Muted) else { Text("${bank.s("bank")} • ${bank.s("account")}",fontWeight = FontWeight.Bold); Text("a.n. ${bank.s("holder")}",fontSize = 12.sp,color = Muted); TextButton(onClick = { clipboard.setText(AnnotatedString(bank.s("account"))); vm.notice = "Nomor rekening disalin." }) { Text("Salin nomor rekening") } } } }
        if (vm.isAdmin) item { FilledTonalButton(onClick = create,modifier = Modifier.fillMaxWidth()) { Icon(AppIcons.Add,null); Text("Terbitkan tagihan",Modifier.padding(start = 7.dp)) } }
        item { Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement = Arrangement.spacedBy(8.dp)) { filters.forEach { label -> FilterChip(selected = filter == label,onClick = { filter = label },label = { Text(label,fontSize = 11.sp) }) } } }
        if (rows.isEmpty()) item { Empty(AppIcons.ReceiptLong,"Belum ada tagihan","Tagihan pada kategori ini akan tampil setelah diterbitkan pengurus.") }
        items(rows,key = { it.s("id") }) { i -> Paper {
            Row(verticalAlignment = Alignment.CenterVertically) { Badge(when(i.s("status")) { "paid" -> "Lunas"; "pending" -> "Menunggu verifikasi"; else -> "Belum bayar" },i.s("status") != "paid"); Spacer(Modifier.weight(1f)); Text(i.s("period"),fontSize = 11.sp,color = Muted) }
            Text(i.s("title"),fontWeight = FontWeight.SemiBold,fontSize = 16.sp)
            if (vm.isAdmin) Text(i.s("resident_name"),fontSize = 12.sp,color = Muted)
            Text(money(i,"amount"),fontSize = 26.sp,fontWeight = FontWeight.SemiBold)
            Text("Jatuh tempo ${i.s("due_date")}",fontSize = 12.sp,color = Muted)
            if (i.s("status") == "unpaid" && i.s("resident_id") == vm.me!!.s("id")) OutlinedButton(onClick = { show(Form("Konfirmasi pembayaran","Transfer ke rekening RT terlebih dahulu. Pembayaran baru lunas setelah diperiksa pengurus.",listOf(Field("reference","Nama pengirim, bank & referensi transfer")),"Kirim konfirmasi") { v,done -> vm.mutate("invoices/${i.s("id")}/confirm",JSONObject(v),after = done) }) },modifier = Modifier.fillMaxWidth()) { Text("Sudah transfer? Konfirmasi") }
            if (i.s("status") == "pending") { Text("Referensi: ${i.s("reference")}",fontSize = 12.sp); if (vm.isAdmin) { Text("Periksa mutasi bank sebelum menyetujui.",fontSize = 11.sp,color = Muted); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { vm.mutate("invoices/${i.s("id")}/review",JSONObject().put("approve",true)) },enabled = !vm.busy) { Text("Setujui") }; OutlinedButton(onClick = { vm.mutate("invoices/${i.s("id")}/review",JSONObject().put("approve",false)) },enabled = !vm.busy) { Text("Tolak") } } } }
            if (i.s("status") == "paid") Text("Terverifikasi • ${i.s("paid_at").take(10)}",fontSize = 11.sp,color = Pine)
        } }
    }
}

@Composable private fun NewsCard(vm: AppModel,n: JSONObject,open: () -> Unit) { Paper(Modifier.clickable(onClick = open)) { Row { Badge(n.s("category")); Spacer(Modifier.weight(1f)); Text(n.s("created_at").take(10),fontSize = 10.sp,color = Muted) }; PostPhoto(vm,n.s("photo_id"),"Foto kabar RT"); Text(n.s("title"),fontFamily = FontFamily.Serif,fontSize = 23.sp,lineHeight = 28.sp); Text(n.s("body"),maxLines = 2,overflow = TextOverflow.Ellipsis,color = Muted,fontSize = 12.sp,lineHeight = 20.sp); Row(verticalAlignment = Alignment.CenterVertically) { Text("Baca selengkapnya",fontSize = 12.sp,fontWeight = FontWeight.SemiBold,color = Pine); Spacer(Modifier.weight(1f)); Icon(AppIcons.ArrowForward,"Baca berita",tint = Pine,modifier = Modifier.size(19.dp)) } } }
@Composable private fun NewsScreen(vm: AppModel,open: (JSONObject) -> Unit,create: () -> Unit) { LazyColumn(Modifier.fillMaxSize(),contentPadding = PaddingValues(22.dp),verticalArrangement = Arrangement.spacedBy(18.dp)) { item { Eyebrow("Dari pengurus untuk warga"); Spacer(Modifier.height(10.dp)); Headline("Kabar baik\ndimulai dari sini.") }; if (vm.isAdmin) item { FilledTonalButton(onClick = create,modifier = Modifier.fillMaxWidth()) { Icon(AppIcons.Edit,null); Text("Tulis kabar RT",Modifier.padding(start = 8.dp)) } }; if (vm.news.isEmpty()) item { Empty(AppIcons.Newspaper,"Belum ada kabar RT","Pengurus akan membagikan informasi lingkungan di sini.") }; items(vm.news,key = { it.s("id") }) { NewsCard(vm,it) { open(it) } } } }

@Composable private fun FinanceScreen(vm: AppModel,expense: () -> Unit) { LazyColumn(Modifier.fillMaxSize(),contentPadding = PaddingValues(22.dp),verticalArrangement = Arrangement.spacedBy(20.dp)) {
    item { Eyebrow("Transparansi bersama"); Spacer(Modifier.height(10.dp)); Headline("Kas terbuka.\nWarga percaya.") }
    item { Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Pine).padding(24.dp),verticalArrangement = Arrangement.spacedBy(13.dp)) { Text("Saldo kas tercatat",color = Lime,fontSize = 13.sp); Text(money(vm.finance,"balance"),color = Color.White,fontSize = 33.sp,fontWeight = FontWeight.SemiBold); Text("Dari IPL terverifikasi dikurangi pengeluaran",color = Color(0xFFCCDCD2),fontSize = 11.sp) } }
    item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Paper(Modifier.weight(1f)) { Text("Pemasukan",color = Muted,fontSize = 12.sp); Text(money(vm.finance,"income"),fontWeight = FontWeight.Bold,fontSize = 16.sp) }; Paper(Modifier.weight(1f)) { Text("Pengeluaran",color = Muted,fontSize = 12.sp); Text(money(vm.finance,"expense"),fontWeight = FontWeight.Bold,fontSize = 16.sp) } } }
    if (vm.isAdmin) item { FilledTonalButton(onClick = expense,modifier = Modifier.fillMaxWidth()) { Icon(AppIcons.Add,null); Text("Catat pengeluaran",Modifier.padding(start = 8.dp)) } }
    item { Section("Rincian pengeluaran") }
    val rows = vm.finance.optJSONArray("expenses")?.objects() ?: emptyList()
    if (rows.isEmpty()) item { Empty(AppIcons.AccountBalanceWallet,"Belum ada pengeluaran","Setiap pengeluaran yang dicatat pengurus akan tampil di sini.") }
    items(rows,key = { it.s("id") }) { e -> Paper { Row { Icon(AppIcons.NorthEast,null,tint = Pine); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f),verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(e.s("title"),fontWeight = FontWeight.Medium); Text(e.s("date"),fontSize = 11.sp,color = Muted) }; Text(money(e,"amount"),fontWeight = FontWeight.SemiBold,fontSize = 13.sp) } } }
} }

@Composable private fun More(vm: AppModel,navigate: (String) -> Unit,show: (Form) -> Unit,open: (String) -> Unit) {
    var delete by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize(),contentPadding = PaddingValues(22.dp),verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Eyebrow("Ruang warga"); Spacer(Modifier.height(10.dp)); Headline("Semua urusan,\nsatu lingkungan.") }
        item { Paper { Text(vm.me!!.s("name"),fontWeight = FontWeight.SemiBold,fontSize = 20.sp); Text(vm.me!!.s("phone"),color = Muted,fontSize = 13.sp); Text(vm.me!!.s("address"),color = Muted,fontSize = 13.sp); Badge(if (vm.isAdmin) "Pengurus RT" else "Warga") } }
        if (vm.isAdmin) item { MenuRow(AppIcons.People,"Data warga","Hanya pengurus dapat menambahkan warga") { navigate("warga") } }
        item { MenuRow(AppIcons.Campaign,"Laporan lingkungan","Sampaikan dan pantau laporan warga") { navigate("laporan") } }
        if (vm.isAdmin) item { MenuRow(AppIcons.AccountBalance,"Rekening IPL","Atur tujuan transfer iuran lingkungan") { val c = vm.me!!.getJSONObject("community"); show(Form("Rekening IPL","Pastikan rekening benar sebelum menyimpan.",listOf(Field("bank","Nama bank",c.s("bank")),Field("account","Nomor rekening",c.s("account"),numeric = true),Field("holder","Nama pemilik rekening",c.s("holder")))) { v,done -> vm.mutate("community/bank",JSONObject(v),"PUT",after = done) }) } }
        item { MenuRow(AppIcons.Lock,"Ganti PIN","Lindungi akun dengan PIN pribadi") { show(Form("Ganti PIN","Gunakan enam digit. Anda perlu masuk kembali setelah PIN diganti.",listOf(Field("current_pin","PIN saat ini",numeric = true),Field("new_pin","PIN baru",numeric = true))) { v,done -> vm.mutate("me/pin",JSONObject(v),after = done) }) } }
        item { MenuRow(AppIcons.PrivacyTip,"Kebijakan privasi","Cara kami mengelola data Anda") { open("https://rtsmartpro.amarlo.online/privacy/") } }
        item { MenuRow(AppIcons.Email,"Pusat bantuan","seandyadryan2@gmail.com") { open("mailto:seandyadryan2@gmail.com") } }
        item { MenuRow(AppIcons.PersonRemove,"Ajukan penghapusan akun",if (vm.me!!.optBoolean("deletion_requested")) "Permintaan Anda sudah tercatat" else "Minta penghapusan akun dan data pribadi") { delete = true } }
        item { OutlinedButton(onClick = { vm.logout() },enabled = !vm.busy,modifier = Modifier.fillMaxWidth().height(50.dp)) { Icon(AppIcons.Logout,null); Text("Keluar dari akun",Modifier.padding(start = 10.dp)) }; Spacer(Modifier.height(18.dp)); Text("RT Smart PRO ${BuildConfig.VERSION_NAME}\nPT Deploy Dulu Pulang Nanti",fontSize = 11.sp,lineHeight = 19.sp,color = Muted) }
    }
    if (delete) AlertDialog(onDismissRequest = { delete = false },title = { Text("Ajukan penghapusan akun?") },text = { Text("Permintaan akan dicatat untuk ditindaklanjuti maksimal 30 hari. Catatan keuangan yang wajib dipertahankan dapat dianonimkan. Hubungi email dukungan untuk informasi lebih lanjut.") },confirmButton = { TextButton(onClick = { vm.mutate("me/deletion-request"); delete = false }) { Text("Ajukan permintaan") } },dismissButton = { TextButton(onClick = { delete = false }) { Text("Batal") } })
}
@Composable private fun MenuRow(icon: ImageVector,title: String,body: String,click: () -> Unit) { Row(Modifier.fillMaxWidth().clickable(onClick = click).padding(vertical = 8.dp),verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(13.dp)) { Box(Modifier.size(45.dp).clip(RoundedCornerShape(13.dp)).background(Pale),contentAlignment = Alignment.Center) { Icon(icon,title,tint = Pine) }; Column(Modifier.weight(1f),verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(title,fontSize = 14.sp,fontWeight = FontWeight.Medium); Text(body,fontSize = 11.sp,color = Muted) }; Icon(AppIcons.ChevronRight,null,tint = Muted,modifier = Modifier.size(18.dp)) } }

private fun residentEditForm(vm: AppModel,r: JSONObject) = Form(
    "Edit warga",
    "Perbarui nama, nomor HP, dan alamat. Jika nomor HP berubah, PIN baru dibuat dan sesi lama dicabut." + if (r.optBoolean("demo")) " Nomor HP boleh kosong untuk data contoh. Isi nomor HP untuk membuat akun login warga." else "",
    listOf(Field("name","Nama lengkap",r.s("name")),Field("phone",if (r.optBoolean("demo")) "Nomor HP (opsional untuk contoh)" else "Nomor HP",r.s("phone"),numeric = true,required = !r.optBoolean("demo")),Field("address","Alamat rumah",r.s("address"))),
    "Simpan perubahan"
) { v,done -> vm.mutate("residents/${r.s("id")}",JSONObject(v),"PUT",after = done) }

@Composable private fun Residents(vm: AppModel,back: () -> Unit,show: (Form) -> Unit) {
    var removing by remember { mutableStateOf<JSONObject?>(null) }
    LazyColumn(Modifier.fillMaxSize(),contentPadding = PaddingValues(22.dp),verticalArrangement = Arrangement.spacedBy(16.dp)) {
    item { TextButton(onClick = back) { Icon(AppIcons.ArrowBack,null); Text("Kembali") }; Headline("Tetangga kita."); Text("${vm.residents.size} warga • Dikelola oleh pengurus RT",color = Muted,fontSize = 12.sp) }
    item { Button(onClick = { show(Form("Tambah warga","Warga hanya dapat didaftarkan oleh pengurus. PIN awal dibuat otomatis dan ditampilkan sekali.",listOf(Field("name","Nama lengkap"),Field("phone","Nomor HP",numeric = true),Field("address","Alamat rumah")),"Tambahkan warga") { v,done -> vm.mutate("residents",JSONObject(v),after = done) }) },modifier = Modifier.fillMaxWidth()) { Icon(AppIcons.PersonAdd,null); Text("Tambah warga",Modifier.padding(start = 8.dp)) } }
    items(vm.residents,key = { it.s("id") }) { r -> Paper {
        Text(r.s("name"),fontWeight = FontWeight.SemiBold)
        Text(r.s("address"),fontSize = 12.sp,color = Muted)
        if (r.s("phone").isNotBlank()) Text(r.s("phone"),fontSize = 12.sp,color = Muted)
        if (r.optBoolean("demo")) Badge("Data contoh • Tidak bisa login")
        if (r.s("role") == "admin") Badge("Pengurus RT")
        if (r.s("role") == "resident") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { show(residentEditForm(vm,r)) },enabled = !vm.busy) { Icon(AppIcons.Edit,null,Modifier.size(17.dp)); Text("Edit",Modifier.padding(start = 6.dp)) }
                TextButton(onClick = { removing = r },enabled = !vm.busy,colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Hapus") }
            }
            if (!r.optBoolean("demo")) TextButton(onClick = { show(Form("Reset PIN warga","PIN baru akan membatalkan semua sesi warga ini. Sampaikan PIN baru secara pribadi.",emptyList(),"Reset PIN" ) { _,done -> vm.mutate("residents/${r.s("id")}/reset-pin",after = done) }) },enabled = !vm.busy) { Text("Reset PIN") }
        }
    } }
    }
    removing?.let { r -> AlertDialog(
        onDismissRequest = { if (!vm.busy) removing = null },
        title = { Text("Hapus warga?",fontFamily = FontFamily.Serif) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("${r.s("name")} akan dihapus dari daftar warga aktif dan tidak dapat masuk lagi. Riwayat tagihan serta pembayaran IPL tetap disimpan untuk pertanggungjawaban kas.")
            vm.message?.let { Text(it,color = MaterialTheme.colorScheme.error,fontSize = 12.sp) }
        } },
        confirmButton = { Button(onClick = { vm.mutate("residents/${r.s("id")}",method = "DELETE",after = { removing = null }) },enabled = !vm.busy,colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Hapus warga") } },
        dismissButton = { TextButton(onClick = { removing = null },enabled = !vm.busy) { Text("Batal") } }
    ) }
}
@Composable private fun Reports(vm: AppModel,back: () -> Unit,create: () -> Unit) { LazyColumn(Modifier.fillMaxSize(),contentPadding = PaddingValues(22.dp),verticalArrangement = Arrangement.spacedBy(16.dp)) {
    item { TextButton(onClick = back) { Icon(AppIcons.ArrowBack,null); Text("Kembali") }; Headline("Didengar.\nDitindaklanjuti.") }
    item { Button(onClick = create,modifier = Modifier.fillMaxWidth()) { Icon(AppIcons.Add,null); Text("Buat laporan",Modifier.padding(start = 8.dp)) } }
    if (vm.reports.isEmpty()) item { Empty(AppIcons.Campaign,"Belum ada laporan","Kirimkan laporan kebutuhan lingkungan kepada pengurus RT.") }
    items(vm.reports,key = { it.s("id") }) { r -> Paper { Badge(if (r.s("status") == "resolved") "Selesai" else "Menunggu tindak lanjut",r.s("status") == "open"); Text(r.s("title"),fontWeight = FontWeight.SemiBold); PostPhoto(vm,r.s("photo_id"),"Foto laporan lingkungan"); Text(r.s("body"),fontSize = 13.sp,color = Muted,lineHeight = 21.sp); Text("${r.s("resident_name")} • ${r.s("created_at").take(10)}",fontSize = 11.sp,color = Muted); if (vm.isAdmin && r.s("status") == "open") OutlinedButton(onClick = { vm.mutate("reports/${r.s("id")}/resolve") },enabled = !vm.busy) { Text("Tandai selesai") } } }
} }

@Composable private fun FormDialog(form: Form,vm: AppModel,dismiss: () -> Unit) {
    val busy = vm.busy
    var preparingPhoto by remember(form) { mutableStateOf(false) }
    val values = remember(form) { mutableStateMapOf<String,String>().apply { form.fields.forEach { put(it.key,it.value) } } }
    AlertDialog(onDismissRequest = { if (!busy && !preparingPhoto) dismiss() },title = { Text(form.title,fontFamily = FontFamily.Serif,fontSize = 27.sp) },text = {
        Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Text(form.subtitle,color = Muted,fontSize = 13.sp,lineHeight = 21.sp)
            form.fields.forEach { field -> OutlinedTextField(value = values[field.key] ?: "",onValueChange = { values[field.key] = it },label = { Text(field.label,fontSize = 12.sp) },singleLine = !field.multiline,minLines = if (field.multiline) 4 else 1,keyboardOptions = KeyboardOptions(keyboardType = if (field.numeric) KeyboardType.Number else KeyboardType.Text),visualTransformation = if (field.key.contains("pin")) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,modifier = Modifier.fillMaxWidth(),shape = RoundedCornerShape(12.dp)) }
            if (form.photo) key(form) {
                PhotoAttachmentPicker(enabled = !busy,onChange = { encoded ->
                    if (encoded == null) values.remove("photo_base64") else values["photo_base64"] = encoded
                },onBusy = { preparingPhoto = it })
            }
            vm.message?.let { Text(it,color = MaterialTheme.colorScheme.error,fontSize = 12.sp) }
        }
    },confirmButton = { Button(onClick = { form.submit(values.toMap(),dismiss) },enabled = !busy && !preparingPhoto && form.fields.all { !it.required || !values[it.key].isNullOrBlank() }) { Text(form.button) } },dismissButton = { TextButton(onClick = dismiss,enabled = !busy && !preparingPhoto) { Text("Batal") } })
}
