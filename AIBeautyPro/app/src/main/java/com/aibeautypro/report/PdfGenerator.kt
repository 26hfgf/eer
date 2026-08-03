package com.aibeautypro.report

import android.graphics.Path
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.aibeautypro.model.BeautyReport
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

object PdfGenerator {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 42f

    fun generate(context: Context, report: BeautyReport, bitmap: Bitmap): String {
        val document = PdfDocument()
        try {
            drawCover(document, report, bitmap)
            drawDetails(document, report)

            val dir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "AIBeautyPro"
            ).apply { mkdirs() }
            val file = File(dir, "BeautyReport_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { output -> document.writeTo(output) }
            return file.absolutePath
        } finally {
            document.close()
        }
    }

    private fun drawCover(document: PdfDocument, report: BeautyReport, bitmap: Bitmap) {
        val page = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        )
        val canvas = page.canvas
        canvas.drawColor(Color.rgb(255, 248, 252))

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(36, 27, 47)
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(90, 80, 100)
            textSize = 14f
        }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(124, 77, 255)
            textSize = 50f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText("AI颜值大师 Pro", MARGIN, 68f, titlePaint)
        canvas.drawText("面部比例分析报告", MARGIN, 96f, normalPaint)

        val imageRect = fitRect(bitmap.width, bitmap.height, MARGIN, 126f, 511f, 430f)
        canvas.drawRoundRect(imageRect, 18f, 18f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        })
        canvas.save()
        canvas.clipPath(Path().apply {
    addRoundRect(imageRect, 18f, 18f, Path.Direction.CW)
})
        canvas.drawBitmap(bitmap, null, imageRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        canvas.restore()

        canvas.drawText(report.score.toString(), PAGE_WIDTH / 2f, 622f, accentPaint)
        val centered = Paint(normalPaint).apply {
            textAlign = Paint.Align.CENTER
            textSize = 18f
            color = Color.rgb(36, 27, 47)
        }
        canvas.drawText("${report.faceShape} · 娱乐性比例评分", PAGE_WIDTH / 2f, 654f, centered)

        val body = Paint(normalPaint).apply { textSize = 13f }
        drawWrappedText(
            canvas = canvas,
            text = report.summary,
            x = MARGIN,
            y = 700f,
            maxWidth = PAGE_WIDTH - MARGIN * 2,
            lineHeight = 21f,
            paint = body
        )

        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(report.createdAt))
        canvas.drawText("生成时间：$date", MARGIN, 790f, normalPaint)
        canvas.drawText("声明：本报告仅分析几何比例，结果只供娱乐参考。", MARGIN, 814f, normalPaint)
        document.finishPage(page)
    }

    private fun drawDetails(document: PdfDocument, report: BeautyReport) {
        val page = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
        )
        val canvas = page.canvas
        canvas.drawColor(Color.WHITE)

        val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(36, 27, 47)
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val section = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(124, 77, 255)
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(70, 62, 78)
            textSize = 14f
        }

        canvas.drawText("详细分析", MARGIN, 64f, heading)
        canvas.drawText("五官比例", MARGIN, 112f, section)

        var y = 145f
        report.metrics.asReadableText().lineSequence().forEach { line ->
            canvas.drawText("• $line", MARGIN + 8f, y, body)
            y += 29f
        }

        y += 15f
        canvas.drawText("推荐发型", MARGIN, y, section)
        y += 34f
        report.hairRecommendations.forEachIndexed { index, name ->
            canvas.drawText("${index + 1}. $name", MARGIN + 8f, y, body)
            y += 28f
        }

        y += 20f
        canvas.drawText("使用建议", MARGIN, y, section)
        y += 34f
        val tips = listOf(
            "正脸、均匀光线和无遮挡照片能提高关键点定位稳定性。",
            "试戴效果属于二维透明图层融合，侧脸与大角度照片可能出现偏移。",
            "审美受文化、年龄与个人偏好影响，不存在统一的客观颜值标准。",
            "请勿将本结果用于招聘、医疗、身份判断或其他高影响决策。"
        )
        tips.forEach { tip ->
            y = drawWrappedText(
                canvas,
                "• $tip",
                MARGIN + 8f,
                y,
                495f,
                22f,
                body
            ) + 10f
        }

        document.finishPage(page)
    }

    private fun fitRect(
        sourceWidth: Int,
        sourceHeight: Int,
        left: Float,
        top: Float,
        maxWidth: Float,
        maxHeight: Float
    ): RectF {
        val scale = min(maxWidth / sourceWidth, maxHeight / sourceHeight)
        val width = sourceWidth * scale
        val height = sourceHeight * scale
        return RectF(
            left + (maxWidth - width) / 2f,
            top + (maxHeight - height) / 2f,
            left + (maxWidth + width) / 2f,
            top + (maxHeight + height) / 2f
        )
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        lineHeight: Float,
        paint: Paint
    ): Float {
        var currentY = y
        val line = StringBuilder()
        text.forEach { char ->
            val candidate = line.toString() + char
            if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line.toString(), x, currentY, paint)
                currentY += lineHeight
                line.clear()
            }
            line.append(char)
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line.toString(), x, currentY, paint)
        }
        return currentY
    }
}
