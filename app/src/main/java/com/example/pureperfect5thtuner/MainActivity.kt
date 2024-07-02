package com.example.pureperfect5thtuner

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pureperfect5thtuner.AudioRecordPermissionHandler.checkAudioRecordPermission
import com.example.pureperfect5thtuner.AudioRecordPermissionHandler.requestRecordPermission
import com.example.pureperfect5thtuner.AudioRecordPermissionHandler.showAudioRecordPermissionDeniedDialog
import com.example.pureperfect5thtuner.KingOfConstants.REQUEST_RECORD_AUDIO_PERMISSION

class MainActivity : AppCompatActivity(), FrequencyUpdateListener {

    private lateinit var textViewFrequency: TextView
    private val lock = Any()
    private var audioRecorderInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        textViewFrequency = findViewById(R.id.textViewFrequency)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkPermissionsAndStartRecording()
        AudioProcessor.registerListener(this)
    }

    private fun checkPermissionsAndStartRecording() {
        if (checkAudioRecordPermission(this)) {
            startAudioRecording()
        } else {
            requestRecordPermission(this)
        }
    }

    private fun startAudioRecording() {
        synchronized(lock) {
            if (!audioRecorderInitialized) {
                AudioRecorder.startRecording(this)
                audioRecorderInitialized = true
            }
        }
    }

    override fun onFrequencyUpdate(frequency: Double) {
        runOnUiThread {
            synchronized(lock) {
                val frequencyText = getString(R.string.frequency_placeholder, frequency)
                textViewFrequency.text = frequencyText
                Log.d("MyTag: MainActivity", frequencyText)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startAudioRecording()
            } else {
                showAudioRecordPermissionDeniedDialog(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MyTag: MainActivity", "Resumed!")
        startAudioRecording()
    }

    override fun onStop() {
        super.onStop()
        Log.d("MyTag: MainActivity", "Stopped!")
        // Ensure audio recording is stopped when the activity is stopped
        stopRecording()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyTag: MainActivity", "Godspeed!")
        AudioProcessor.unregisterListener(this)
        stopRecording()
    }

    private fun stopRecording() {
        synchronized(lock) {
            if (audioRecorderInitialized) {
                AudioRecorder.stopRecording()
                audioRecorderInitialized = false
            }
        }
    }
}
