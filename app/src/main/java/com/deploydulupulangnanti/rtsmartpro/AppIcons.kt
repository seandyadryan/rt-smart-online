package com.deploydulupulangnanti.rtsmartpro

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** Small, consistent 24-unit line icons; only the vectors used by this app are packaged. */
object AppIcons {
    private fun icon(name:String,path:String):ImageVector = ImageVector.Builder(name,24.dp,24.dp,24f,24f).apply {
        addPath(PathParser().parsePathString(path).toNodes(), PathFillType.NonZero,
            fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.65f,
            strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round)
    }.build()
    val Home=icon("Beranda","M3 10L12 3L21 10 M5 9V21H19V9 M9 21V13H15V21")
    val HolidayVillage=icon("Lingkungan","M2 11L8 5L14 11 M4 10V21H12V10 M7 21V15H9V21 M14 5L17 2L22 7 M16 7V21H21V7 M17 11H19 M17 15H19")
    val ReceiptLong=icon("Tagihan","M5 3L7 5L9 3L11 5L13 3L15 5L17 3L19 5V21L17 19L15 21L13 19L11 21L9 19L7 21L5 19Z M9 9H15 M9 13H15 M9 17H12")
    val Newspaper=icon("Kabar RT","M4 3H18V20H4Z M18 7H21V20H18 M7 7H15 M7 11H10V15H7Z M13 11H15 M13 15H15 M7 18H15")
    val AccountBalanceWallet=icon("Kas","M20 7H4V20H21V7 M4 7V4H18V7 M16 12H22V16H16Z M18 14H19")
    val GridView=icon("Lainnya","M3 3H9V9H3Z M15 3H21V9H15Z M3 15H9V21H3Z M15 15H21V21H15Z")
    val Refresh=icon("Muat ulang","M20 7V3 M20 7H16 M20 7C16 -1 3 2 3 12C3 21 17 25 21 15")
    val Park=icon("Pohon","M12 2L5 10H8L3 17H21L16 10H19Z M10 17V22H14V17")
    val ArrowForward=icon("Lanjut","M4 12H20 M14 6L20 12L14 18")
    val ArrowBack=icon("Kembali","M20 12H4 M10 6L4 12L10 18")
    val People=icon("Warga","M9 4A3 3 0 1 1 8.99 4 M3 21V18C3 12 15 12 15 18V21 M16 5C21 5 21 11 16 11 M18 15C22 15 22 18 22 21")
    val TaskAlt=icon("Verifikasi","M20 12A8 8 0 1 1 12 4 M8 11L12 15L22 4")
    val Campaign=icon("Laporan","M3 10H9L20 5V19L9 14H3Z M6 14L8 21H12L10 15")
    val AddCard=icon("Buat tagihan","M21 10V5H3V19H12 M3 9H20 M17 15V23 M13 19H21 M6 15H9")
    val Person=icon("Akun","M12 3A4 4 0 1 1 11.99 3 M4 21V18C4 12 20 12 20 18V21")
    val AccountBalance=icon("Bank","M2 8L12 2L22 8Z M3 21H21 M5 11V18 M10 11V18 M15 11V18 M20 11V18")
    val Add=icon("Tambah","M12 4V20 M4 12H20")
    val Edit=icon("Tulis","M4 16L16 4L20 8L8 20H4Z M14 6L18 10")
    val NorthEast=icon("Pengeluaran","M5 19L19 5 M7 5H19V17")
    val Lock=icon("PIN","M5 10H19V21H5Z M8 10V6C8 1 16 1 16 6V10 M12 14V17")
    val PrivacyTip=icon("Privasi","M12 2L21 6V12C21 18 12 22 12 22C12 22 3 18 3 12V6Z M12 7V12 M12 16V17")
    val Email=icon("Email","M3 5H21V19H3Z M3 6L12 13L21 6")
    val PersonRemove=icon("Hapus akun","M10 3A4 4 0 1 1 9.99 3 M2 21V18C2 12 16 12 16 18V21 M17 10H23")
    val PersonAdd=icon("Tambah warga","M10 3A4 4 0 1 1 9.99 3 M2 21V18C2 12 16 12 16 18V21 M20 7V13 M17 10H23")
    val Logout=icon("Keluar","M10 3H3V21H10 M9 12H22 M17 7L22 12L17 17")
    val ChevronRight=icon("Buka","M9 5L16 12L9 19")
}
