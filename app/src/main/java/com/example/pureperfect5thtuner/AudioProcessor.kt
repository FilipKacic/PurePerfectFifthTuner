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

    fun processAudioData(audioData: ShortArray, readSizeInBytes: Int) {
        try {
            val readSize = readSizeInBytes / 2

            val fftSize = determineFFTSize(readSize)

            val data = audioData.map { it.toDouble() }.toDoubleArray()

            applyHammingWindow(data)
            applyHighPassFilter(data)

            val real = data.copyOf(fftSize)
            val imaginary = DoubleArray(fftSize)
            fft(real, imaginary, fftSize)

            val magnitude = DoubleArray(fftSize / 2)
            for (i in 0 until fftSize / 2) {
                magnitude[i] = sqrt(real[i] * real[i] + imaginary[i] * imaginary[i])
            }

            val maxIndex = findPeakIndex(magnitude)
            val interpolatedPeak = interpolatePeak(magnitude, maxIndex)

            val dominantFrequency = interpolatedPeak * SAMPLE_RATE / fftSize.toDouble()

            notifyFrequencyUpdate(dominantFrequency)

            Log.d("MyTag: AudioProcessor", "$dominantFrequency Hz")
        } catch (e: Exception) {
            Log.e("MyTag: AudioProcessor", "Error processing audio data", e)
        }
    }

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

    private fun applyHammingWindow(data: DoubleArray) {
        val n = data.size
        for (i in 0 until n) {
            val multiplier = 0.54 - 0.46 * cos(2 * PI * i / (n - 1))
            data[i] *= multiplier
        }
    }

    private fun applyHighPassFilter(data: DoubleArray) {
        val alpha = 2 * PI * CUT_OFF_FREQUENCY / SAMPLE_RATE
        var lastFilteredValue = 0.0

        for (i in data.indices) {
            val newValue = (1 - alpha) * lastFilteredValue + alpha * data[i]
            lastFilteredValue = newValue
            data[i] = newValue
        }
    }

    private fun determineFFTSize(inputSize: Int): Int {
        var fftSize = MIN_FFT_SIZE
        while (fftSize < inputSize) {
            fftSize *= 2
        }
        return fftSize
    }

    private fun fft(real: DoubleArray, imaginary: DoubleArray, fftSize: Int) {
        val n = real.size
        if (n != fftSize) {
            Log.e("MyTag: AudioProcessor", "FFT size mismatch: expected $fftSize, got $n")
            return
        }
        recursiveFFT(real, imaginary, fftSize)
    }

    private fun recursiveFFT(real: DoubleArray, imaginary: DoubleArray, n: Int) {
        if (n <= 1) return

        val halfN = n / 2
        val evenReal = DoubleArray(halfN)
        val evenImaginary = DoubleArray(halfN)
        val oddReal = DoubleArray(halfN)
        val oddImaginary = DoubleArray(halfN)

        for (i in 0 until halfN) {
            evenReal[i] = real[2 * i]
            evenImaginary[i] = imaginary[2 * i]
            oddReal[i] = real[2 * i + 1]
            oddImaginary[i] = imaginary[2 * i + 1]
        }

        recursiveFFT(evenReal, evenImaginary, halfN)
        recursiveFFT(oddReal, oddImaginary, halfN)

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
