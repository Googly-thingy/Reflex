package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.ReactionRepository
import com.example.hardware.AudioLatencyEngine
import com.example.hardware.HapticManager
import com.example.ui.MainAppScreen
import com.example.ui.ReflexViewModel
import com.example.ui.ReflexViewModelFactory
import com.example.ui.TrialPhase
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var hapticManager: HapticManager
    private lateinit var audioEngine: AudioLatencyEngine

    private val reflexViewModel: ReflexViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ReactionRepository(database.reactionDao())
        ReflexViewModelFactory(repository, hapticManager, audioEngine)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        hapticManager = HapticManager(applicationContext)
        audioEngine = AudioLatencyEngine()

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = reflexViewModel)
            }
        }
    }

    /**
     * Ultra-low latency hardware key interception.
     * Hooks direct Linux kernel EV_KEY input events at the Activity dispatch level,
     * recording System.nanoTime() with sub-microsecond precision before any UI hierarchy dispatch.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                // Instantaneous hardware timestamp capture
                val nanoTime = System.nanoTime()
                val consumed = reflexViewModel.onHardwareKeyDown(keyCode, nanoTime)
                if (consumed) {
                    return true
                }
            } else if (event.action == KeyEvent.ACTION_UP) {
                // Suppress volume up event to prevent system volume dialog during active trials
                val phase = reflexViewModel.uiState.value.phase
                if (phase == TrialPhase.ARMED || phase == TrialPhase.SIGNAL_ACTIVE) {
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.release()
    }
}
