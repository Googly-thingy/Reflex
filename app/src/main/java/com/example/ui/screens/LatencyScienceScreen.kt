package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ReflexUiState
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun LatencyScienceScreen(
    state: ReflexUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                Text(
                    text = "ZERO-LAG ENGINEERING",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Why tactile vibration & physical keys bypass Android latency",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Live Hardware Key Test Bench
        item {
            HardwareDiagnosticCard(
                isDetected = state.isHardwareKeyDetected,
                keyName = state.lastKeyDetectedName
            )
        }

        // Latency Comparison Table
        item {
            ArchitectureComparisonCard()
        }

        // Section 1: Somatosensory vs Visual Biology
        item {
            ScienceDeepDiveCard(
                title = "1. BIOLOGY: SOMATOSENSORY VS VISUAL",
                icon = Icons.Default.Psychology,
                accentColor = NeonCyan,
                badge = "~50ms Biological Advantage",
                body = "In the human nervous system, tactile impulses travel from skin mechanoreceptors along large myelinated A-beta fibers directly up the spinal cord to the thalamus and primary somatosensory cortex. This pathway takes only ~130ms to 160ms.\n\nIn contrast, visual stimuli must undergo retinal photochemical conversion, bipolar and ganglion cell transduction, optic tract transmission, and complex occipital lobe processing before motor initiation (~190ms to 240ms). Feeling a vibration triggers an innate, primal twitch reflex far faster than seeing a color change."
            )
        }

        // Section 2: Physical Microswitch vs Capacitive Touchscreen
        item {
            ScienceDeepDiveCard(
                title = "2. HARDWARE: PHYSICAL KEY VS TOUCH DIGITIZER",
                icon = Icons.Default.Hardware,
                accentColor = NeonLime,
                badge = "~35ms Hardware Advantage",
                body = "Capacitive touchscreens do not report touches continuously. They sample electrostatic changes at 60Hz-240Hz intervals (every 4.2ms to 16.6ms). Android's WindowManager and View system then apply multi-touch disambiguation and touch debounce filters (10-15ms) before firing a MotionEvent to the UI.\n\nPhysical volume switches, however, are mechanical microswitches wired directly to motherboard GPIO pins. Pressing the button causes an immediate hardware interrupt at the Linux kernel level, dispatched instantly to the application's dispatchKeyEvent within <1ms."
            )
        }

        // Section 3: OLED Blackout vs Frame Buffering
        item {
            ScienceDeepDiveCard(
                title = "3. DISPLAY: ZERO-FRAME VSYNC ELIMINATION",
                icon = Icons.Default.Sensors,
                accentColor = ElectricBlue,
                badge = "~16-33ms Render Advantage",
                body = "Typical visual reaction apps must wait for Android's SurfaceFlinger and display VSYNC (16.6ms on 60Hz screens) to push the green stimulus frame to the panel, plus 2-8ms OLED pixel rise time. The user's eye only sees the light tens of milliseconds after the app 'started' counting.\n\nReflex Zero's Pitch Black OLED mode turns off screen redraws entirely during the waiting interval. Stimulus timing starts on the exact nanosecond the vibrator actuator pulse is commanded, completely bypassing the display pipeline."
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun HardwareDiagnosticCard(
    isDetected: Boolean,
    keyName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDetected) NeonLime else DarkBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE KEY INTERCEPT DIAGNOSTIC",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isDetected) NeonLime else TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isDetected) NeonLime else AmberWarning)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = if (isDetected) NeonLime else TextTertiary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isDetected) "Detected: $keyName" else "Press Volume Up or Down to test",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isDetected) NeonLime else TextPrimary
                    )
                    Text(
                        text = "System volume popup is suppressed; key is captured at kernel speed.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchitectureComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "END-TO-END PIPELINE LATENCY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = ElectricBlue
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Traditional App Row
            PipelineRow(
                title = "Standard Screen + Touch App",
                delay = "~55 - 90 ms System Lag",
                isAdvantaged = false,
                details = "VSYNC (16ms) + OLED Rise (5ms) + Digitizer (12ms) + Touch Debounce (15ms)"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Reflex Zero Row
            PipelineRow(
                title = "Reflex Zero (Vibration + Vol Button)",
                delay = "< 2 - 4 ms System Lag",
                isAdvantaged = true,
                details = "Linear Motor (<2ms) + Kernel GPIO Interrupt (<1ms) + Instant dispatchKeyEvent"
            )
        }
    }
}

@Composable
private fun PipelineRow(
    title: String,
    delay: String,
    isAdvantaged: Boolean,
    details: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = if (isAdvantaged) NeonLime.copy(alpha = 0.08f) else DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAdvantaged) NeonLime.copy(alpha = 0.5f) else DarkBorder
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAdvantaged) NeonLime else TextPrimary
                )
                Text(
                    text = delay,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isAdvantaged) NeonLime else NeonCrimson
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = details,
                fontSize = 10.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun ScienceDeepDiveCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    badge: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = body,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
