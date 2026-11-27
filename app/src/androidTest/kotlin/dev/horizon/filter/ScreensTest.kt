package dev.horizon.filter

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreensTest {
    @Test
    fun schedulePresetsMissionAndSettingsAreReachableFromHome() {
        val instrument = InstrumentationRegistry.getInstrumentation()
        val context = instrument.targetContext
        val device = UiDevice.getInstance(instrument)
        val testOutput = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val root = testOutput?.let { File(it) } ?: context.getExternalFilesDir(null)
        val output = File(root, "screens").apply { mkdirs() }
        fun tap(text: String) {
            requireNotNull(device.wait(Until.findObject(By.text(text)), 5000)) { text }.click()
        }
        fun capture(file: String, title: String) {
            assertTrue(title, device.wait(Until.hasObject(By.text(title)), 5000))
            assertTrue(device.takeScreenshot(File(output, "$file.png")))
        }

        context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        capture("home", "Schedule")
        tap("Schedule")
        tap("Sun-linked")
        capture("schedule", "Follows the sun")
        tap("Fixed times")
        device.pressBack()
        tap("+")
        capture("presets", "+  Save current as preset")
        tap("Edit")
        capture("preset-edit", "Rename preset")
        device.pressBack()
        if (device.hasObject(By.text("Four ways to look"))) device.pressBack()
        tap("Settings")
        capture("advanced", "Advanced")
        device.pressBack()
        val mission = device.wait(Until.findObject(By.textContains("Free forever")), 2000)
            ?: requireNotNull(device.wait(Until.findObject(By.text("◯")), 2000))
        mission.click()
        capture("mission", "I build apps that ask for nothing.")
        tap("Leave a tip")
        capture("tip", "Keep the lamp on")
        tap("I’m good, thanks")
        capture("home-after-decline", "Schedule")
    }
}
