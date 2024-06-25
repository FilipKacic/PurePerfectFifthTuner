package com.example.pureperfect5thtuner

import android.util.Log
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
        val readSize = readSizeInBytes / 2

        val fftSize = determineFFTSize(readSize)

        val data = audioData.map { it.toDouble() }.toDoubleArray()

        val real = data.copyOf(fftSize)
        val imaginary = DoubleArray(fftSize)
        fft(real, imaginary, fftSize)

        val magnitude = DoubleArray(fftSize / 2)
        for (i in 0 until fftSize / 2) {
            magnitude[i] = sqrt(real[i] * real[i] + imaginary[i] * imaginary[i])
        }

        var maxIndex = 0
        var maxMagnitude = magnitude[0]
        for (i in 1 until fftSize / 2) {
            if (magnitude[i] > maxMagnitude) {
                maxMagnitude = magnitude[i]
                maxIndex = i
            }
        }

        val dominantFrequency = maxIndex * SAMPLE_RATE / fftSize.toDouble()

        notifyFrequencyUpdate(dominantFrequency)

        Log.d("MyTag: AudioProcessor", "$dominantFrequency Hz")
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
