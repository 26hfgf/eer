package com.aibeautypro

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aibeautypro.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@registerForActivityResult
            lifecycleScope.launch {
                binding.btnGallery.isEnabled = false
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        val dir = File(
                            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "AIBeautyPro"
                        ).apply { mkdirs() }
                        val file = File(dir, "Gallery_${System.currentTimeMillis()}.jpg")
                        contentResolver.openInputStream(uri).use { input ->
                            requireNotNull(input) { "无法读取所选图片" }
                            file.outputStream().use { output -> input.copyTo(output) }
                        }
                        file.absolutePath
                    }
                }
                binding.btnGallery.isEnabled = true
                result.onSuccess(::openResult).onFailure {
                    Toast.makeText(
                        this@MainActivity,
                        it.message ?: "图片读取失败",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCamera.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        binding.btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun openResult(path: String) {
        startActivity(
            Intent(this, ResultActivity::class.java)
                .putExtra(ResultActivity.EXTRA_IMAGE_PATH, path)
        )
    }
}
