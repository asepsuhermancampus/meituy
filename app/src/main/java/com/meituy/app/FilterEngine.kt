package com.meituy.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import kotlin.math.sqrt

object FilterEngine {

    fun applyFilter(bitmap: Bitmap, filterType: FilterType, intensity: Float = 1.0f): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true) ?: return bitmap

        when (filterType) {
            FilterType.ORIGINAL -> return result
            FilterType.RICON_FLASH -> applyRiconFlash(result, width, height, intensity)
            FilterType.FLASH_FILM -> applyFlashFilm(result, width, height, intensity)
            FilterType.G7X -> applyG7X(result, width, height, intensity)
            FilterType.FUJI_FLASH -> applyFujiFlash(result, width, height, intensity)
            FilterType.GOLDEN_HOUR -> applyGoldenHour(result, width, height, intensity)
            FilterType.MATAHARI_TERBENAM -> applyMatahariTerbenam(result, width, height, intensity)
            FilterType.LAMPU_KILAT_IPHONE -> applyLampuKilatIphone(result, width, height, intensity)
        }

        return result
    }

    // Interpolasi dari identitas: intensity 0 = gambar asli, 1 = kekuatan penuh.
    private fun blendedMatrix(gains: FloatArray, offsets: FloatArray, intensity: Float): ColorMatrix {
        return ColorMatrix(floatArrayOf(
            1f + (gains[0] - 1f) * intensity, 0.0f, 0.0f, 0f, offsets[0] * intensity,
            0.0f, 1f + (gains[1] - 1f) * intensity, 0.0f, 0f, offsets[1] * intensity,
            0.0f, 0.0f, 1f + (gains[2] - 1f) * intensity, 0f, offsets[2] * intensity,
            0.0f, 0.0f, 0.0f, 1f, 0f
        ))
    }

    private fun drawWithMatrix(bitmap: Bitmap, matrix: ColorMatrix) {
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(matrix) }
        Canvas(bitmap).drawBitmap(bitmap, 0f, 0f, paint)
    }

    private fun applyRiconFlash(bitmap: Bitmap, width: Int, height: Int, intensity: Float) {
        drawWithMatrix(bitmap, blendedMatrix(
            gains = floatArrayOf(1.2f, 1.15f, 1.0f),
            offsets = floatArrayOf(20f, 15f, 0f),
            intensity = intensity
        ))

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            var r = Color.red(pixel)
            var g = Color.green(pixel)
            var b = Color.blue(pixel)

            val lum = 0.299 * r + 0.587 * g + 0.114 * b

            if (lum > 180) {
                r = (r + (255 - r) * 0.15f * intensity).toInt().coerceIn(0, 255)
                g = (g + (255 - g) * 0.12f * intensity).toInt().coerceIn(0, 255)
            }

            if (lum < 60) {
                val lift = (60 - lum) * 0.08f * intensity
                r = (r + lift).toInt().coerceIn(0, 255)
                g = (g + lift * 0.9f).toInt().coerceIn(0, 255)
            }

            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applyFlashFilm(bitmap: Bitmap, width: Int, height: Int, intensity: Float) {
        drawWithMatrix(bitmap, blendedMatrix(
            gains = floatArrayOf(1.2f, 1.08f, 1.05f),
            offsets = floatArrayOf(10f, 5f, 0f),
            intensity = intensity
        ))

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            var r = Color.red(pixel)
            var g = Color.green(pixel)
            var b = Color.blue(pixel)

            val lum = 0.299 * r + 0.587 * g + 0.114 * b

            val contrast = 1.0f + 0.15f * intensity
            r = (((r / 255.0 - 0.5) * contrast + 0.5) * 255).toInt().coerceIn(0, 255)
            g = (((g / 255.0 - 0.5) * contrast + 0.5) * 255).toInt().coerceIn(0, 255)
            b = (((b / 255.0 - 0.5) * contrast + 0.5) * 255).toInt().coerceIn(0, 255)

            if (lum > 200) {
                r = (r + (255 - r) * 0.1f * intensity).toInt().coerceIn(0, 255)
                g = (g + (255 - g) * 0.08f * intensity).toInt().coerceIn(0, 255)
            }

            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applyG7X(bitmap: Bitmap, width: Int, height: Int, intensity: Float) {
        drawWithMatrix(bitmap, blendedMatrix(
            gains = floatArrayOf(1.25f, 1.2f, 1.17f),
            offsets = floatArrayOf(12f, 8f, 10f),
            intensity = intensity
        ))

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            var r = Color.red(pixel)
            var g = Color.green(pixel)
            var b = Color.blue(pixel)

            val lum = 0.299 * r + 0.587 * g + 0.114 * b

            if (lum > 190) {
                val roll = (lum - 190) / 65.0
                r = (r + (255 - r) * roll * 0.12f * intensity).toInt().coerceIn(0, 255)
                g = (g + (255 - g) * roll * 0.10f * intensity).toInt().coerceIn(0, 255)
                b = (b + (255 - b) * roll * 0.08f * intensity).toInt().coerceIn(0, 255)
            }

            if (lum < 50) {
                val lift = (50 - lum) * 0.06f * intensity
                r = (r + lift).toInt().coerceIn(0, 255)
                g = (g + lift * 1.1f).toInt().coerceIn(0, 255)
                b = (b + lift * 1.2f).toInt().coerceIn(0, 255)
            }

            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applyFujiFlash(bitmap: Bitmap, width: Int, height: Int, intensity: Float) {
        drawWithMatrix(bitmap, blendedMatrix(
            gains = floatArrayOf(1.12f, 1.18f, 1.08f),
            offsets = floatArrayOf(8f, 12f, 5f),
            intensity = intensity
        ))

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            var r = Color.red(pixel)
            var g = Color.green(pixel)
            var b = Color.blue(pixel)

            val lum = 0.299 * r + 0.587 * g + 0.114 * b

            if (lum > 180) {
                r = (r + (255 - r) * 0.08f * intensity).toInt().coerceIn(0, 255)
                g = (g + (255 - g) * 0.12f * intensity).toInt().coerceIn(0, 255)
            }

            if (lum < 55) {
                val lift = (55 - lum) * 0.07f * intensity
                r = (r + lift * 1.1f).toInt().coerceIn(0, 255)
                g = (g + lift).toInt().coerceIn(0, 255)
                b = (b + lift * 0.9f).toInt().coerceIn(0, 255)
            }

            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applyGoldenHour(bitmap: Bitmap, width: Int, height: Int, intensity: Float) {
        drawWithMatrix(bitmap, blendedMatrix(
            gains = floatArrayOf(1.35f, 1.15f, 0.9f),
            offsets = floatArrayOf(25f, 15f, -10f),
            intensity = intensity
        ))

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            var r = Color.red(pixel)
            var g = Color.green(pixel)
            var b = Color.blue(pixel)

            val lum = 0.299 * r + 0.587 * g + 0.114 * b

            if (lum > 150) {
                val warmth = (lum - 150) / 105.0
                r = (r + warmth * 15 * intensity).toInt().coerceIn(0, 255)
                g = (g + warmth * 8 * intensity).toInt().coerceIn(0, 255)
                b = (b - warmth * 10 * intensity).toInt().coerceIn(0, 255)
            }

            if (lum < 70) {
                val lift = (70 - lum) * 0.06f * intensity
                r = (r + lift * 1.2f).toInt().coerceIn(0, 255)
                g = (g + lift * 0.9f).toInt().coerceIn(0, 255)
                b = (b + lift * 0.7f).toInt().coerceIn(0, 255)
            }

            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applyMatahariTerbenam(bitmap: Bitmap, width: Int, height: Int, intensity: Float) {
        drawWithMatrix(bitmap, blendedMatrix(
            gains = floatArrayOf(1.45f, 1.05f, 0.88f),
            offsets = floatArrayOf(30f, 10f, -15f),
            intensity = intensity
        ))

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            var r = Color.red(pixel)
            var g = Color.green(pixel)
            var b = Color.blue(pixel)

            val lum = 0.299 * r + 0.587 * g + 0.114 * b

            if (lum > 130) {
                val warmth = (lum - 130) / 125.0
                r = (r + warmth * 20 * intensity).toInt().coerceIn(0, 255)
                g = (g + warmth * 10 * intensity).toInt().coerceIn(0, 255)
                b = (b - warmth * 15 * intensity).toInt().coerceIn(0, 255)
            }

            if (lum < 60) {
                val lift = (60 - lum) * 0.05f * intensity
                r = (r + lift * 1.3f).toInt().coerceIn(0, 255)
                g = (g + lift * 0.8f).toInt().coerceIn(0, 255)
                b = (b + lift * 0.6f).toInt().coerceIn(0, 255)
            }

            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        if (intensity > 0.3f) {
            applyVignette(bitmap, width, height, 0.25f * intensity)
        }
    }

    private fun applyLampuKilatIphone(bitmap: Bitmap, width: Int, height: Int, intensity: Float) {
        drawWithMatrix(bitmap, blendedMatrix(
            gains = floatArrayOf(1.18f, 1.16f, 1.15f),
            offsets = floatArrayOf(15f, 12f, 8f),
            intensity = intensity
        ))

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            var r = Color.red(pixel)
            var g = Color.green(pixel)
            var b = Color.blue(pixel)

            val lum = 0.299 * r + 0.587 * g + 0.114 * b

            if (lum > 200) {
                r = (r + (255 - r) * 0.1f * intensity).toInt().coerceIn(0, 255)
                g = (g + (255 - g) * 0.09f * intensity).toInt().coerceIn(0, 255)
                b = (b + (255 - b) * 0.08f * intensity).toInt().coerceIn(0, 255)
            }

            if (lum < 40) {
                val lift = (40 - lum) * 0.07f * intensity
                r = (r + lift).toInt().coerceIn(0, 255)
                g = (g + lift).toInt().coerceIn(0, 255)
                b = (b + lift * 1.1f).toInt().coerceIn(0, 255)
            }

            pixels[i] = Color.argb(Color.alpha(pixel), r, g, b)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun applyVignette(bitmap: Bitmap, width: Int, height: Int, strength: Float) {
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val centerX = width / 2f
        val centerY = height / 2f

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
}

enum class FilterType(val displayName: String) {
    ORIGINAL("Original"),
    RICON_FLASH("Ricon Flash"),
    FLASH_FILM("Flash Film"),
    G7X("G7X"),
    FUJI_FLASH("Fuji Flash"),
    GOLDEN_HOUR("Golden Hour"),
    MATAHARI_TERBENAM("Matahari Terbenam"),
    LAMPU_KILAT_IPHONE("Lampu Kilat iPhone")
}
