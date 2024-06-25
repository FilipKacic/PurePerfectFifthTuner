package com.example.pureperfect5thtuner

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pureperfect5thtuner.AudioRecordPermissionHandler.REQUEST_RECORD_AUDIO_PERMISSION
import com.example.pureperfect5thtuner.AudioRecordPermissionHandler.checkAudioRecordPermission
import com.example.pureperfect5thtuner.AudioRecordPermissionHandler.requestRecordPermission
import com.example.pureperfect5thtuner.AudioRecordPermissionHandler.showAudioRecordPermissionDeniedDialog
import com.example.pureperfect5thtuner.UserInterfaceHelper.updateFrequency

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MyTag: MainActivity", "Heaveno World!")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (checkAudioRecordPermission(this)) {
            Toast.makeText(this, "Permission to record audio is GRANTED!", Toast.LENGTH_SHORT).show()
            AudioRecorder.startRecording(this)
        } else {
            requestRecordPermission(this) // onRequestPermissionsResult
        }

        var frequencyValue = 0.0
        updateFrequency(this, findViewById(R.id.textViewFrequency), frequencyValue)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted
                    Toast.makeText(this, "Permission to record audio is GRANTED!", Toast.LENGTH_SHORT).show()
                } else {
                    // Permission denied
                    // Toast.makeText(this, "Permission to record audio is DENIED!", Toast.LENGTH_SHORT).show()
                    showAudioRecordPermissionDeniedDialog(this)
                }
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
        AudioRecorder.stopRecording()
        super.onDestroy()
    }
}
