package com.aibeautypro.model

import java.util.Locale

data class BeautyData(
    val faceWidth: Float,
    val faceHeight: Float,
    val faceRatio: Float,
    val leftEyeWidth: Float,
    val rightEyeWidth: Float,
    val eyeDistance: Float,
    val noseWidth: Float,
    val mouthWidth: Float,
    val jawWidth: Float,
    val symmetry: Float
) {
    private fun f(value: Float, digits: Int = 2): String =
        String.format(Locale.getDefault(), "%.${digits}f", value)

    fun asReadableText(): String = buildString {
        appendLine("脸宽高比：${f(faceRatio)}")
        appendLine("眼间距/脸宽：${f(eyeDistance / faceWidth.coerceAtLeast(0.001f))}")
        appendLine("鼻宽/脸宽：${f(noseWidth / faceWidth.coerceAtLeast(0.001f))}")
        appendLine("嘴宽/脸宽：${f(mouthWidth / faceWidth.coerceAtLeast(0.001f))}")
        appendLine("下颌宽/脸宽：${f(jawWidth / faceWidth.coerceAtLeast(0.001f))}")
        append("左右对称度：${f(symmetry * 100f, 1)}%")
    }
}
