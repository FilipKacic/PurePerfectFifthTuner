package com.example.pureperfect5thtuner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import android.util.Log
import androidx.core.content.ContextCompat

object AudioRecorder {
    // Buffer size for audio recording, calculated based on the sample rate and audio format
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
        KingOfConstants.SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    // AudioRecord instance for recording audio
    private var audioRecord: AudioRecord? = null

    // Flag indicating whether recording is in progress
    private var isRecording = false

    // Buffer to hold audio data
    private val audioData = ShortArray(BUFFER_SIZE)

    // Starts audio recording and processing
    fun startRecording(context: Context) {
        try {
            // Check if audioRecord is null and initialize if needed
            if (audioRecord == null) {
                // Ensure the app has permission to record audio
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    throw SecurityException("Permission to record audio is not granted.")
                }

                // Initialize AudioRecord with specified settings
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    KingOfConstants.SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    BUFFER_SIZE
                )

                // Check if AudioRecord is properly initialized
                if (audioRecord!!.state != AudioRecord.STATE_INITIALIZED) {
                    throw RuntimeException("Failed to initialize AudioRecord.")
                }
            }

            // Start audio recording
            audioRecord?.startRecording()
            isRecording = true

            // Create and start a new thread for audio data processing
            Thread {
                // Set thread priority to audio to minimize latency
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)

                while (isRecording) {
                    // Read audio data into the buffer
                    val readSizeInBytes = audioRecord?.read(audioData, 0, BUFFER_SIZE)
                    val readSize = readSizeInBytes?.div(2) ?: 0 // Convert bytes to shorts
                    if (readSize > 0) {
                        // Process the audio data
                        AudioProcessor.processAudioData(audioData, readSize)
                    } else {
                        // Log an error if reading audio data fails
                        Log.e("MyTag: AudioRecorder", "Error reading audio data.")
                    }
                    // Sleep for a specified delay before the next read
                    Thread.sleep(KingOfConstants.THREAD_DELAY_IN_MS.toLong())
                }
            }.start()
        } catch (e: Exception) {
            // Log any exceptions encountered during recording
            Log.e("MyTag: AudioRecorder", "${e.message}")
        }
    }

    // Stops audio recording and releases resources
    fun stopRecording() {
        try {
            isRecording = false
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            // Log any exceptions encountered while stopping recording
            Log.e("MyTag: AudioRecorder", "${e.message}")
        }
    }
}
