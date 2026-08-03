package com.aibeautypro.ai

object HairAI {
    fun recommendIds(faceShape: String): List<String> = when (faceShape) {
        "圆脸" -> listOf("long_wave", "side_part", "high_bob")
        "方脸" -> listOf("long_wave", "soft_bob", "side_part")
        "长脸" -> listOf("soft_bob", "air_bangs", "high_bob")
        "心形脸" -> listOf("soft_bob", "long_wave", "air_bangs")
        "鹅蛋脸", "椭圆脸" -> listOf("air_bangs", "side_part", "long_wave")
        else -> listOf("long_wave", "soft_bob", "air_bangs")
    }
}
