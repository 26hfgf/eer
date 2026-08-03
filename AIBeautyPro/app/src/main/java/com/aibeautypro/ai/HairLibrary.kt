package com.aibeautypro.ai

import android.graphics.Color
import com.aibeautypro.model.HairStyle
import com.aibeautypro.model.HairTemplate

object HairLibrary {
    val all: List<HairTemplate> = listOf(
        HairTemplate(
            id = "long_wave",
            name = "长卷发",
            style = HairStyle.LONG_WAVE,
            suitableFaceShapes = setOf("圆脸", "方脸", "心形脸", "鹅蛋脸", "椭圆脸"),
            widthScale = 1.55f,
            heightScale = 1.75f,
            topOffset = 0.38f,
            baseColor = Color.rgb(54, 31, 27),
            highlightColor = Color.rgb(126, 79, 58),
            description = "纵向线条拉长轮廓，柔和两侧边缘。"
        ),
        HairTemplate(
            id = "soft_bob",
            name = "柔和波波头",
            style = HairStyle.SOFT_BOB,
            suitableFaceShapes = setOf("方脸", "长脸", "心形脸"),
            widthScale = 1.42f,
            heightScale = 1.25f,
            topOffset = 0.42f,
            baseColor = Color.rgb(35, 25, 31),
            highlightColor = Color.rgb(101, 70, 81),
            description = "下颌附近的弧线能弱化硬朗轮廓。"
        ),
        HairTemplate(
            id = "side_part",
            name = "侧分层次",
            style = HairStyle.SIDE_PART,
            suitableFaceShapes = setOf("圆脸", "方脸", "鹅蛋脸", "椭圆脸"),
            widthScale = 1.48f,
            heightScale = 1.55f,
            topOffset = 0.40f,
            baseColor = Color.rgb(41, 28, 19),
            highlightColor = Color.rgb(140, 93, 52),
            description = "不对称分缝增加纵向感与层次感。"
        ),
        HairTemplate(
            id = "air_bangs",
            name = "空气刘海",
            style = HairStyle.AIR_BANGS,
            suitableFaceShapes = setOf("长脸", "心形脸", "鹅蛋脸", "椭圆脸"),
            widthScale = 1.38f,
            heightScale = 1.35f,
            topOffset = 0.44f,
            baseColor = Color.rgb(47, 30, 24),
            highlightColor = Color.rgb(119, 75, 53),
            description = "轻薄刘海缩短视觉脸长，保留通透感。"
        ),
        HairTemplate(
            id = "high_bob",
            name = "高层次短发",
            style = HairStyle.HIGH_BOB,
            suitableFaceShapes = setOf("圆脸", "长脸", "方脸"),
            widthScale = 1.36f,
            heightScale = 1.10f,
            topOffset = 0.48f,
            baseColor = Color.rgb(31, 27, 25),
            highlightColor = Color.rgb(111, 92, 77),
            description = "顶部蓬松提升重心，短侧边更利落。"
        )
    )

    fun byId(id: String): HairTemplate = all.firstOrNull { it.id == id } ?: all.first()

    fun recommended(faceShape: String): List<HairTemplate> =
        HairAI.recommendIds(faceShape).map(::byId)
}
