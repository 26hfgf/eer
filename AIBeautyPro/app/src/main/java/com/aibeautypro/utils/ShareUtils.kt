package com.aibeautypro.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ShareUtils {
    fun openFile(context: Context, pathOrUri: String, mimeType: String) {
        val uri = resolveUri(context, pathOrUri)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "打开文件"))
    }

    fun shareFile(context: Context, pathOrUri: String, mimeType: String) {
        val uri = resolveUri(context, pathOrUri)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享"))
    }

    private fun resolveUri(context: Context, pathOrUri: String): Uri {
        if (pathOrUri.startsWith("content://")) return Uri.parse(pathOrUri)
        val file = if (pathOrUri.startsWith("file://")) {
            File(requireNotNull(Uri.parse(pathOrUri).path))
        } else {
            File(pathOrUri)
        }
        require(file.exists()) { "文件不存在" }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
