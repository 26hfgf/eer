package com.aibeautypro.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileInputStream

object BitmapUtils {
    fun load(context: Context, source: String, maxSide: Int = 1800): Bitmap {
        val bytes = openBytes(context, source)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "图片格式无法识别" }

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: error("图片解码失败")

        val orientation = runCatching {
            ExifInterface(bytes.inputStream()).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    postRotate(90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    postRotate(-90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(-90f)
            }
        }

        if (matrix.isIdentity) return bitmap
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        ).also {
            if (it !== bitmap) bitmap.recycle()
        }
    }

    fun loadThumbnail(context: Context, source: String, maxSide: Int = 360): Bitmap? =
        runCatching { load(context, source, maxSide) }.getOrNull()

    private fun openBytes(context: Context, source: String): ByteArray {
        val uri = runCatching { Uri.parse(source) }.getOrNull()
        return when {
            source.startsWith("content://") && uri != null ->
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("无法读取图片")
            source.startsWith("file://") && uri?.path != null ->
                FileInputStream(File(requireNotNull(uri.path))).use { it.readBytes() }
            else -> File(source).inputStream().use { it.readBytes() }
        }
    }
}
