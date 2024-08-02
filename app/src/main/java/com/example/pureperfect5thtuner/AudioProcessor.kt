package com.example.pureperfect5thtuner

import android.util.Log
import com.example.pureperfect5thtuner.KingOfConstants.CUT_OFF_FREQUENCY
import com.example.pureperfect5thtuner.KingOfConstants.MIN_FFT_SIZE
import com.example.pureperfect5thtuner.KingOfConstants.SAMPLE_RATE
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object AudioProcessor {
    // List of listeners to notify about frequency updates
    private val listeners = mutableListOf<FrequencyUpdateListener>()

    // Registers a new listener for frequency updates
    fun registerListener(listener: FrequencyUpdateListener) {
        listeners.add(listener)
    }

    // Unregisters an existing listener
    fun unregisterListener(listener: FrequencyUpdateListener) {
        listeners.remove(listener)
    }

    // Notifies all registered listeners about the updated frequency
    private fun notifyFrequencyUpdate(frequency: Double) {
        listeners.forEach { it.onFrequencyUpdate(frequency) }
    }

    // Processes the audio data to determine the dominant frequency
    fun processAudioData(audioData: ShortArray, readSizeInBytes: Int) {
        try {
            // Convert the byte size to the number of shorts
            val readSize = readSizeInBytes / 2

            // Determine the FFT size based on the read size
            val fftSize = determineFFTSize(readSize)

            // Convert audio data to double array
            val data = audioData.map { it.toDouble() }.toDoubleArray()

            // Apply Hamming window to the data
            applyHammingWindow(data)
            // Apply high-pass filter to remove low-frequency noise
            applyHighPassFilter(data)

            // Arrays to hold real and imaginary parts of FFT
            val real = data.copyOf(fftSize)
            val imaginary = DoubleArray(fftSize)

            // Perform FFT
            fft(real, imaginary, fftSize)

            // Calculate the magnitude of each frequency component
            val magnitude = DoubleArray(fftSize / 2)
            for (i in 0 until fftSize / 2) {
                magnitude[i] = sqrt(real[i] * real[i] + imaginary[i] * imaginary[i])
            }

            // Find the index of the peak magnitude
            val maxIndex = findPeakIndex(magnitude)
            // Interpolate the peak frequency to get a more precise value
            val interpolatedPeak = interpolatePeak(magnitude, maxIndex)

            // Calculate the dominant frequency in Hz
            val dominantFrequency = interpolatedPeak * SAMPLE_RATE / fftSize.toDouble()

            // Notify listeners of the new frequency
            notifyFrequencyUpdate(dominantFrequency)

            // Optionally log the calculates frequency for debugging
            // Log.d("MyTag: AudioProcessor", "Precise frequency: $dominantFrequency Hz")
        } catch (e: Exception) {
            // Log any exceptions that occur during processing
            Log.e("MyTag: AudioProcessor", "Error processing audio data", e)
        }
    }

    // Finds the index of the peak magnitude in the array
    private fun findPeakIndex(magnitude: DoubleArray): Int {
        var maxIndex = 0
        var maxMagnitude = magnitude[0]
        for (i in 1 until magnitude.size) {
            if (magnitude[i] > maxMagnitude) {
                maxMagnitude = magnitude[i]
                maxIndex = i
            }
        }
        return maxIndex
    }

    // Interpolates the peak magnitude to get a more accurate frequency
    private fun interpolatePeak(magnitude: DoubleArray, peakIndex: Int): Double {
        return if (peakIndex <= 0 || peakIndex >= magnitude.size - 1) {
            peakIndex.toDouble()
        } else {
            val prev = magnitude[peakIndex - 1]
            val curr = magnitude[peakIndex]
            val next = magnitude[peakIndex + 1]

            val interpolatedIndex = peakIndex + 0.5 * ((prev - next) / (prev - 2 * curr + next))
            interpolatedIndex
        }
    }

    // Applies a Hamming window to the data to reduce spectral leakage
    private fun applyHammingWindow(data: DoubleArray) {
        val n = data.size
        for (i in 0 until n) {
            val multiplier = 0.54 - 0.46 * cos(2 * PI * i / (n - 1))
            data[i] *= multiplier
        }
    }

    // Applies a high-pass filter to remove low-frequency noise
    private fun applyHighPassFilter(data: DoubleArray) {
        val alpha = 2 * PI * CUT_OFF_FREQUENCY / SAMPLE_RATE
        var lastFilteredValue = 0.0

        for (i in data.indices) {
            val newValue = (1 - alpha) * lastFilteredValue + alpha * data[i]
            lastFilteredValue = newValue
            data[i] = newValue
        }
    }

    // Determines the FFT size that is a power of 2 and at least as large as the input size
    private fun determineFFTSize(inputSize: Int): Int {
        var fftSize = MIN_FFT_SIZE
        while (fftSize < inputSize) {
            fftSize *= 2
        }
        return fftSize
    }

    // Performs the Fast Fourier Transform (FFT) on the given data
    private fun fft(real: DoubleArray, imaginary: DoubleArray, fftSize: Int) {
        val n = real.size
        if (n != fftSize) {
            Log.e("MyTag: AudioProcessor", "FFT size mismatch: expected $fftSize, got $n")
            return
        }
        recursiveFFT(real, imaginary, fftSize)
    }

    // Recursively computes the FFT
    private fun recursiveFFT(real: DoubleArray, imaginary: DoubleArray, n: Int) {
        if (n <= 1) return

        val halfN = n / 2
        val evenReal = DoubleArray(halfN)
        val evenImaginary = DoubleArray(halfN)
        val oddReal = DoubleArray(halfN)
        val oddImaginary = DoubleArray(halfN)

        // Separate the even and odd parts of the data
        for (i in 0 until halfN) {
            evenReal[i] = real[2 * i]
            evenImaginary[i] = imaginary[2 * i]
            oddReal[i] = real[2 * i + 1]
            oddImaginary[i] = imaginary[2 * i + 1]
        }

        // Recursively compute the FFT of the even and odd parts
        recursiveFFT(evenReal, evenImaginary, halfN)
        recursiveFFT(oddReal, oddImaginary, halfN)

        // Combine the results of the even and odd parts
        for (k in 0 until halfN) {
            val theta = -2.0 * PI * k / n
            val wReal = cos(theta)
            val wImaginary = sin(theta)

            val tReal = wReal * oddReal[k] - wImaginary * oddImaginary[k]
            val tImaginary = wReal * oddImaginary[k] + wImaginary * oddReal[k]

            real[k] = evenReal[k] + tReal
            imaginary[k] = evenImaginary[k] + tImaginary
            real[k + halfN] = evenReal[k] - tReal
            imaginary[k + halfN] = evenImaginary[k] - tImaginary
        }
    }
}
