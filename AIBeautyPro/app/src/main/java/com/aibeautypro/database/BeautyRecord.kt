package com.aibeautypro.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "beauty_records")
data class BeautyRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val imagePath: String,
    val score: Int,
    val faceShape: String,
    val summary: String,
    val metricsText: String,
    val hairRecommendations: String,
    val hairImagePath: String? = null,
    val pdfPath: String? = null
)
