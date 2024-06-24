package com.example.pureperfect5thtuner

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pureperfect5thtuner.AudioRecordPermissionHandler.REQUEST_RECORD_AUDIO_PERMISSION
import com.example.pureperfect5thtuner.AudioRecordPermissionHandler.checkAudioRecordPermission
import com.example.pureperfect5thtuner.AudioRecordPermissionHandler.requestAudioRecordPermission

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MayTag: MainActivity", "Heaveno World!")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check permission status
        if (checkAudioRecordPermission(this)) {
            // Permission is already granted
            Toast.makeText(this, "Permission to record audio is GRANTED!", Toast.LENGTH_SHORT).show()
        } else {
            // Request permission
            requestAudioRecordPermission(this)
        }

        var frequencyValue = 432.0
        updateFrequency(frequencyValue)
    }

    override fun onResume() {
        Log.d("MayTag: MainActivity", "God Bless!")
        super.onResume()
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
                    showAudioRecordPermissionDeniedDialog()
                }
            }
        }
    }

    private fun showAudioRecordPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Audio recording permission is required for this application. It can be granted in the SETTINGS.")
            .setPositiveButton("Go to SETTINGS") { dialog, _ ->
                openApplicationSettings()
                dialog.dismiss()
            }
            .setNegativeButton("Quit App") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun openApplicationSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun updateFrequency(frequency: Double) {
        val textViewFrequency = findViewById<TextView>(R.id.textViewFrequency)
        val frequencyText = getString(R.string.frequency_placeholder, frequency)
        textViewFrequency.text = frequencyText
    }

    override fun onDestroy() {
        Log.d("MayTag: MainActivity", "God Bless!")
        super.onDestroy()
    }
}
