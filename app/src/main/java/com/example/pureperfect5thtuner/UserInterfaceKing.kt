package com.example.pureperfect5thtuner

import android.content.Context
import android.widget.TextView

object UserInterfaceHelper {
    fun updateFrequency(context: Context, textView: TextView, frequency: Double) {
        val frequencyText = context.getString(R.string.frequency_placeholder, frequency)
        textView.text = frequencyText
    }
}
