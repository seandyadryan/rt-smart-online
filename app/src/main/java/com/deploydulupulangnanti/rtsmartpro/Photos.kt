package com.deploydulupulangnanti.rtsmartpro

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

private data class PreparedPhoto(val encoded: String, val preview: Bitmap)

private fun preparePhoto(context: Context, uri: Uri): PreparedPhoto {
    val output = ByteArrayOutputStream()
    context.contentResolver.openInputStream(uri)?.use { input ->
        val buffer = ByteArray(16384)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            require(output.size() + count <= 15 * 1024 * 1024) { "Pilih foto berukuran maksimal 15 MB." }
            output.write(buffer,0,count)
        }
    } ?: error("Foto tidak dapat dibuka. Silakan pilih ulang.")
    val raw = output.toByteArray()
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(raw,0,raw.size,bounds)
    require(bounds.outWidth > 0 && bounds.outHeight > 0 && bounds.outWidth.toLong() * bounds.outHeight <= 100_000_000L) { "Foto tidak dapat dibaca. Pilih foto JPEG, PNG, atau WebP." }
    var sample = 1
    while (max(bounds.outWidth,bounds.outHeight) / sample > 2560) sample *= 2
    val decoded = BitmapFactory.decodeByteArray(raw,0,raw.size,BitmapFactory.Options().apply { inSampleSize = sample })
        ?: error("Foto tidak dapat dibaca. Silakan pilih foto lain.")
    val ratio = minOf(1f,1280f / max(decoded.width,decoded.height))
    val scaled = if (ratio < 1f) Bitmap.createScaledBitmap(decoded,(decoded.width*ratio).roundToInt().coerceAtLeast(1),(decoded.height*ratio).roundToInt().coerceAtLeast(1),true) else decoded
    if (scaled !== decoded) decoded.recycle()
    val orientation = runCatching { ExifInterface(ByteArrayInputStream(raw)).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL) }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
    val matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f,1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f,-1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { setRotate(90f); postScale(-1f,1f) }
            ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> { setRotate(-90f); postScale(-1f,1f) }
            ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(-90f)
        }
    }
    val upright = if (matrix.isIdentity) scaled else Bitmap.createBitmap(scaled,0,0,scaled.width,scaled.height,matrix,true)
    if (upright !== scaled) scaled.recycle()
    val prepared = if (upright.hasAlpha()) Bitmap.createBitmap(upright.width,upright.height,Bitmap.Config.ARGB_8888).apply {
        val canvas = Canvas(this)
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawBitmap(upright,0f,0f,null)
    } else upright
    if (prepared !== upright) upright.recycle()
    val encoded = ByteArrayOutputStream()
    var quality = 85
    do {
        encoded.reset()
        check(prepared.compress(Bitmap.CompressFormat.JPEG,quality,encoded)) { "Foto belum dapat disiapkan." }
        quality -= 10
    } while (encoded.size() > 900_000 && quality >= 45)
    require(encoded.size() <= 2 * 1024 * 1024) { "Foto terlalu besar. Silakan pilih foto lain." }
    return PreparedPhoto(Base64.encodeToString(encoded.toByteArray(),Base64.NO_WRAP),prepared)
}

@Composable
fun PhotoAttachmentPicker(enabled: Boolean, onChange: (String?) -> Unit, onBusy: (Boolean) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf<PreparedPhoto?>(null) }
    var preparing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            preparing = true; onBusy(true); error = null
            try {
                val photo = withContext(Dispatchers.IO) { preparePhoto(context,uri) }
                selected = photo; onChange(photo.encoded)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { error = e.message ?: "Foto belum dapat dipilih. Silakan coba lagi." }
            finally { preparing = false; onBusy(false) }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Foto (opsional)",fontSize = 13.sp)
        Text("Kabar atau laporan tetap dapat dikirim tanpa foto.",fontSize = 12.sp,color = MaterialTheme.colorScheme.onSurfaceVariant)
        selected?.let { photo ->
            Image(photo.preview.asImageBitmap(),"Pratinjau foto yang dipilih",Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),contentScale = ContentScale.Fit)
        }
        if (preparing) LinearProgressIndicator(Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { picker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },enabled = enabled && !preparing) {
                Icon(AppIcons.Add,null,Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                Text(if (selected == null) "Tambah foto" else "Ganti foto")
            }
            if (selected != null) TextButton(onClick = { selected = null; error = null; onChange(null) },enabled = enabled && !preparing) { Text("Hapus foto") }
        }
        error?.let { Text(it,color = MaterialTheme.colorScheme.error,fontSize = 12.sp) }
    }
}

@Composable
fun PostPhoto(vm: AppModel, id: String, description: String) {
    if (id.isBlank()) return
    var attempt by remember(id) { mutableIntStateOf(0) }
    var failed by remember(id,attempt) { mutableStateOf(false) }
    var expanded by remember(id) { mutableStateOf(false) }
    val bitmap by produceState<Bitmap?>(null,id,attempt) {
        try { value = vm.photo(id) }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { failed = true }
    }
    val photo = bitmap
    if (photo != null) {
        Image(photo.asImageBitmap(),description,Modifier.fillMaxWidth().heightIn(min = 120.dp,max = 280.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { expanded = true },contentScale = ContentScale.Fit)
        if (expanded) Dialog(onDismissRequest = { expanded = false },properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Column(Modifier.fillMaxWidth().fillMaxHeight(.9f).padding(16.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFFF7F7F0)).padding(12.dp),horizontalAlignment = Alignment.CenterHorizontally) {
                Image(photo.asImageBitmap(),description,Modifier.weight(1f).fillMaxWidth(),contentScale = ContentScale.Fit)
                TextButton(onClick = { expanded = false }) { Text("Tutup foto") }
            }
        }
    } else if (failed) {
        TextButton(onClick = { attempt++ }) { Text("Foto belum dimuat · Coba lagi") }
    } else {
        Box(Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp),strokeWidth = 2.dp) }
    }
}
