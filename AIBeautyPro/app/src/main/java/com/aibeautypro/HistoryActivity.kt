package com.aibeautypro

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.aibeautypro.adapter.HistoryAdapter
import com.aibeautypro.database.AppDatabase
import com.aibeautypro.databinding.ActivityHistoryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = HistoryAdapter { record ->
            startActivity(
                Intent(this, RecordDetailActivity::class.java)
                    .putExtra(RecordDetailActivity.EXTRA_RECORD_ID, record.id)
            )
        }
        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistory.adapter = adapter
        binding.btnBack.setOnClickListener { finish() }
        binding.btnClearHistory.setOnClickListener { confirmClear() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppDatabase.get(this@HistoryActivity)
                    .beautyDao()
                    .observeAll()
                    .collect { records ->
                        adapter.submitList(records)
                        binding.emptyState.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
                        binding.recyclerHistory.visibility = if (records.isEmpty()) View.GONE else View.VISIBLE
                        binding.btnClearHistory.isEnabled = records.isNotEmpty()
                    }
            }
        }
    }

    private fun confirmClear() {
        MaterialAlertDialogBuilder(this)
            .setTitle("清空历史记录？")
            .setMessage("此操作只删除数据库记录，不会删除已保存到系统相册的图片。")
            .setNegativeButton("取消", null)
            .setPositiveButton("清空") { _, _ ->
                lifecycleScope.launch {
                    AppDatabase.get(this@HistoryActivity).beautyDao().clearAll()
                }
            }
            .show()
    }

    override fun onDestroy() {
        adapter.close()
        super.onDestroy()
    }
}
