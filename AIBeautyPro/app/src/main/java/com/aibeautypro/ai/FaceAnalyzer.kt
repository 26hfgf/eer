package com.aibeautypro.ai

import android.graphics.PointF
import com.aibeautypro.model.BeautyData
import kotlin.math.abs
import kotlin.math.hypot

object FaceAnalyzer {
    private fun distance(a: PointF, b: PointF): Float =
        hypot((a.x - b.x).toDouble(), (a.y - b.y).toDouble()).toFloat()

    fun analyze(points: List<PointF>): BeautyData {
        require(points.size >= 468) { "人脸关键点数量不足" }

        val faceWidth = distance(points[234], points[454]).coerceAtLeast(0.001f)
        val faceHeight = distance(points[10], points[152]).coerceAtLeast(0.001f)
        val leftEye = distance(points[33], points[133])
        val rightEye = distance(points[362], points[263])
        val eyeDistance = distance(points[133], points[362])
        val noseWidth = distance(points[98], points[327])
        val mouthWidth = distance(points[61], points[291])
        val jawWidth = distance(points[172], points[397])

        val nose = points[1]
        val leftDistances = listOf(
            distance(points[33], nose),
            distance(points[61], nose),
            distance(points[234], nose)
        )
        val rightDistances = listOf(
            distance(points[263], nose),
            distance(points[291], nose),
            distance(points[454], nose)
        )
        val asymmetry = leftDistances.zip(rightDistances).map { (left, right) ->
            abs(left - right) / ((left + right) / 2f).coerceAtLeast(0.001f)
        }.average().toFloat()

        return BeautyData(
            faceWidth = faceWidth,
            faceHeight = faceHeight,
            faceRatio = faceWidth / faceHeight,
            leftEyeWidth = leftEye,
            rightEyeWidth = rightEye,
            eyeDistance = eyeDistance,
            noseWidth = noseWidth,
            mouthWidth = mouthWidth,
            jawWidth = jawWidth,
            symmetry = (1f - asymmetry).coerceIn(0f, 1f)
        )
    }
}
