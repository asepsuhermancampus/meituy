package com.meituy.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.ln

object FilterEngine {

    fun applyFilter(bitmap: Bitmap, filterType: FilterType): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true) ?: return bitmap

        when (filterType) {
            FilterType.ORIGINAL -> return result
            FilterType.ENHANCE -> applyEnhance(result, width, height)
            FilterType.BRIGHTNESS -> applyBrightness(result, width, height, 1.2f)
            FilterType.CONTRAST -> applyContrast(result, width, height, 1.3f)
            FilterType.SATURATION -> applySaturation(result, width, height, 1.4f)
            FilterType.BLUR_REDUCTION -> applySharpen(result, width, height)
            FilterType.COLOR_CORRECTION -> applyColorCorrection(result, width, height)
            FilterType.MEITU_STYLE -> applyMeituStyle(result, width, height)
            FilterType.IPHONE_VIBRANT -> applyIphoneVibrant(result, width, height)
            FilterType.IPHONE_NATURAL -> applyIphoneNatural(result, width, height)
            FilterType.IPHONE_DRAMATIC -> applyIphoneDramatic(result, width, height)
            FilterType.IPHONE_PORTRAIT -> applyIphonePortrait(result, width, height)
            FilterType.IPHONE_CINEMATIC -> applyIphoneCinematic(result, width, height)
        }

        return result
    }

    fun applyFilterAsync(bitmap: Bitmap, filterType: FilterType, onComplete: (Bitmap) -> Unit): Bitmap {
        return applyFilter(bitmap, filterType)
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
        val step = (pixels.size / sampleSize).coerceAtLeast(1)

        var count = 0
        var i = 0
        while (i < pixels.size && count < sampleSize) {
            val pixel = pixels[i]
            avgR += Color.red(pixel)
            avgG += Color.green(pixel)
            avgB += Color.blue(pixel)
            count++
            i += step
        }

        if (count == 0) return

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

        for (j in pixels.indices) {
            val pixel = pixels[j]
            val r = (Color.red(pixel) * correctedScaleR).toInt().coerceIn(0, 255)
            val g = (Color.green(pixel) * correctedScaleG).toInt().coerceIn(0, 255)
            val b = (Color.blue(pixel) * correctedScaleB).toInt().coerceIn(0, 255)
            pixels[j] = Color.argb(Color.alpha(pixel), r, g, b)
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

    // ============================================================
    // IPHONE-STYLE FILTERS
    // Inspired by iPhone 15+ computational photography
    // ============================================================

    private fun applyIphoneVibrant(bitmap: Bitmap, width: Int, height: Int) {
        val canvas = Canvas(bitmap)
        val paint = Paint()

        val saturationMatrix = ColorMatrix(floatArrayOf(
            1.2f, 0.0f, 0.0f, 0f, 0f,
            0.0f, 1.25f, 0.0f, 0f, 0f,
            0.0f, 0.0f, 1.1f, 0f, 5f,
            0.0f, 0.0f, 0.0f, 1f, 0f
        ))
        paint.colorFilter = ColorMatrixColorFilter(saturationMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            var r = Color.red(pixel)
            var g = Color.green(pixel)
            var b = Color.blue(pixel)

            val lum = 0.299 * r + 0.587 * g + 0.114 * b

            if (lum > 230) {
                r = (r + (255 - r) * 0.12f).toInt().coerceIn(0, 255)
                g = (g + (255 - g) * 0.10f).toInt().coerceIn(0, 255)
                b = (b + (255 - b) * 0.15f).toInt().coerceIn(0, 255)
            }

            if (lum < 40) {
                val lift = (40 - lum) * 0.05f
                r = (r + lift).toInt().coerceIn(0, 255)
                g = (g + lift).toInt().coerceIn(0, 255)
                b = (b + lift * 1.2f).toInt().coerceIn(0, 255)
            }

            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applyIphoneNatural(bitmap: Bitmap, width: Int, height: Int) {
        applyColorCorrection(bitmap, width, height)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            var r = Color.red(pixel)
            var g = Color.green(pixel)
            var b = Color.blue(pixel)

            val lum = 0.299 * r + 0.587 * g + 0.114 * b

            val contrast = 1.05f
            r = (((r / 255.0 - 0.5) * contrast + 0.5) * 255).toInt().coerceIn(0, 255)
            g = (((g / 255.0 - 0.5) * contrast + 0.5) * 255).toInt().coerceIn(0, 255)
            b = (((b / 255.0 - 0.5) * contrast + 0.5) * 255).toInt().coerceIn(0, 255)

            if (lum > 240) {
                r = (r * 0.97f).toInt().coerceIn(0, 255)
                g = (g * 0.98f).toInt().coerceIn(0, 255)
                b = (b * 0.96f).toInt().coerceIn(0, 255)
            }

            if (lum < 30) {
                r = (r + 5).toInt().coerceIn(0, 255)
                g = (g + 4).toInt().coerceIn(0, 255)
                b = (b + 6).toInt().coerceIn(0, 255)
            }

            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applyIphoneDramatic(bitmap: Bitmap, width: Int, height: Int) {
        val canvas = Canvas(bitmap)
        val paint = Paint()

        val contrastMatrix = ColorMatrix(floatArrayOf(
            1.3f, 0.0f, 0.0f, 0f, -10f,
            0.0f, 1.3f, 0.0f, 0f, -10f,
            0.0f, 0.0f, 1.3f, 0f, -5f,
            0.0f, 0.0f, 0.0f, 1f, 0f
        ))
        paint.colorFilter = ColorMatrixColorFilter(contrastMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            var r = Color.red(pixel)
            var g = Color.green(pixel)
            var b = Color.blue(pixel)

            val lum = 0.299 * r + 0.587 * g + 0.114 * b

            if (lum < 30) {
                val blueShift = ((30 - lum) * 0.15).toInt()
                b = (b + blueShift).toInt().coerceIn(0, 255)
            }

            if (lum > 220) {
                r = (r * 0.92f).toInt().coerceIn(0, 255)
                g = (g * 0.94f).toInt().coerceIn(0, 255)
                b = (b * 0.96f).toInt().coerceIn(0, 255)
            }

            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applyIphonePortrait(bitmap: Bitmap, width: Int, height: Int) {
        val canvas = Canvas(bitmap)
        val paint = Paint()

        val skinMatrix = ColorMatrix(floatArrayOf(
            1.1f, 0.05f, 0.0f, 0f, 8f,
            0.0f, 1.05f, 0.02f, 0f, 5f,
            0.0f, 0.0f, 1.0f, 0f, 3f,
            0.0f, 0.0f, 0.0f, 1f, 0f
        ))
        paint.colorFilter = ColorMatrixColorFilter(skinMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            var r = Color.red(pixel)
            var g = Color.green(pixel)
            var b = Color.blue(pixel)

            val lum = 0.299 * r + 0.587 * g + 0.114 * b

            if (lum > 200) {
                val roll = (lum - 200) / 55.0
                r = (r + (255 - r) * roll * 0.08f).toInt().coerceIn(0, 255)
                g = (g + (255 - g) * roll * 0.06f).toInt().coerceIn(0, 255)
                b = (b + (255 - b) * roll * 0.05f).toInt().coerceIn(0, 255)
            }

            if (lum < 35) {
                val lift = (35 - lum) * 0.08f
                r = (r + lift).toInt().coerceIn(0, 255)
                g = (g + lift).toInt().coerceIn(0, 255)
                b = (b + lift * 1.1f).toInt().coerceIn(0, 255)
            }

            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applyIphoneCinematic(bitmap: Bitmap, width: Int, height: Int) {
        val canvas = Canvas(bitmap)
        val paint = Paint()

        val cinematicMatrix = ColorMatrix(floatArrayOf(
            0.95f, 0.05f, 0.0f, 0f, 5f,
            0.0f, 0.9f, 0.05f, 0f, 0f,
            0.05f, 0.0f, 1.1f, 0f, 10f,
            0.0f, 0.0f, 0.0f, 1f, 0f
        ))
        paint.colorFilter = ColorMatrixColorFilter(cinematicMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            var r = Color.red(pixel)
            var g = Color.green(pixel)
            var b = Color.blue(pixel)

            val lum = 0.299 * r + 0.587 * g + 0.114 * b

            val vignetteFactor = 1.0f

            if (lum < 60) {
                val tealShift = ((60 - lum) / 60.0 * 8).toInt()
                r = (r - tealShift / 2).toInt().coerceIn(0, 255)
                g = (g + tealShift / 4).toInt().coerceIn(0, 255)
                b = (b + tealShift).toInt().coerceIn(0, 255)
            }

            if (lum > 200) {
                val warmShift = ((lum - 200) / 55.0 * 10).toInt()
                r = (r + warmShift).toInt().coerceIn(0, 255)
                g = (g + warmShift / 2).toInt().coerceIn(0, 255)
                b = (b - warmShift / 3).toInt().coerceIn(0, 255)
            }

            val contrast = 1.08f
            r = (((r / 255.0 - 0.5) * contrast + 0.5) * 255).toInt().coerceIn(0, 255)
            g = (((g / 255.0 - 0.5) * contrast + 0.5) * 255).toInt().coerceIn(0, 255)
            b = (((b / 255.0 - 0.5) * contrast + 0.5) * 255).toInt().coerceIn(0, 255)

            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        applyVignette(bitmap, width, height, 0.3f)
    }

    private fun applyVignette(bitmap: Bitmap, width: Int, height: Int, strength: Float) {
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val centerX = width / 2f
        val centerY = height / 2f
        val maxDist = sqrt(centerX * centerX + centerY * centerY)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                val pixel = pixels[idx]

                val dx = (x - centerX) / centerX
                val dy = (y - centerY) / centerY
                val dist = sqrt(dx * dx + dy * dy)

                val vignette = 1.0f - (dist * strength).coerceIn(0f, 1f)

                val r = (Color.red(pixel) * vignette).toInt().coerceIn(0, 255)
                val g = (Color.green(pixel) * vignette).toInt().coerceIn(0, 255)
                val b = (Color.blue(pixel) * vignette).toInt().coerceIn(0, 255)

                pixels[idx] = Color.argb(Color.alpha(pixel), r, g, b)
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun enhanceValue(value: Int, factor: Float): Int {
        return (value * factor).toInt().coerceIn(0, 255)
    }

    private fun applyContrastValue(value: Int, contrast: Float): Int {
        return (((value / 255.0 - 0.5) * contrast + 0.5) * 255).toInt().coerceIn(0, 255)
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
    MEITU_STYLE("Meitu"),
    IPHONE_VIBRANT("Vibrant"),
    IPHONE_NATURAL("Natural"),
    IPHONE_DRAMATIC("Dramatic"),
    IPHONE_PORTRAIT("Portrait"),
    IPHONE_CINEMATIC("Cinematic")
}