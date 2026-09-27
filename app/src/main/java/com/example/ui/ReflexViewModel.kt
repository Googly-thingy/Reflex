package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ReactionRepository
import com.example.data.model.ReactionRecord
import com.example.hardware.AudioLatencyEngine
import com.example.hardware.HapticManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

enum class TrialPhase {
    IDLE,
    ARMED,
    SIGNAL_ACTIVE,
    RESULT_RECORDED,
    FALSE_START
}

enum class StimulusType(val displayName: String) {
    TACTILE_VIBRATION("Tactile Vibration"),
    AUDITORY_TONE("Auditory Tone"),
    DUAL("Tactile + Audio")
}

enum class SessionMode(val totalRounds: Int, val displayName: String) {
    SINGLE(1, "Single Trial"),
    SERIES_5(5, "5-Round Benchmark"),
    SERIES_10(10, "10-Round Endurance")
}

enum class ScreenMode(val displayName: String) {
    PITCH_BLACK_OLED("Pitch Black OLED (Zero Visual)"),
    MINIMAL_TACTICAL("Tactical HUD")
}

data class ReflexUiState(
    val phase: TrialPhase = TrialPhase.IDLE,
    val lastReactionTimeMs: Double = 0.0,
    val lastInputMethod: String = "",
    val stimulusType: StimulusType = StimulusType.TACTILE_VIBRATION,
    val sessionMode: SessionMode = SessionMode.SINGLE,
    val screenMode: ScreenMode = ScreenMode.PITCH_BLACK_OLED,
    val currentRound: Int = 1,
    val seriesResults: List<Double> = emptyList(),
    val seriesCompleted: Boolean = false,
    val falseStartCountInSession: Int = 0,
    val isHardwareKeyDetected: Boolean = false,
    val lastKeyDetectedName: String = "",
    val minDelaySeconds: Float = 1.5f,
    val maxDelaySeconds: Float = 4.5f,
    val vibrationDurationMs: Long = 40L,
    val vibrationIntensity: Int = 255,
    val hasVibratorHardware: Boolean = true
)

class ReflexViewModel(
    private val repository: ReactionRepository,
    private val hapticManager: HapticManager,
    private val audioEngine: AudioLatencyEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReflexUiState(hasVibratorHardware = hapticManager.hasVibrator())
    )
    val uiState: StateFlow<ReflexUiState> = _uiState.asStateFlow()

    // Database reactive streams
    val allRecords = repository.allRecords.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val validRecords = repository.validRecords.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val personalBest = repository.personalBest.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val bestHardwareRecord = repository.bestHardwareRecord.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val bestTouchRecord = repository.bestTouchRecord.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val averageReactionTime = repository.averageReactionTime.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val averageHardwareReactionTime = repository.averageHardwareReactionTime.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val averageTouchReactionTime = repository.averageTouchReactionTime.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val totalTrials = repository.totalValidTrialsCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )
    val totalFalseStarts = repository.falseStartsCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    private var countdownJob: Job? = null
    @Volatile
    private var stimulusNanoTimestamp: Long = 0L

    /**
     * Called directly from MainActivity.dispatchKeyEvent when a volume key is pressed down.
     * Guaranteed instant nanosecond response capture.
     * Returns true if handled and should swallow system volume dialog.
     */
    fun onHardwareKeyDown(keyCode: Int, nanoTime: Long): Boolean {
        val inputName = if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN) "volume_down" else "volume_up"
        val state = _uiState.value

        when (state.phase) {
            TrialPhase.SIGNAL_ACTIVE -> {
                // High-precision reaction time calculation
                val elapsedNano = nanoTime - stimulusNanoTimestamp
                if (elapsedNano > 0) {
                    val reactionMs = elapsedNano / 1_000_000.0
                    recordSuccessfulReaction(reactionMs, inputName)
                    return true
                }
                return true
            }
            TrialPhase.ARMED -> {
                // False start! Button pressed before stimulus
                triggerFalseStart(inputName)
                return true
            }
            TrialPhase.IDLE, TrialPhase.RESULT_RECORDED, TrialPhase.FALSE_START -> {
                // Flash hardware detected badge to reassure user that keys are responding
                _uiState.value = state.copy(
                    isHardwareKeyDetected = true,
                    lastKeyDetectedName = if (inputName == "volume_down") "VOL DOWN" else "VOL UP"
                )
                return true
            }
        }
    }

    /**
     * Fallback on-screen tap response for emulator testing or comparing touch latency.
     */
    fun onScreenTap(nanoTime: Long = System.nanoTime()) {
        val state = _uiState.value
        when (state.phase) {
            TrialPhase.SIGNAL_ACTIVE -> {
                val elapsedNano = nanoTime - stimulusNanoTimestamp
                if (elapsedNano > 0) {
                    val reactionMs = elapsedNano / 1_000_000.0
                    recordSuccessfulReaction(reactionMs, "screen_tap")
                }
            }
            TrialPhase.ARMED -> {
                triggerFalseStart("screen_tap")
            }
            TrialPhase.IDLE, TrialPhase.RESULT_RECORDED, TrialPhase.FALSE_START -> {
                armTrial()
            }
        }
    }

    fun armTrial() {
        countdownJob?.cancel()
        hapticManager.cancel()
        hapticManager.triggerArmConfirm()

        val state = _uiState.value
        val round = if (state.seriesCompleted) 1 else state.currentRound
        val seriesResults = if (state.seriesCompleted) emptyList() else state.seriesResults

        _uiState.value = state.copy(
            phase = TrialPhase.ARMED,
            currentRound = round,
            seriesResults = seriesResults,
            seriesCompleted = false
        )

        // Randomized non-anticipatable delay between minDelay and maxDelay
        val delayMillis = Random.nextLong(
            (state.minDelaySeconds * 1000).toLong(),
            (state.maxDelaySeconds * 1000).toLong()
        )

        countdownJob = viewModelScope.launch {
            delay(delayMillis)
            fireStimulus()
        }
    }

    private fun fireStimulus() {
        val state = _uiState.value
        if (state.phase != TrialPhase.ARMED) return

        // 1. Record nanosecond timestamp immediately at trigger
        stimulusNanoTimestamp = System.nanoTime()

        // 2. Fire physical hardware impulses based on chosen mode
        when (state.stimulusType) {
            StimulusType.TACTILE_VIBRATION -> {
                hapticManager.triggerStimulus(state.vibrationDurationMs, state.vibrationIntensity)
            }
            StimulusType.AUDITORY_TONE -> {
                audioEngine.playTone()
            }
            StimulusType.DUAL -> {
                hapticManager.triggerStimulus(state.vibrationDurationMs, state.vibrationIntensity)
                audioEngine.playTone()
            }
        }

        // 3. Update phase
        _uiState.value = _uiState.value.copy(phase = TrialPhase.SIGNAL_ACTIVE)
    }

    private fun recordSuccessfulReaction(reactionMs: Double, inputMethod: String) {
        hapticManager.triggerSuccessPulse()
        val state = _uiState.value
        val newSeries = state.seriesResults + reactionMs
        val isSeriesComplete = state.sessionMode != SessionMode.SINGLE && newSeries.size >= state.sessionMode.totalRounds

        _uiState.value = state.copy(
            phase = TrialPhase.RESULT_RECORDED,
            lastReactionTimeMs = reactionMs,
            lastInputMethod = inputMethod,
            seriesResults = newSeries,
            seriesCompleted = isSeriesComplete,
            currentRound = if (isSeriesComplete) state.sessionMode.totalRounds else newSeries.size + 1
        )

        // Asynchronously persist to Room Database on background thread
        viewModelScope.launch {
            val record = ReactionRecord(
                reactionTimeMs = reactionMs,
                stimulusType = when (state.stimulusType) {
                    StimulusType.TACTILE_VIBRATION -> "tactile_vibration"
                    StimulusType.AUDITORY_TONE -> "auditory"
                    StimulusType.DUAL -> "dual"
                },
                inputMethod = inputMethod,
                sessionMode = when (state.sessionMode) {
                    SessionMode.SINGLE -> "single"
                    SessionMode.SERIES_5 -> "series_5"
                    SessionMode.SERIES_10 -> "series_10"
                },
                roundNumber = state.currentRound,
                isFalseStart = false
            )
            repository.insertRecord(record)
        }
    }

    private fun triggerFalseStart(inputMethod: String) {
        countdownJob?.cancel()
        hapticManager.triggerFalseStartPenalty()

        val state = _uiState.value
        _uiState.value = state.copy(
            phase = TrialPhase.FALSE_START,
            lastInputMethod = inputMethod,
            falseStartCountInSession = state.falseStartCountInSession + 1
        )

        viewModelScope.launch {
            val record = ReactionRecord(
                reactionTimeMs = 0.0,
                stimulusType = when (state.stimulusType) {
                    StimulusType.TACTILE_VIBRATION -> "tactile_vibration"
                    StimulusType.AUDITORY_TONE -> "auditory"
                    StimulusType.DUAL -> "dual"
                },
                inputMethod = inputMethod,
                sessionMode = when (state.sessionMode) {
                    SessionMode.SINGLE -> "single"
                    SessionMode.SERIES_5 -> "series_5"
                    SessionMode.SERIES_10 -> "series_10"
                },
                roundNumber = state.currentRound,
                isFalseStart = true
            )
            repository.insertRecord(record)
        }
    }

    fun resetTrial() {
        countdownJob?.cancel()
        hapticManager.cancel()
        _uiState.value = _uiState.value.copy(
            phase = TrialPhase.IDLE,
            currentRound = 1,
            seriesResults = emptyList(),
            seriesCompleted = false
        )
    }

    fun setStimulusType(type: StimulusType) {
        _uiState.value = _uiState.value.copy(stimulusType = type)
    }

    fun setSessionMode(mode: SessionMode) {
        _uiState.value = _uiState.value.copy(
            sessionMode = mode,
            currentRound = 1,
            seriesResults = emptyList(),
            seriesCompleted = false
        )
    }

    fun setScreenMode(mode: ScreenMode) {
        _uiState.value = _uiState.value.copy(screenMode = mode)
    }

    fun setVibrationDuration(durationMs: Long) {
        _uiState.value = _uiState.value.copy(vibrationDurationMs = durationMs)
    }

    fun setVibrationIntensity(intensity: Int) {
        _uiState.value = _uiState.value.copy(vibrationIntensity = intensity)
    }

    fun setDelayRange(minSec: Float, maxSec: Float) {
        _uiState.value = _uiState.value.copy(
            minDelaySeconds = minSec,
            maxDelaySeconds = maxSec
        )
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
            resetTrial()
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteRecord(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        hapticManager.cancel()
        audioEngine.release()
    }

    companion object {
        fun calculateStats(times: List<Double>): SeriesStats {
            if (times.isEmpty()) return SeriesStats(0.0, 0.0, 0.0, 0.0)
            val mean = times.average()
            val best = times.minOrNull() ?: 0.0
            val sorted = times.sorted()
            val median = if (sorted.size % 2 == 1) {
                sorted[sorted.size / 2]
            } else {
                (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
            }
            val variance = times.map { (it - mean).pow(2) }.average()
            val stdDev = sqrt(variance)
            return SeriesStats(mean, best, median, stdDev)
        }

        fun getReflexTier(ms: Double): ReflexTier {
            return when {
                ms <= 0.0 -> ReflexTier("Invalid", "False start or no response", 0xFF888888)
                ms < 140.0 -> ReflexTier("⚡ Godspeed", "Incredible somatosensory twitch reflex (Top 0.1%)", 0xFF00F5D4)
                ms < 170.0 -> ReflexTier("🏎️ Formula 1", "Elite motorsport & esports reaction tier", 0xFF39FF14)
                ms < 200.0 -> ReflexTier("🎯 Pro Athlete", "High-velocity neuromuscular responsiveness", 0xFF00D4FF)
                ms < 240.0 -> ReflexTier("⚡ Superior", "Above average human reaction speed", 0xFFFFBE0B)
                ms < 280.0 -> ReflexTier("⏱️ Normal Human", "Standard biological processing time", 0xFFFF9E00)
                else -> ReflexTier("🐢 Sluggish", "Fatigued or delayed motor transmission", 0xFFFF5400)
            }
        }
    }
}

data class SeriesStats(
    val meanMs: Double,
    val bestMs: Double,
    val medianMs: Double,
    val stdDevMs: Double
)

data class ReflexTier(
    val title: String,
    val description: String,
    val colorHex: Long
)

class ReflexViewModelFactory(
    private val repository: ReactionRepository,
    private val hapticManager: HapticManager,
    private val audioEngine: AudioLatencyEngine
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReflexViewModel::class.java)) {
            return ReflexViewModel(repository, hapticManager, audioEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
