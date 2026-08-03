package com.aibeautypro

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aibeautypro.adapter.HairTemplateAdapter
import com.aibeautypro.ai.HairAI
import com.aibeautypro.ai.HairLibrary
import com.aibeautypro.ai.HairRenderer
import com.aibeautypro.ai.RealFaceDetector
import com.aibeautypro.database.AppDatabase
import com.aibeautypro.databinding.ActivityHairBinding
import com.aibeautypro.model.HairTemplate
import com.aibeautypro.utils.BitmapUtils
import com.aibeautypro.utils.ImageSaver
import com.aibeautypro.utils.ShareUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HairActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHairBinding
    private lateinit var imagePath: String
    private lateinit var faceShape: String
    private var recordId: Long = 0
    private var sourceBitmap: Bitmap? = null
    private var renderedBitmap: Bitmap? = null
    private var landmarks: List<PointF>? = null
    private var selectedTemplate: HairTemplate? = null
    private var savedPath: String? = null
    private lateinit var adapter: HairTemplateAdapter
    private var pendingGallerySave = false

    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted && pendingGallerySave) saveCurrentToGallery()
            pendingGallerySave = false
            if (!granted) {
                Toast.makeText(this, "未授予存储权限，无法保存到系统相册", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHairBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH).orEmpty()
        faceShape = intent.getStringExtra(EXTRA_FACE_SHAPE).orEmpty().ifBlank { "椭圆脸" }
        recordId = intent.getLongExtra(EXTRA_RECORD_ID, 0)

        if (imagePath.isBlank()) {
            Toast.makeText(this, "没有可试戴的图片", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSaveHair.setOnClickListener { requestGallerySave() }
        binding.btnShareHair.setOnClickListener { shareCurrent() }
        binding.tvFaceShape.text = "当前脸型：$faceShape"

        val recommendedIds = HairAI.recommendIds(faceShape).toSet()
        adapter = HairTemplateAdapter(HairLibrary.all, recommendedIds, ::renderTemplate)
        binding.recyclerHair.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.recyclerHair.adapter = adapter
        loadFace()
    }

    private fun loadFace() {
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val bitmap = BitmapUtils.load(this@HairActivity, imagePath)
                    val points = RealFaceDetector(this@HairActivity).use { detector ->
                        detector.detect(bitmap)
                    } ?: error("未检测到清晰正脸，无法定位发型")
                    bitmap to points
                }
            }
            result.onSuccess { (bitmap, points) ->
                sourceBitmap = bitmap
                landmarks = points
                binding.imageHairPreview.setImageBitmap(bitmap)
                binding.progressHair.visibility = View.GONE
                binding.tvHairStatus.visibility = View.GONE
                binding.btnSaveHair.isEnabled = true
                binding.btnShareHair.isEnabled = true

                val initial = HairLibrary.recommended(faceShape).firstOrNull() ?: HairLibrary.all.first()
                adapter.select(initial.id)
                renderTemplate(initial)
            }.onFailure {
                binding.progressHair.visibility = View.GONE
                binding.tvHairStatus.text = it.message ?: "加载失败"
                Toast.makeText(this@HairActivity, it.message ?: "加载失败", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun renderTemplate(template: HairTemplate) {
        val bitmap = sourceBitmap ?: return
        val points = landmarks ?: return
        selectedTemplate = template
        savedPath = null
        binding.progressHair.visibility = View.VISIBLE
        binding.tvHairStatus.visibility = View.VISIBLE
        binding.tvHairStatus.text = "正在融合 ${template.name}…"
        binding.tvHairDescription.text = template.description

        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.Default) {
                    HairRenderer.render(bitmap, points, template)
                }
            }
            binding.progressHair.visibility = View.GONE
            binding.tvHairStatus.visibility = View.GONE
            result.onSuccess { rendered ->
                if (selectedTemplate?.id == template.id) {
                    renderedBitmap?.takeIf { it !== sourceBitmap && !it.isRecycled }?.recycle()
                    renderedBitmap = rendered
                    binding.imageHairPreview.setImageBitmap(rendered)
                } else if (!rendered.isRecycled) {
                    rendered.recycle()
                }
            }.onFailure {
                Toast.makeText(
                    this@HairActivity,
                    "发型融合失败：${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun requestGallerySave() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            pendingGallerySave = true
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        saveCurrentToGallery()
    }

    private fun saveCurrentToGallery() {
        val bitmap = renderedBitmap ?: sourceBitmap ?: return
        binding.btnSaveHair.isEnabled = false
        binding.btnSaveHair.text = "正在保存…"
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    ImageSaver.saveToGallery(this@HairActivity, bitmap, "HairTryOn").also { path ->
                        if (recordId > 0) {
                            AppDatabase.get(this@HairActivity)
                                .beautyDao()
                                .updateHairPath(recordId, path)
                        }
                    }
                }
            }
            binding.btnSaveHair.isEnabled = true
            binding.btnSaveHair.text = "保存到相册"
            result.onSuccess { path ->
                savedPath = path
                Toast.makeText(this@HairActivity, "已保存到相册", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(
                    this@HairActivity,
                    "保存失败：${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun shareCurrent() {
        val bitmap = renderedBitmap ?: sourceBitmap ?: return
        lifecycleScope.launch {
            val result = runCatching {
                val path = savedPath ?: withContext(Dispatchers.IO) {
                    ImageSaver.savePrivate(this@HairActivity, bitmap, "HairShare")
                }
                ShareUtils.shareFile(this@HairActivity, path, "image/jpeg")
            }
            result.onFailure {
                Toast.makeText(
                    this@HairActivity,
                    "分享失败：${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        renderedBitmap?.takeIf { it !== sourceBitmap && !it.isRecycled }?.recycle()
        sourceBitmap?.takeIf { !it.isRecycled }?.recycle()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_IMAGE_PATH = "image_path"
        const val EXTRA_FACE_SHAPE = "face_shape"
        const val EXTRA_RECORD_ID = "record_id"
    }
}
