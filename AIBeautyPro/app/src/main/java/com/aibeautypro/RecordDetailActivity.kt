package com.aibeautypro

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aibeautypro.database.AppDatabase
import com.aibeautypro.database.BeautyRecord
import com.aibeautypro.databinding.ActivityRecordDetailBinding
import com.aibeautypro.utils.BitmapUtils
import com.aibeautypro.utils.ShareUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordDetailBinding
    private var record: BeautyRecord? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnOpenPdf.setOnClickListener { openPdf() }
        binding.btnShareRecord.setOnClickListener { shareImage() }
        binding.btnDeleteRecord.setOnClickListener { confirmDelete() }

        val id = intent.getLongExtra(EXTRA_RECORD_ID, 0)
        if (id <= 0) {
            finish()
            return
        }
        loadRecord(id)
    }

    private fun loadRecord(id: Long) {
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val item = AppDatabase.get(this@RecordDetailActivity)
                        .beautyDao()
                        .getById(id)
                        ?: error("记录不存在")
                    val imageSource = item.hairImagePath ?: item.imagePath
                    val bitmap = BitmapUtils.load(this@RecordDetailActivity, imageSource)
                    item to bitmap
                }
            }
            result.onSuccess { (item, bitmap) ->
                record = item
                binding.progressDetail.visibility = View.GONE
                binding.contentDetail.visibility = View.VISIBLE
                binding.imageRecord.setImageBitmap(bitmap)
                binding.tvRecordScore.text = item.score.toString()
                binding.tvRecordShape.text = item.faceShape
                binding.tvRecordDate.text = dateFormat.format(Date(item.createdAt))
                binding.tvRecordSummary.text = item.summary
                binding.tvRecordMetrics.text = buildString {
                    append(item.metricsText)
                    append("\n\n推荐发型：")
                    append(item.hairRecommendations)
                }
                binding.btnOpenPdf.isEnabled = !item.pdfPath.isNullOrBlank()
                binding.btnShareRecord.isEnabled = true
                binding.btnDeleteRecord.isEnabled = true
            }.onFailure {
                Toast.makeText(this@RecordDetailActivity, it.message ?: "加载失败", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun openPdf() {
        val path = record?.pdfPath ?: return
        runCatching { ShareUtils.openFile(this, path, "application/pdf") }
            .onFailure {
                Toast.makeText(this, "无法打开 PDF：${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun shareImage() {
        val item = record ?: return
        val source = item.hairImagePath ?: item.imagePath
        runCatching { ShareUtils.shareFile(this, source, "image/jpeg") }
            .onFailure {
                Toast.makeText(this, "分享失败：${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun confirmDelete() {
        val item = record ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle("删除这条记录？")
            .setMessage("数据库中的分析记录将被删除。")
            .setNegativeButton("取消", null)
            .setPositiveButton("删除") { _, _ ->
                lifecycleScope.launch {
                    AppDatabase.get(this@RecordDetailActivity).beautyDao().delete(item)
                    finish()
                }
            }
            .show()
    }

    companion object {
        const val EXTRA_RECORD_ID = "record_id"
    }
}
