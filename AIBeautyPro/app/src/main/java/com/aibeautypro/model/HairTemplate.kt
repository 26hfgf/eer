package com.aibeautypro.model

enum class HairStyle {
    LONG_WAVE,
    SOFT_BOB,
    SIDE_PART,
    AIR_BANGS,
    HIGH_BOB
}

data class HairTemplate(
    val id: String,
    val name: String,
    val style: HairStyle,
    val suitableFaceShapes: Set<String>,
    val widthScale: Float,
    val heightScale: Float,
    val topOffset: Float,
    val baseColor: Int,
    val highlightColor: Int,
    val description: String
)
