package com.example.pureperfect5thtuner

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var frequencyValue = 432.0
        updateFrequency(frequencyValue)
    }

    private fun updateFrequency(frequency: Double) {
        val textViewFrequency = findViewById<TextView>(R.id.textViewFrequency)
        val frequencyText = getString(R.string.frequency_placeholder, frequency)
        textViewFrequency.text = frequencyText
    }
}