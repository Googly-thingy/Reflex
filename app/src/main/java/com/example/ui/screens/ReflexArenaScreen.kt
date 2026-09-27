package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ReflexUiState
import com.example.ui.ReflexViewModel
import com.example.ui.ScreenMode
import com.example.ui.SessionMode
import com.example.ui.StimulusType
import com.example.ui.TrialPhase
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonLime
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun ReflexArenaScreen(
    state: ReflexUiState,
    onArmTrial: () -> Unit,
    onScreenTap: () -> Unit,
    onResetTrial: () -> Unit,
    onSetStimulusType: (StimulusType) -> Unit,
    onSetSessionMode: (SessionMode) -> Unit,
    modifier: Modifier = Modifier
) {
    // If Pitch Black OLED mode is active and trial is ARMED, render pure blackout screen for zero visual stimulus
    if (state.screenMode == ScreenMode.PITCH_BLACK_OLED && state.phase == TrialPhase.ARMED) {
        PitchBlackArmedScreen(
            onScreenTap = onScreenTap,
            modifier = modifier
        )
        return
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header & Branding
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Reflex Zero",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REFLEX ZERO",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Hardware Switch • Tactile Impulse",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Live Hardware Switch Status Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (state.isHardwareKeyDetected) NeonLime.copy(alpha = 0.15f) else DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (state.isHardwareKeyDetected) NeonLime else DarkBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (state.isHardwareKeyDetected) NeonLime else AmberWarning)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.isHardwareKeyDetected) "HW KEY ACTIVE" else "VOL KEYS READY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (state.isHardwareKeyDetected) NeonLime else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode Selectors
        ModeSelectionRow(
            currentStimulus = state.stimulusType,
            currentSession = state.sessionMode,
            onSelectStimulus = onSetStimulusType,
            onSelectSession = onSetSessionMode,
            isLocked = state.phase == TrialPhase.ARMED || state.phase == TrialPhase.SIGNAL_ACTIVE
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Main Reflex Arena Interactive Card
        MainReflexArenaCard(
            state = state,
            onArmTrial = onArmTrial,
            onScreenTap = onScreenTap,
            onResetTrial = onResetTrial
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Hardware Volume Button Latency Benefit Tip
        HardwareAdvantageCard(onScreenTapFallback = onScreenTap)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ModeSelectionRow(
    currentStimulus: StimulusType,
    currentSession: SessionMode,
    onSelectStimulus: (StimulusType) -> Unit,
    onSelectSession: (SessionMode) -> Unit,
    isLocked: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "STIMULUS SENSORY CHANNEL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = TextTertiary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StimulusPill(
                    title = "Tactile Vibration",
                    icon = Icons.Default.Vibration,
                    isSelected = currentStimulus == StimulusType.TACTILE_VIBRATION,
                    onClick = { if (!isLocked) onSelectStimulus(StimulusType.TACTILE_VIBRATION) },
                    modifier = Modifier.weight(1f)
                )
                StimulusPill(
                    title = "Audio Tone",
                    icon = Icons.Default.VolumeUp,
                    isSelected = currentStimulus == StimulusType.AUDITORY_TONE,
                    onClick = { if (!isLocked) onSelectStimulus(StimulusType.AUDITORY_TONE) },
                    modifier = Modifier.weight(1f)
                )
                StimulusPill(
                    title = "Dual",
                    icon = Icons.Default.GraphicEq,
                    isSelected = currentStimulus == StimulusType.DUAL,
                    onClick = { if (!isLocked) onSelectStimulus(StimulusType.DUAL) },
                    modifier = Modifier.weight(0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "BENCHMARK SERIES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = TextTertiary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SessionPill(
                    title = "Single",
                    isSelected = currentSession == SessionMode.SINGLE,
                    onClick = { if (!isLocked) onSelectSession(SessionMode.SINGLE) },
                    modifier = Modifier.weight(1f)
                )
                SessionPill(
                    title = "5-Round Bench",
                    isSelected = currentSession == SessionMode.SERIES_5,
                    onClick = { if (!isLocked) onSelectSession(SessionMode.SERIES_5) },
                    modifier = Modifier.weight(1f)
                )
                SessionPill(
                    title = "10-Round Bench",
                    isSelected = currentSession == SessionMode.SERIES_10,
                    onClick = { if (!isLocked) onSelectSession(SessionMode.SERIES_10) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StimulusPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) NeonCyan else DarkBorder
    val bgColor = if (isSelected) NeonCyan.copy(alpha = 0.12f) else DarkSurfaceVariant

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp)),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) NeonCyan else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) NeonCyan else TextSecondary
            )
        }
    }
}

@Composable
private fun SessionPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) ElectricBlue else DarkBorder
    val bgColor = if (isSelected) ElectricBlue.copy(alpha = 0.12f) else DarkSurfaceVariant

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp)),
        color = bgColor
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) ElectricBlue else TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MainReflexArenaCard(
    state: ReflexUiState,
    onArmTrial: () -> Unit,
    onScreenTap: () -> Unit,
    onResetTrial: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Pulsing animation for armed state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reflex_arena_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            when (state.phase) {
                TrialPhase.SIGNAL_ACTIVE -> NeonLime
                TrialPhase.FALSE_START -> NeonCrimson
                TrialPhase.RESULT_RECORDED -> NeonCyan
                TrialPhase.ARMED -> ElectricBlue
                TrialPhase.IDLE -> DarkBorder
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Series Round Indicator (if applicable)
            if (state.sessionMode != SessionMode.SINGLE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.seriesCompleted) "BENCHMARK COMPLETED" else "ROUND ${state.currentRound} OF ${state.sessionMode.totalRounds}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (state.seriesCompleted) NeonLime else ElectricBlue
                    )
                    Text(
                        text = "${state.seriesResults.size}/${state.sessionMode.totalRounds} Recorded",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { state.seriesResults.size.toFloat() / state.sessionMode.totalRounds },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = NeonCyan,
                    trackColor = DarkSurfaceVariant
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Big Central Trigger / Display Circle
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(
                        when (state.phase) {
                            TrialPhase.SIGNAL_ACTIVE -> NeonLime.copy(alpha = 0.25f)
                            TrialPhase.FALSE_START -> NeonCrimson.copy(alpha = 0.15f)
                            TrialPhase.RESULT_RECORDED -> NeonCyan.copy(alpha = 0.12f)
                            TrialPhase.ARMED -> ElectricBlue.copy(alpha = 0.1f)
                            TrialPhase.IDLE -> DarkSurfaceVariant
                        }
                    )
                    .border(
                        width = if (state.phase == TrialPhase.SIGNAL_ACTIVE) 3.dp else 2.dp,
                        color = when (state.phase) {
                            TrialPhase.SIGNAL_ACTIVE -> NeonLime
                            TrialPhase.FALSE_START -> NeonCrimson
                            TrialPhase.RESULT_RECORDED -> NeonCyan
                            TrialPhase.ARMED -> ElectricBlue
                            TrialPhase.IDLE -> DarkBorder
                        },
                        shape = CircleShape
                    )
                    .scale(if (state.phase == TrialPhase.ARMED) pulseScale else 1f)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            when (state.phase) {
                                TrialPhase.IDLE, TrialPhase.RESULT_RECORDED, TrialPhase.FALSE_START -> onArmTrial()
                                TrialPhase.ARMED, TrialPhase.SIGNAL_ACTIVE -> onScreenTap()
                            }
                        }
                    )
                    .testTag("central_trigger_circle"),
                contentAlignment = Alignment.Center
            ) {
                when (state.phase) {
                    TrialPhase.IDLE -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Arm Test",
                                tint = NeonCyan,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "TAP TO ARM",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rest thumb on\nVolume button",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    TrialPhase.ARMED -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = "Waiting for impulse",
                                tint = ElectricBlue,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "READY...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = ElectricBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "WAIT FOR VIBRATION\nTHEN CLICK VOLUME",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    TrialPhase.SIGNAL_ACTIVE -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Click now",
                                tint = NeonLime,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "CLICK!",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = NeonLime
                            )
                        }
                    }

                    TrialPhase.RESULT_RECORDED -> {
                        val tier = ReflexViewModel.getReflexTier(state.lastReactionTimeMs)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "%.1f".format(state.lastReactionTimeMs),
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color(tier.colorHex)
                            )
                            Text(
                                text = "MILLISECONDS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(tier.colorHex).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = tier.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(tier.colorHex),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    TrialPhase.FALSE_START -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "False start",
                                tint = NeonCrimson,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "EARLY PRESS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = NeonCrimson
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Anticipation penalty.\nWait for impulse!",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action / Status Text description below circle
            when (state.phase) {
                TrialPhase.IDLE -> {
                    Text(
                        text = "Press Physical Volume Down/Up or Tap circle to start countdown",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
                TrialPhase.ARMED -> {
                    Text(
                        text = "Vibration will pulse randomly within ${state.minDelaySeconds}s - ${state.maxDelaySeconds}s",
                        fontSize = 12.sp,
                        color = ElectricBlue,
                        textAlign = TextAlign.Center
                    )
                }
                TrialPhase.SIGNAL_ACTIVE -> {
                    Text(
                        text = "High-precision timer running at nanosecond resolution!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime,
                        textAlign = TextAlign.Center
                    )
                }
                TrialPhase.RESULT_RECORDED -> {
                    val inputLabel = when (state.lastInputMethod) {
                        "volume_down" -> "⚡ Captured via Hardware Volume Down (0ms lag)"
                        "volume_up" -> "⚡ Captured via Hardware Volume Up (0ms lag)"
                        else -> "📱 Captured via Touchscreen Tap (~35ms digitizer lag)"
                    }
                    Text(
                        text = inputLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (state.lastInputMethod.startsWith("volume")) NeonLime else AmberWarning,
                        textAlign = TextAlign.Center
                    )
                }
                TrialPhase.FALSE_START -> {
                    Text(
                        text = "Invalid trial. Rest your finger gently without depressing switch.",
                        fontSize = 12.sp,
                        color = NeonCrimson,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Series Summary if Benchmark completed
            if (state.seriesCompleted && state.seriesResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                val stats = ReflexViewModel.calculateStats(state.seriesResults)
                SeriesStatsCard(stats = stats, results = state.seriesResults)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.phase == TrialPhase.RESULT_RECORDED || state.phase == TrialPhase.FALSE_START) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onArmTrial)
                            .border(1.dp, NeonCyan, RoundedCornerShape(12.dp))
                            .testTag("arm_again_button"),
                        color = NeonCyan.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.seriesCompleted) "NEW BENCHMARK" else "NEXT TRIAL",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = NeonCyan
                            )
                        }
                    }
                }

                if (state.phase == TrialPhase.ARMED) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onResetTrial)
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                            .testTag("cancel_button"),
                        color = DarkSurfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CANCEL",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PitchBlackArmedScreen(
    onScreenTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pure OLED absolute black screen with zero rendering overhead
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onScreenTap
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ultra-faint breathing dot to let user know app is armed
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222222))
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "BLIND REFLEX MODE",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                color = Color(0xFF444444)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Rest eyes • Feel the vibration\nClick Volume Button immediately",
                fontSize = 12.sp,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SeriesStatsCard(stats: com.example.ui.SeriesStats, results: List<Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "BENCHMARK PERFORMANCE SUMMARY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatColumn(label = "MEAN", value = "%.1f ms".format(stats.meanMs), color = TextPrimary)
                StatColumn(label = "PEAK BEST", value = "%.1f ms".format(stats.bestMs), color = NeonLime)
                StatColumn(label = "MEDIAN", value = "%.1f ms".format(stats.medianMs), color = ElectricBlue)
                StatColumn(label = "JITTER (σ)", value = "±%.1f ms".format(stats.stdDevMs), color = AmberWarning)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                results.forEachIndexed { index, res ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp)),
                        color = DarkSurface
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "R${index + 1}", fontSize = 9.sp, color = TextTertiary)
                            Text(text = "%.0f".format(res), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, color = TextTertiary, fontFamily = FontFamily.Monospace)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun HardwareAdvantageCard(onScreenTapFallback: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NeonLime,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WHY VOLUME BUTTON + VIBRATION?",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = NeonLime
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Touchscreens introduce 30-70ms of digitizer sampling lag & VSYNC queue delay. Physical volume switches hook direct Linux kernel input interrupts (<1ms), and somatosensory nerve conduction is 50ms faster than retinal visual processing.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Interactive compare button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onScreenTapFallback)
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
                color = DarkSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Touchscreen Tap Fallback (Compare with Hardware)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
