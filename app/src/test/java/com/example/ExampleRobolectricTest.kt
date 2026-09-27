package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.ReflexViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Reflex Zero", appName)
  }

  @Test
  fun `calculate stats accurately computes mean best median and standard deviation`() {
    val times = listOf(150.0, 160.0, 170.0, 180.0, 190.0)
    val stats = ReflexViewModel.calculateStats(times)
    assertEquals(170.0, stats.meanMs, 0.001)
    assertEquals(150.0, stats.bestMs, 0.001)
    assertEquals(170.0, stats.medianMs, 0.001)
    assertTrue(stats.stdDevMs > 0.0)
  }

  @Test
  fun `reflex tiers classify reaction speeds correctly`() {
    val godspeedTier = ReflexViewModel.getReflexTier(135.0)
    assertTrue(godspeedTier.title.contains("Godspeed"))

    val formula1Tier = ReflexViewModel.getReflexTier(155.0)
    assertTrue(formula1Tier.title.contains("Formula 1"))

    val athleteTier = ReflexViewModel.getReflexTier(185.0)
    assertTrue(athleteTier.title.contains("Pro Athlete"))

    val normalTier = ReflexViewModel.getReflexTier(250.0)
    assertTrue(normalTier.title.contains("Normal Human"))
  }
}
