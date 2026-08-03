package com.aibeautypro

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aibeautypro.ai.FaceAnalyzer
import com.aibeautypro.ai.FaceShapeAI
import com.aibeautypro.ai.HairAI
import com.aibeautypro.ai.HairLibrary
import com.aibeautypro.ai.RealFaceDetector
import com.aibeautypro.ai.ScoreEngine
import com.aibeautypro.database.AppDatabase
import com.aibeautypro.database.BeautyRecord
import com.aibeautypro.databinding.ActivityResultBinding
import com.aibeautypro.model.BeautyReport
import com.aibeautypro.report.PdfGenerator
import com.aibeautypro.utils.BitmapUtils
import com.aibeautypro.utils.ShareUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var imagePath: String
    private var sourceBitmap: Bitmap? = null
    private var report: BeautyReport? = null
    private var recordId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH).orEmpty()
        if (imagePath.isBlank()) {
            Toast.makeText(this, "没有可分析的图片", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnHair.setOnClickListener { openHair() }
        binding.btnPdf.setOnClickListener { createPdf() }
        analyze()
    }

    private fun analyze() {
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val bitmap = BitmapUtils.load(this@ResultActivity, imagePath)
                    val landmarks = RealFaceDetector(this@ResultActivity).use { detector ->
                        detector.detect(bitmap)
                    } ?: error("未检测到清晰正脸，请更换光线均匀、无遮挡的照片")

                    val metrics = FaceAnalyzer.analyze(landmarks)
                    val faceShape = FaceShapeAI.classify(metrics)
                    val score = ScoreEngine.score(metrics)
                    val recommendedIds = HairAI.recommendIds(faceShape)
                    val recommendedNames = recommendedIds.map { HairLibrary.byId(it).name }
                    val beautyReport = BeautyReport(
                        imagePath = imagePath,
                        score = score,
                        faceShape = faceShape,
                        summary = ScoreEngine.summary(score, faceShape, metrics),
                        metrics = metrics,
                        hairRecommendations = recommendedNames
                    )

                    val id = AppDatabase.get(this@ResultActivity).beautyDao().insert(
                        BeautyRecord(
                            createdAt = beautyReport.createdAt,
                            imagePath = imagePath,
                            score = score,
                            faceShape = faceShape,
                            summary = beautyReport.summary,
                            metricsText = metrics.asReadableText(),
                            hairRecommendations = recommendedNames.joinToString("、")
                        )
                    )
                    Triple(bitmap, beautyReport, id)
                }
            }

            result.onSuccess { (bitmap, beautyReport, id) ->
                sourceBitmap = bitmap
                report = beautyReport
                recordId = id
                showReport(bitmap, beautyReport)
            }.onFailure {
                binding.progress.visibility = View.GONE
                binding.tvStatus.text = it.message ?: "分析失败"
                Toast.makeText(
                    this@ResultActivity,
                    it.message ?: "分析失败",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showReport(bitmap: Bitmap, report: BeautyReport) {
        binding.imageFace.setImageBitmap(bitmap)
        binding.progress.visibility = View.GONE
        binding.tvStatus.visibility = View.GONE
        binding.tvScore.text = report.score.toString()
        binding.tvFaceShape.text = "脸型：${report.faceShape}"
        binding.tvSummary.text = report.summary
        binding.tvMetrics.text = buildString {
            append(report.metrics.asReadableText())
            append("\n\n推荐发型：")
            append(report.hairRecommendations.joinToString("、"))
        }
        binding.btnHair.isEnabled = true
        binding.btnPdf.isEnabled = true
    }

    private fun openHair() {
        val currentReport = report ?: return
        startActivity(
            Intent(this, HairActivity::class.java)
                .putExtra(HairActivity.EXTRA_IMAGE_PATH, imagePath)
                .putExtra(HairActivity.EXTRA_FACE_SHAPE, currentReport.faceShape)
                .putExtra(HairActivity.EXTRA_RECORD_ID, recordId)
        )
    }

    private fun createPdf() {
        val currentReport = report ?: return
        val bitmap = sourceBitmap ?: return
        binding.btnPdf.isEnabled = false
        binding.btnPdf.text = "正在生成…"
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    PdfGenerator.generate(this@ResultActivity, currentReport, bitmap).also { path ->
                        if (recordId > 0) {
                            AppDatabase.get(this@ResultActivity)
                                .beautyDao()
                                .updatePdfPath(recordId, path)
                        }
                    }
                }
            }
            binding.btnPdf.isEnabled = true
            binding.btnPdf.text = "生成 PDF 报告"
            result.onSuccess { path ->
                Toast.makeText(this@ResultActivity, "PDF 已生成", Toast.LENGTH_SHORT).show()
                runCatching {
                    ShareUtils.openFile(this@ResultActivity, path, "application/pdf")
                }.onFailure {
                    Toast.makeText(this@ResultActivity, "PDF 已保存：$path", Toast.LENGTH_LONG).show()
                }
            }.onFailure {
                Toast.makeText(
                    this@ResultActivity,
                    "PDF 生成失败：${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        const val EXTRA_IMAGE_PATH = "image_path"
    }
}
