package com.example.hardware

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.sin

/**
 * Zero-latency audio synthesizer using pre-allocated in-memory static PCM buffer.
 * Provides optional auditory stimulus for comparing hearing vs somatosensory tactile latency.
 */
class AudioLatencyEngine {

    private var audioTrack: AudioTrack? = null
    private val sampleRate = 44100
    private val durationMs = 50
    private val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        try {
            val generatedSnd = ShortArray(numSamples)
            val freqOfTone = 1200.0 // Crisp 1200 Hz tone

            // Generate clean sine wave with gentle fade out to avoid clicks
            for (i in 0 until numSamples) {
                val dVal = sin(2.0 * Math.PI * i / (sampleRate / freqOfTone))
                // Envelope window: linear decay over the last 15%
                val envelope = if (i > numSamples * 0.85) {
                    (numSamples - i).toDouble() / (numSamples * 0.15)
                } else {
                    1.0
                }
                generatedSnd[i] = (dVal * 32767 * envelope * 0.85).toInt().toShort()
            }

            val bufferSize = numSamples * 2
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val format = AudioFormat.Builder()
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .build()

            audioTrack = AudioTrack(
                attributes,
                format,
                bufferSize,
                AudioTrack.MODE_STATIC,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            ).apply {
                write(generatedSnd, 0, numSamples)
            }
        } catch (_: Exception) {
            audioTrack = null
        }
    }

    /**
     * Instantly triggers the static sound buffer from position 0.
     */
    fun playTone() {
        try {
            audioTrack?.let { track ->
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.reloadStaticData()
                track.play()
            }
        } catch (_: Exception) {
            // Fallback
        }
    }

    fun release() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {
            // Ignore
        } finally {
            audioTrack = null
        }
    }
}
