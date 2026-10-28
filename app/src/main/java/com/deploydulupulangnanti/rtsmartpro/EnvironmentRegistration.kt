package com.deploydulupulangnanti.rtsmartpro

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val registrationWhatsApp: String
    get() = Uri.Builder().scheme("https").authority("wa.me").appendPath("6285814654689")
        .appendQueryParameter("text", "Halo RT Smart PRO, saya ingin mendaftarkan lingkungan baru dengan biaya Rp150.000 per bulan per lingkungan, tanpa minimal rumah.\n\nNama lingkungan/RT:\nAlamat lingkungan:\nNama pengurus:\nNomor HP pengurus:\n\nMohon panduan pendaftaran dan aktivasi. Terima kasih.")
        .build().toString()

@Composable
fun EnvironmentRegistration(open: (String) -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("UNTUK PENGURUS LINGKUNGAN", fontSize = 10.sp, letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Lingkungan Anda belum terdaftar?", fontFamily = FontFamily.Serif,
                fontSize = 24.sp, lineHeight = 29.sp)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Rp150.000 / bulan", fontSize = 23.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Text("Per lingkungan · Tanpa minimal rumah", fontSize = 13.sp)
            }
            Text("Daftarkan lingkungan lewat WhatsApp. Setelah aktif, pengurus dapat menambahkan warganya sendiri.",
                fontSize = 13.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = { open(registrationWhatsApp) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp), shape = RoundedCornerShape(12.dp)) {
                Text("Daftarkan lingkungan baru", fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(onClick = { open("https://rtsmartpro.amarlo.online/pendaftaran/") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(12.dp)) {
                Text("Baca panduan pendaftaran")
            }
            Text("Biaya layanan ini terpisah dari iuran IPL warga.", fontSize = 11.sp,
                lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
