package com.deploydulupulangnanti.rtsmartpro

import android.content.Intent
import android.os.SystemClock
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Uses owner-provided credentials via instrumentation args, never source code. */
@RunWith(AndroidJUnit4::class)
class ReleaseSmokeTest {
    @Test fun loginNavigateAndCaptureAdaptiveLayouts() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val device=UiDevice.getInstance(instrumentation)
        val context=instrumentation.targetContext
        val args=InstrumentationRegistry.getArguments()
        val output=File(context.getExternalFilesDir(null),"captures").apply { mkdirs() }
        fun shell(command:String) { device.executeShellCommand(command) }
        fun click(text:String) {
            val obj=device.wait(Until.findObject(By.text(text)),20000)
            assertNotNull("Missing UI element: $text",obj)
            obj.click(); device.waitForIdle(); SystemClock.sleep(1200)
        }
        fun navigate(label:String) {
            device.wait(Until.hasObject(By.text(label)),20000)
            val obj=device.findObjects(By.text(label)).maxByOrNull { it.visibleCenter.y } ?: error("Missing navigation: $label")
            obj.click(); device.waitForIdle(); SystemClock.sleep(1600)
        }
        fun capture(folder:String,name:String) {
            val file=File(output,"$folder/$name.png"); file.parentFile!!.mkdirs()
            assertTrue(device.takeScreenshot(file))
            val bitmap=BitmapFactory.decodeFile(file.absolutePath) ?: error("Screenshot must decode")
            val points=listOf(.25 to .25,.5 to .5,.75 to .75,.25 to .75,.75 to .25)
            assertTrue("Black compositor frame is not a valid app screenshot",points.any { (x,y) ->
                bitmap.getPixel((bitmap.width*x).toInt(),(bitmap.height*y).toInt()) and 0xffffff != 0
            })
            bitmap.recycle()
        }
        shell("wm size 1080x2400"); shell("wm density 420")
        shell("settings put global window_animation_scale 0")
        shell("settings put global transition_animation_scale 0")
        shell("settings put global animator_duration_scale 0")
        shell("settings put system screen_off_timeout 2147483647")
        device.wakeUp()
        shell("wm dismiss-keyguard")
        context.startActivity(context.packageManager.getLaunchIntentForPackage(context.packageName)!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        if(device.wait(Until.hasObject(By.text("Nomor HP")),15000)) {
            val fields=device.findObjects(By.clazz("android.widget.EditText"))
            assertEquals(2,fields.size)
            fields[0].click(); fields[0].text=args.getString("phone") ?: error("phone instrumentation arg required")
            fields[1].click(); fields[1].text=args.getString("pin") ?: error("pin instrumentation arg required")
            device.pressBack(); click("Masuk ke lingkungan Anda")
        }
        assertTrue("Live dashboard did not load",device.wait(Until.hasObject(By.text("Iuran lingkungan Anda")),45000))
        try {
            for(mode in listOf("phone","tablet-7","tablet-10","desktop")) {
                when(mode) {
                    "phone" -> { shell("wm size 1080x2400"); shell("wm density 420"); device.setOrientationNatural() }
                    "tablet-7" -> { shell("wm size 1200x1920"); shell("wm density 240"); device.setOrientationNatural() }
                    "tablet-10" -> { shell("wm size 1200x1920"); shell("wm density 200"); device.setOrientationLeft() }
                    "desktop" -> { shell("wm size 1080x1920"); shell("wm density 160"); device.setOrientationLeft() }
                }
                SystemClock.sleep(3500)
                for((label,name) in listOf("Beranda" to "home","IPL" to "ipl","Kabar RT" to "news","Kas" to "finance")) {
                    // Navigation items expose their label both as text and as an icon description.
                    navigate(label)
                    assertTrue(device.wait(Until.hasObject(By.pkg(context.packageName)),10000))
                    capture(mode,name)
                }
                if(mode=="phone") {
                    navigate("Lainnya"); click("Data warga")
                    assertTrue(device.wait(Until.hasObject(By.text("Tambah warga")),10000))
                    capture(mode,"residents")
                }
            }
        } finally { shell("wm size reset"); shell("wm density reset"); device.unfreezeRotation() }
    }
}
