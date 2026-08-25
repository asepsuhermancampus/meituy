package com.meituy.app

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.pow
import kotlin.math.sqrt

object FilterEngine {

    fun applyFilter(bitmap: Bitmap, filterType: FilterType): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        
        when (filterType) {
            FilterType.ORIGINAL -> return result
            FilterType.ENHANCE -> applyEnhance(result, width, height)
            FilterType.BRIGHTNESS -> applyBrightness(result, width, height, 1.2f)
            FilterType.CONTRAST -> applyContrast(result, width, height, 1.3f)
            FilterType.SATURATION -> applySaturation(result, width, height, 1.4f)
            FilterType.BLUR_REDUCTION -> applySharpen(result, width, height)
            FilterType.COLOR_CORRECTION -> applyColorCorrection(result, width, height)
            FilterType.MEITU_STYLE -> applyMeituStyle(result, width, height)
        }
        
        return result
    }

    private fun applyEnhance(bitmap: Bitmap, width: Int, height: Int) {
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            var r = Color.red(pixel)
            var g = Color.green(pixel)
            var b = Color.blue(pixel)
            
            val brightness = (r + g + b) / 3.0
            if (brightness > 200) {
                r = (r * 0.85).toInt().coerceIn(0, 255)
                g = (g * 0.85).toInt().coerceIn(0, 255)
                b = (b * 0.85).toInt().coerceIn(0, 255)
            }
            
            r = enhanceValue(r, 1.15f)
            g = enhanceValue(g, 1.1f)
            b = enhanceValue(b, 1.12f)
            
            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }
        
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applyBrightness(bitmap: Bitmap, width: Int, height: Int, factor: Float) {
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = enhanceValue(Color.red(pixel), factor)
            val g = enhanceValue(Color.green(pixel), factor)
            val b = enhanceValue(Color.blue(pixel), factor)
            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }
        
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applyContrast(bitmap: Bitmap, width: Int, height: Int, factor: Float) {
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val contrast = ((factor + 1.0) / 2.0).toFloat()
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = applyContrastValue(Color.red(pixel), contrast)
            val g = applyContrastValue(Color.green(pixel), contrast)
            val b = applyContrastValue(Color.blue(pixel), contrast)
            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }
        
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applySaturation(bitmap: Bitmap, width: Int, height: Int, factor: Float) {
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            
            val gray = 0.299 * r + 0.587 * g + 0.114 * b
            
            val newR = (gray + factor * (r - gray)).toInt().coerceIn(0, 255)
            val newG = (gray + factor * (g - gray)).toInt().coerceIn(0, 255)
            val newB = (gray + factor * (b - gray)).toInt().coerceIn(0, 255)
            
            pixels[i] = Color.argb(Color.alpha(pixel), newR, newG, newB)
        }
        
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applySharpen(bitmap: Bitmap, width: Int, height: Int) {
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val original = pixels.copyOf()
        
        val kernel = arrayOf(
            intArrayOf(0, -1, 0),
            intArrayOf(-1, 5, -1),
            intArrayOf(0, -1, 0)
        )
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var sumR = 0
                var sumG = 0
                var sumB = 0
                
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val idx = (y + ky) * width + (x + kx)
                        val pixel = original[idx]
                        val weight = kernel[ky + 1][kx + 1]
                        sumR += Color.red(pixel) * weight
                        sumG += Color.green(pixel) * weight
                        sumB += Color.blue(pixel) * weight
                    }
                }
                
                val idx = y * width + x
                val r = sumR.coerceIn(0, 255)
                val g = sumG.coerceIn(0, 255)
                val b = sumB.coerceIn(0, 255)
                pixels[idx] = Color.argb(Color.alpha(original[idx]), r, g, b)
            }
        }
        
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applyColorCorrection(bitmap: Bitmap, width: Int, height: Int) {
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = Color.red(pixel).toFloat()
            val g = Color.green(pixel).toFloat()
            val b = Color.blue(pixel).toFloat()
            
            val maxVal = maxOf(r, g, b)
            if (maxVal > 220) {
                val correctedR = (r * 0.9f).toInt().coerceIn(0, 255)
                val correctedG = (g * 0.95f).toInt().coerceIn(0, 255)
                val correctedB = (b * 0.9f).toInt().coerceIn(0, 255)
                pixels[i] = Color.argb(Color.alpha(pixel), correctedR, correctedG, correctedB)
            }
        }
        
        applyWhiteBalanceCorrection(pixels, width, height)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applyWhiteBalanceCorrection(pixels: IntArray, width: Int, height: Int) {
        var avgR = 0.0
        var avgG = 0.0
        var avgB = 0.0
        
        val sampleSize = minOf(10000, pixels.size)
        val step = pixels.size / sampleSize
        
        var count = 0
        for (i in pixels.indices step step) {
            val pixel = pixels[i]
            avgR += Color.red(pixel)
            avgG += Color.green(pixel)
            avgB += Color.blue(pixel)
            count++
        }
        
        avgR /= count
        avgG /= count
        avgB /= count
        
        val avgGray = (avgR + avgG + avgB) / 3.0
        val scaleR = avgGray / avgR.coerceAtLeast(1.0)
        val scaleG = avgGray / avgG.coerceAtLeast(1.0)
        val scaleB = avgGray / avgB.coerceAtLeast(1.0)
        
        val correctedScaleR = 1.0 + (scaleR - 1.0) * 0.4
        val correctedScaleG = 1.0 + (scaleG - 1.0) * 0.4
        val correctedScaleB = 1.0 + (scaleB - 1.0) * 0.4
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = (Color.red(pixel) * correctedScaleR).toInt().coerceIn(0, 255)
            val g = (Color.green(pixel) * correctedScaleG).toInt().coerceIn(0, 255)
            val b = (Color.blue(pixel) * correctedScaleB).toInt().coerceIn(0, 255)
            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }
    }

    private fun applyMeituStyle(bitmap: Bitmap, width: Int, height: Int) {
        applyContrast(bitmap, width, height, 1.2f)
        applySaturation(bitmap, width, height, 1.15f)
        applyColorCorrection(bitmap, width, height)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            
            val brightness = (r + g + b) / 3.0
            
            val (nr, ng, nb) = when {
                brightness > 200 -> {
                    Triple(
                        (r * 0.85).toInt().coerceIn(0, 255),
                        (g * 0.87).toInt().coerceIn(0, 255),
                        (b * 0.88).toInt().coerceIn(0, 255)
                    )
                }
                brightness < 50 -> {
                    Triple(
                        (r * 1.1).toInt().coerceIn(0, 255),
                        (g * 1.08).toInt().coerceIn(0, 255),
                        (b * 1.12).toInt().coerceIn(0, 255)
                    )
                }
                else -> Triple(r, g, b)
            }
            
            pixels[i] = Color.argb(Color.alpha(pixel), nr, ng, nb)
        }
        
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun enhanceValue(value: Int, factor: Float): Int {
        val newValue = (value * factor).toInt()
        return newValue.coerceIn(0, 255)
    }

    private fun applyContrastValue(value: Int, contrast: Float): Int {
        val newValue = (((value / 255.0 - 0.5) * contrast + 0.5) * 255).toInt()
        return newValue.coerceIn(0, 255)
    }
}

enum class FilterType(val displayName: String) {
    ORIGINAL("Original"),
    ENHANCE("Enhance"),
    BRIGHTNESS("Brightness"),
    CONTRAST("Contrast"),
    SATURATION("Saturation"),
    BLUR_REDUCTION("Sharp"),
    COLOR_CORRECTION("Color Fix"),
    MEITU_STYLE("Meitu Style")
}