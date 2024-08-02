package com.example.pureperfect5thtuner

// Interface for listening to frequency updates
interface FrequencyUpdateListener {
    // Method to be called when a new frequency is detected
    fun onFrequencyUpdate(frequency: Double)
}
