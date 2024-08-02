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
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
        KingOfConstants.SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val audioData = ShortArray(BUFFER_SIZE)

    fun startRecording(context: Context) {
        try {
            if (audioRecord == null) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    throw SecurityException("Permission to record audio is not granted.")
                }
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    KingOfConstants.SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    BUFFER_SIZE
                )
                if (audioRecord!!.state != AudioRecord.STATE_INITIALIZED) {
                    throw RuntimeException("Failed to initialize AudioRecord.")
                }
            }

            audioRecord?.startRecording()
            isRecording = true

            Thread {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
                while (isRecording) {
                    val readSizeInBytes = audioRecord?.read(audioData, 0, BUFFER_SIZE)
                    val readSize = readSizeInBytes?.div(2) ?: 0 // Convert bytes to shorts
                    if (readSize > 0) {
                        AudioProcessor.processAudioData(audioData, readSize)
                    } else {
                        Log.e("MyTag: AudioRecorder", "Error reading audio data.")
                    }
                    Thread.sleep(KingOfConstants.THREAD_DELAY_IN_MS.toLong())
                }
            }.start()
        } catch (e: Exception) {
            Log.e("MyTag: AudioRecorder", "${e.message}")
        }
    }

    fun stopRecording() {
        try {
            isRecording = false
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            Log.e("MyTag: AudioRecorder", "${e.message}")
        }
    }
}
