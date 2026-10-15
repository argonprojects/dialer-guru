package com.example.gurudialer

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.exp
import kotlin.math.sin

/**
 * Provides classic mechanical rotary dial sound effects, tactile haptic feedback,
 * and clear Text-to-Speech voice prompts when a contact photo is tapped.
 */
class SoundAndSpeechHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isTtsReady = false
    private val audioScope = CoroutineScope(Dispatchers.Default)

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            // Slightly slower speech rate (0.9x) and clear pitch for elderly listeners
            tts?.setSpeechRate(0.9f)
            tts?.setPitch(1.0f)
            isTtsReady = true
        }
    }

    /**
     * Plays a classic mechanical rotary click burst + speaks the prompt aloud.
     */
    fun onContactPhotoTapped(contactName: String) {
        triggerHapticClick()
        audioScope.launch {
            playClassicRotarySound(pulses = 4)
        }
        speak("Call $contactName? Tap green for Yes, or red for No.")
    }

    /**
     * Plays a longer rotary wind-up sound and announces that dialing has started.
     */
    fun onCallConfirmed(contactName: String) {
        triggerHapticClick()
        audioScope.launch {
            playClassicRotarySound(pulses = 7)
        }
        speak("Calling $contactName")
    }

    fun onCallCancelled() {
        triggerHapticClick()
        speak("Cancelled")
    }

    fun speak(message: String) {
        if (isTtsReady) {
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "guru_tts_prompt")
        }
    }

    /**
     * Synthesizes a warm, vintage mechanical rotary pulse train in memory using PCM audio
     * so the app requires no external MP3 assets.
     */
    private fun playClassicRotarySound(pulses: Int) {
        val sampleRate = 22050
        val pulseDurationMs = 55
        val gapDurationMs = 35
        val samplesPerPulse = (sampleRate * pulseDurationMs) / 1000
        val samplesPerGap = (sampleRate * gapDurationMs) / 1000
        val totalSamples = pulses * (samplesPerPulse + samplesPerGap)

        val pcmBuffer = ShortArray(totalSamples)
        var offset = 0

        for (p in 0 until pulses) {
            for (i in 0 until samplesPerPulse) {
                val t = i.toDouble() / sampleRate
                // Mechanical brass bell + escapement click envelope
                val envelope = exp(-t * 65.0)
                val wave = (0.65 * sin(2.0 * Math.PI * 1150.0 * t) +
                        0.35 * sin(2.0 * Math.PI * 2400.0 * t)) * envelope
                pcmBuffer[offset + i] = (wave * Short.MAX_VALUE * 0.85).toInt().toShort()
            }
            offset += samplesPerPulse + samplesPerGap
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(pcmBuffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(pcmBuffer, 0, pcmBuffer.size)
        audioTrack.play()
    }

    private fun triggerHapticClick() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
