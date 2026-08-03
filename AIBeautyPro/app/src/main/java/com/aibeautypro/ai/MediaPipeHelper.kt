package com.aibeautypro.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import java.io.Closeable

class MediaPipeHelper(context: Context) : Closeable {
    private val faceLandmarker: FaceLandmarker

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(MODEL_NAME)
            .build()

        val options = FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setNumFaces(1)
            .setMinFaceDetectionConfidence(0.55f)
            .setMinFacePresenceConfidence(0.55f)
            .setMinTrackingConfidence(0.5f)
            .setOutputFaceBlendshapes(false)
            .setOutputFacialTransformationMatrixes(false)
            .build()

        faceLandmarker = FaceLandmarker.createFromOptions(
            context.applicationContext,
            options
        )
    }

    fun detect(bitmap: Bitmap): List<PointF>? {
        val safeBitmap = if (bitmap.config == Bitmap.Config.ARGB_8888) {
            bitmap
        } else {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        }
        val image = BitmapImageBuilder(safeBitmap).build()
        return try {
            val result = faceLandmarker.detect(image)
            val face = result.faceLandmarks().firstOrNull() ?: return null
            face.map { landmark -> PointF(landmark.x(), landmark.y()) }
        } finally {
            image.close()
            if (safeBitmap !== bitmap && !safeBitmap.isRecycled) safeBitmap.recycle()
        }
    }

    override fun close() {
        faceLandmarker.close()
    }

    companion object {
        private const val MODEL_NAME = "face_landmarker.task"
    }
}
