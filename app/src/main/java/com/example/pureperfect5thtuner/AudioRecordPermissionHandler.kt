package com.example.pureperfect5thtuner

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pureperfect5thtuner.KingOfConstants.REQUEST_RECORD_AUDIO_PERMISSION

object AudioRecordPermissionHandler {

    // Checks if the app has permission to record audio
    fun checkAudioRecordPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    // Requests the record audio permission from the user
    fun requestRecordPermission(activity: Activity) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
    }

    // Shows a dialog informing the user that the permission was denied and provides options to go to settings or quit the app
    fun showAudioRecordPermissionDeniedDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Permission Denied")
            .setMessage("Audio recording permission is required for this application. It can be granted in the SETTINGS.")
            .setPositiveButton("Go to SETTINGS") { dialog, _ ->
                openApplicationSettings(activity)
                dialog.dismiss()
            }
            .setNegativeButton("Quit App") { dialog, _ ->
                dialog.dismiss()
                activity.finish()
            }
            .setCancelable(false)
            .show()
    }

    // Opens the application's settings page to allow the user to manually grant permissions
    private fun openApplicationSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }
}
