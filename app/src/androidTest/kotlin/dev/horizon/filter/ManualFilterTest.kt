package dev.horizon.filter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import dev.horizon.surface.overlay.FilterService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManualFilterTest {
    @Test
    fun darkestPresetAllowsTouchesAndOffRestoresOriginalBrightness() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context: Context = instrumentation.targetContext
        val device = UiDevice.getInstance(instrumentation)
        device.executeShellCommand("appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow")
        device.executeShellCommand("appops set ${context.packageName} WRITE_SETTINGS allow")
        if (Build.VERSION.SDK_INT >= 33) {
            device.executeShellCommand("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
        }
        val brightness = Settings.System.SCREEN_BRIGHTNESS
        val original = Settings.System.getInt(context.contentResolver, brightness)
        val mode = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
        context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        try {
            device.wait(Until.findObject(By.text("Turn filter on")), 5000).click()
            device.wait(Until.findObject(By.text("Filter on")), 5000)
            device.wait(Until.findObject(By.text("Deep Night")), 5000).click()
            device.pressHome()
            device.openNotification()
            device.wait(Until.findObject(By.textContains("Turn off")), 5000).click()
            assertTrue(device.wait(Until.gone(By.textContains("Turn off")), 5000))
            instrumentation.waitForIdleSync()
            assertEquals(original, Settings.System.getInt(context.contentResolver, brightness))
            assertEquals(mode, Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE))
        } finally {
            context.stopService(Intent(context, FilterService::class.java))
        }
    }
}
