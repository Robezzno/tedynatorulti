package com.puffyai.puffyai.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

class ImageProcessor {

    /**
     * Scales down a Bitmap to a target width while maintaining aspect ratio.
     * This helps in reducing memory usage and improving performance for large images.
     *
     * @param bitmap The original Bitmap to scale.
     * @param targetWidth The desired width for the scaled bitmap.
     * @return The scaled Bitmap.
     */
    fun scaleBitmap(bitmap: Bitmap, targetWidth: Int): Bitmap {
        val aspectRatio = bitmap.width.toDouble() / bitmap.height.toDouble()
        val targetHeight = (targetWidth / aspectRatio).toInt()
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    /**
     * Compresses a Bitmap to a ByteArray.
     *
     * @param bitmap The Bitmap to compress.
     * @param format The compression format (e.g., Bitmap.CompressFormat.PNG, Bitmap.CompressFormat.JPEG).
     * @param quality The compression quality (0-100).
     * @return The compressed image as a ByteArray.
     */
    fun compressBitmapToByteArray(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    /**
     * Decodes a ByteArray into a Bitmap.
     *
     * @param byteArray The ByteArray to decode.
     * @return The decoded Bitmap, or null if decoding fails.
     */
    fun decodeByteArrayToBitmap(byteArray: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}