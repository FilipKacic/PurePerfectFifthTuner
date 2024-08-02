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

        // Enables edge-to-edge display
        enableEdgeToEdge()

        // Initializes the TextView for displaying frequency
        textViewFrequency = findViewById(R.id.textViewFrequency)

        // Adjusts padding to avoid overlap with system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Checks permissions and starts audio recording if granted
        checkPermissionsAndStartRecording()

        // Registers the current activity as a listener for frequency updates
        AudioProcessor.registerListener(this)
    }

    // Checks if the app has permission to record audio; if not, requests it
    private fun checkPermissionsAndStartRecording() {
        if (checkAudioRecordPermission(this)) {
            startAudioRecording()
        } else {
            requestRecordPermission(this)
        }
    }

    // Starts audio recording in a synchronized block to ensure thread safety
    private fun startAudioRecording() {
        synchronized(lock) {
            if (!audioRecorderInitialized) {
                AudioRecorder.startRecording(this)
                audioRecorderInitialized = true
            }
        }
    }

    // Callback for when the frequency is updated, runs on the UI thread
    override fun onFrequencyUpdate(frequency: Double) {
        runOnUiThread {
            synchronized(lock) {
                val frequencyText = getString(R.string.frequency_placeholder, frequency)
                textViewFrequency.text = frequencyText
                Log.d("MyTag: MainActivity", frequencyText)
            }
        }
    }

    // Handles the result of the permission request
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

    // Lifecycle callback: called when the activity is resumed
    override fun onResume() {
        super.onResume()
        Log.d("MyTag: MainActivity", "Resumed!")
        startAudioRecording()
    }

    // Lifecycle callback: called when the activity is stopped
    override fun onStop() {
        super.onStop()
        Log.d("MyTag: MainActivity", "Stopped!")
        // Ensure audio recording is stopped when the activity is stopped
        stopRecording()
    }

    // Lifecycle callback: called when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyTag: MainActivity", "Godspeed!")
        AudioProcessor.unregisterListener(this)
        stopRecording()
    }

    // Stops audio recording in a synchronized block to ensure thread safety
    private fun stopRecording() {
        synchronized(lock) {
            if (audioRecorderInitialized) {
                AudioRecorder.stopRecording()
                audioRecorderInitialized = false
            }
        }
    }
}
