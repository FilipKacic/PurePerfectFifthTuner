package com.example.pureperfect5thtuner

import android.util.Log
import com.example.pureperfect5thtuner.KingOfConstants.SAMPLE_RATE
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

interface FrequencyUpdateListener {
    fun onFrequencyUpdate(frequency: Double)
}

object AudioProcessor {
    private const val MIN_FFT_SIZE = 256 // Minimum FFT size for processing

    // List of listeners to notify frequency updates
    private val listeners = mutableListOf<FrequencyUpdateListener>()

    fun registerListener(listener: FrequencyUpdateListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: FrequencyUpdateListener) {
        listeners.remove(listener)
    }

    private fun notifyFrequencyUpdate(frequency: Double) {
        listeners.forEach { it.onFrequencyUpdate(frequency) }
    }

    fun processAudioData(audioData: ShortArray, readSize: Int) {
        Log.d("MyTag: AudioProcessor", "Processing audio data of size: $readSize")

        // Determine FFT size based on input audio data size
        val fftSize = determineFFTSize(audioData.size)

        // Convert ShortArray to DoubleArray for FFT processing
        val data = audioData.map { it.toDouble() }.toDoubleArray()

        // Perform FFT
        val real = data.copyOf()
        val imaginary = DoubleArray(fftSize)
        fft(real, imaginary, fftSize)

        // Calculate magnitude spectrum
        val magnitude = DoubleArray(fftSize / 2)
        for (i in 0 until fftSize / 2) {
            magnitude[i] = sqrt(real[i] * real[i] + imaginary[i] * imaginary[i])
        }

        // Find the index with the maximum magnitude (dominant frequency)
        var maxIndex = 0
        var maxMagnitude = magnitude[0]
        for (i in 1 until fftSize / 2) {
            if (magnitude[i] > maxMagnitude) {
                maxMagnitude = magnitude[i]
                maxIndex = i
            }
        }

        // Calculate dominant frequency in Hz
        val dominantFrequency = maxIndex * SAMPLE_RATE / fftSize.toDouble()

        // Notify listeners of the updated frequency
        notifyFrequencyUpdate(dominantFrequency)

        // Log the dominant frequency
        Log.d("MyTag: AudioProcessor", "Dominant frequency: $dominantFrequency Hz")
    }

    private fun determineFFTSize(inputSize: Int): Int {
        var fftSize = MIN_FFT_SIZE
        while (fftSize < inputSize) {
            fftSize *= 2 // Increase FFT size to the next power of 2
        }
        return fftSize
    }

    private fun fft(real: DoubleArray, imaginary: DoubleArray, fftSize: Int) {
        val n = real.size
        if (n != fftSize) {
            Log.e("MyTag: AudioProcessor", "FFT size mismatch: expected $fftSize, got $n")
            return
        }

        // Split even and odd indices
        val evenReal = DoubleArray(n / 2)
        val evenImaginary = DoubleArray(n / 2)
        val oddReal = DoubleArray(n / 2)
        val oddImaginary = DoubleArray(n / 2)
        for (i in 0 until n / 2) {
            evenReal[i] = real[2 * i]
            evenImaginary[i] = imaginary[2 * i]
            oddReal[i] = real[2 * i + 1]
            oddImaginary[i] = imaginary[2 * i + 1]
        }

        // Recursively compute FFT for even and odd halves
        fft(evenReal, evenImaginary, fftSize)
        fft(oddReal, oddImaginary, fftSize)

        // Combine results
        for (k in 0 until n / 2) {
            val theta = -2 * PI * k / n
            val wReal = cos(theta)
            val wImaginary = sin(theta)

            val realN = wReal * oddReal[k] - wImaginary * oddImaginary[k]
            val imaginaryN = wReal * oddImaginary[k] + wImaginary * oddReal[k]

            real[k] = evenReal[k] + realN
            imaginary[k] = evenImaginary[k] + imaginaryN
            real[k + n / 2] = evenReal[k] - realN
            imaginary[k + n / 2] = evenImaginary[k] - imaginaryN
        }
    }
}
