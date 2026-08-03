package com.aibeautypro.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF

class RealFaceDetector(context: Context) : AutoCloseable {
    private val helper = MediaPipeHelper(context)

    fun detect(bitmap: Bitmap): List<PointF>? = helper.detect(bitmap)

    override fun close() {
        helper.close()
    }
}
