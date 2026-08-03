package com.aibeautypro.ai

import com.aibeautypro.model.BeautyData
import kotlin.math.abs

object ScoreEngine {
    private fun closeness(value: Float, target: Float, tolerance: Float): Float =
        (1f - abs(value - target) / tolerance).coerceIn(0f, 1f)

    fun score(data: BeautyData): Int {
        val eyeBalance = closeness(
            data.leftEyeWidth / data.rightEyeWidth.coerceAtLeast(0.001f),
            1f,
            0.22f
        )
        val eyeSpacing = closeness(data.eyeDistance / data.faceWidth, 0.25f, 0.12f)
        val nose = closeness(data.noseWidth / data.faceWidth, 0.22f, 0.11f)
        val mouth = closeness(data.mouthWidth / data.faceWidth, 0.38f, 0.16f)
        val face = closeness(data.faceRatio, 0.76f, 0.22f)

        val normalized =
            eyeBalance * 0.18f +
                eyeSpacing * 0.18f +
                nose * 0.14f +
                mouth * 0.14f +
                face * 0.16f +
                data.symmetry * 0.20f

        return (55 + normalized * 44).toInt().coerceIn(55, 99)
    }

    fun summary(score: Int, shape: String, data: BeautyData): String {
        val symmetryText = when {
            data.symmetry >= 0.94f -> "面部对称性较高"
            data.symmetry >= 0.88f -> "面部对称性自然"
            else -> "左右细节存在自然差异"
        }
        val scoreText = when {
            score >= 90 -> "比例协调度突出"
            score >= 82 -> "整体比例较协调"
            score >= 72 -> "五官比例自然"
            else -> "具有鲜明的个人特征"
        }
        return "$shape，$scoreText，$symmetryText。"
    }
}
