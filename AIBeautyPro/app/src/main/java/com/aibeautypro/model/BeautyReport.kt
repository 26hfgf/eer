package com.aibeautypro.model

data class BeautyReport(
    val imagePath: String,
    val score: Int,
    val faceShape: String,
    val summary: String,
    val metrics: BeautyData,
    val hairRecommendations: List<String>,
    val createdAt: Long = System.currentTimeMillis()
)
