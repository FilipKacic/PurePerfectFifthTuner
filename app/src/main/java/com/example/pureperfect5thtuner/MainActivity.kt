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
import com.example.pureperfect5thtuner.UserInterfaceKing.updateFrequency

class MainActivity : AppCompatActivity(), FrequencyUpdateListener {
    private lateinit var textViewFrequency: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MyTag: MainActivity", "Heaveno World!")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        textViewFrequency = findViewById(R.id.textViewFrequency)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (checkAudioRecordPermission(this)) {
            AudioRecorder.startRecording(this)
        } else {
            requestRecordPermission(this)
        }

        AudioProcessor.registerListener(this)
    }

    override fun onFrequencyUpdate(frequency: Double) {
        runOnUiThread {
            updateFrequency(this@MainActivity, textViewFrequency, frequency)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AudioRecorder.startRecording(this)
            } else {
                showAudioRecordPermissionDeniedDialog(this)
            }
        }
    }

    override fun onStop() {
        Log.d("MyTag: MainActivity", "Stopped!")
        AudioRecorder.stopRecording()
        super.onStop()
    }

    override fun onResume() {
        Log.d("MyTag: MainActivity", "Resumed!")
        AudioRecorder.startRecording(this)
        super.onResume()
    }

    override fun onDestroy() {
        Log.d("MyTag: MainActivity", "Godspeed!")
        AudioProcessor.unregisterListener(this)
        AudioRecorder.stopRecording()
        super.onDestroy()
    }
}
