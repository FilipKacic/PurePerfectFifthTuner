package com.example.pureperfect5thtuner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.pureperfect5thtuner.AudioProcessor.processAudioData
import com.example.pureperfect5thtuner.KingOfConstants.SAMPLE_RATE

object AudioRecorder {
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val audioData = ShortArray(BUFFER_SIZE)

    fun startRecording(context: Context) {
        if (audioRecord == null) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                return
            }
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                BUFFER_SIZE
            )
        }

        audioRecord?.startRecording()
        isRecording = true

        Thread {
            while (isRecording) {
                val readSize = audioRecord?.read(audioData, 0, BUFFER_SIZE)
                if (readSize != null && readSize > 0) {
                    processAudioData(audioData, readSize)
                }
            }
        }.start()

        Log.d("MyTag: AudioRecorder", "Recording STARTED.")
    }

    fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        Log.d("MyTag: AudioRecorder", "Recording STOPPED.")
    }
}
