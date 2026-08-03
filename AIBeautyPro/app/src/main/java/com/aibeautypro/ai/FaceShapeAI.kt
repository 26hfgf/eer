package com.aibeautypro.ai

import com.aibeautypro.model.BeautyData

object FaceShapeAI {
    fun classify(data: BeautyData): String {
        val ratio = data.faceRatio
        val jawRatio = data.jawWidth / data.faceWidth.coerceAtLeast(0.001f)
        val cheekToJaw = data.faceWidth / data.jawWidth.coerceAtLeast(0.001f)

        return when {
            ratio < 0.68f -> "长脸"
            ratio > 0.84f && jawRatio > 0.76f -> "方脸"
            ratio > 0.86f -> "圆脸"
            jawRatio < 0.58f && cheekToJaw > 1.55f -> "心形脸"
            jawRatio < 0.66f -> "鹅蛋脸"
            else -> "椭圆脸"
        }
    }
}
