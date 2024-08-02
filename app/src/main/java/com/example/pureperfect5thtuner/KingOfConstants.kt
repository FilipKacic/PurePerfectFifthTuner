package com.example.pureperfect5thtuner

object KingOfConstants {
    // The sample rate for audio recording (44.1 kHz)
    const val SAMPLE_RATE = 44100

    // Request code for recording audio permission
    const val REQUEST_RECORD_AUDIO_PERMISSION = 432

    // Minimum size for the FFT (Fast Fourier Transform)
    const val MIN_FFT_SIZE = 256

    // Cut-off frequency for the high-pass filter (in Hz)
    const val CUT_OFF_FREQUENCY = 2187.0

    // Delay between audio processing threads (in milliseconds)
    const val THREAD_DELAY_IN_MS = 500
}
