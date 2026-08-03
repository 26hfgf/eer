package com.aibeautypro.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import com.aibeautypro.model.HairStyle
import com.aibeautypro.model.HairTemplate
import kotlin.math.atan2
import kotlin.math.hypot

object HairRenderer {
    private fun distance(
        a: PointF,
        b: PointF,
        width: Int,
        height: Int
    ): Float {
        val dx = (a.x - b.x) * width
        val dy = (a.y - b.y) * height
        return hypot(dx.toDouble(), dy.toDouble()).toFloat()
    }

    fun render(
        source: Bitmap,
        landmarks: List<PointF>,
        template: HairTemplate
    ): Bitmap {
        require(landmarks.size >= 468) { "人脸关键点数量不足" }

        val result = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val width = source.width
        val height = source.height

        val faceWidth = distance(landmarks[234], landmarks[454], width, height)
        val faceHeight = distance(landmarks[10], landmarks[152], width, height)
        val centerX = ((landmarks[234].x + landmarks[454].x) / 2f) * width
        val foreheadY = landmarks[10].y * height
        val chinY = landmarks[152].y * height
        val hairWidth = faceWidth * template.widthScale
        val hairHeight = faceHeight * template.heightScale
        val top = foreheadY - hairHeight * template.topOffset
        val left = centerX - hairWidth / 2f
        val right = centerX + hairWidth / 2f
        val bottom = top + hairHeight

        val leftEye = PointF(
            (landmarks[33].x + landmarks[133].x) / 2f,
            (landmarks[33].y + landmarks[133].y) / 2f
        )
        val rightEye = PointF(
            (landmarks[362].x + landmarks[263].x) / 2f,
            (landmarks[362].y + landmarks[263].y) / 2f
        )
        val angle = Math.toDegrees(
            atan2(
                ((rightEye.y - leftEye.y) * height).toDouble(),
                ((rightEye.x - leftEye.x) * width).toDouble()
            )
        ).toFloat()

        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = template.baseColor
            style = Paint.Style.FILL
        }
        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = template.highlightColor
            style = Paint.Style.STROKE
            strokeWidth = (faceWidth * 0.032f).coerceAtLeast(4f)
            strokeCap = Paint.Cap.ROUND
            alpha = 150
        }
        val fineHighlightPaint = Paint(highlightPaint).apply {
            strokeWidth = (faceWidth * 0.014f).coerceAtLeast(2f)
            alpha = 110
        }

        canvas.save()
        canvas.rotate(angle, centerX, foreheadY + faceHeight * 0.32f)

        val shell = buildShell(
            style = template.style,
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            centerX = centerX,
            foreheadY = foreheadY,
            chinY = chinY,
            faceWidth = faceWidth,
            faceHeight = faceHeight
        )
        canvas.drawPath(shell, basePaint)

        drawHighlights(
            canvas = canvas,
            style = template.style,
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            centerX = centerX,
            foreheadY = foreheadY,
            faceWidth = faceWidth,
            faceHeight = faceHeight,
            paint = highlightPaint,
            finePaint = fineHighlightPaint
        )

        drawFringe(
            canvas = canvas,
            style = template.style,
            centerX = centerX,
            foreheadY = foreheadY,
            faceWidth = faceWidth,
            faceHeight = faceHeight,
            basePaint = basePaint,
            highlightPaint = fineHighlightPaint
        )

        canvas.restore()
        return result
    }

    private fun buildShell(
        style: HairStyle,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        centerX: Float,
        foreheadY: Float,
        chinY: Float,
        faceWidth: Float,
        faceHeight: Float
    ): Path {
        val outer = Path().apply {
            moveTo(centerX, top)
            cubicTo(left + faceWidth * 0.18f, top, left, top + faceHeight * 0.42f, left, foreheadY + faceHeight * 0.42f)
            when (style) {
                HairStyle.LONG_WAVE, HairStyle.SIDE_PART -> {
                    cubicTo(left - faceWidth * 0.08f, bottom - faceHeight * 0.35f, left + faceWidth * 0.06f, bottom, left + faceWidth * 0.28f, bottom)
                    cubicTo(centerX - faceWidth * 0.40f, bottom - faceHeight * 0.12f, centerX + faceWidth * 0.40f, bottom - faceHeight * 0.12f, right - faceWidth * 0.28f, bottom)
                    cubicTo(right - faceWidth * 0.06f, bottom, right + faceWidth * 0.08f, bottom - faceHeight * 0.35f, right, foreheadY + faceHeight * 0.42f)
                }
                HairStyle.SOFT_BOB, HairStyle.AIR_BANGS -> {
                    cubicTo(left, bottom - faceHeight * 0.18f, left + faceWidth * 0.14f, bottom, centerX, bottom)
                    cubicTo(right - faceWidth * 0.14f, bottom, right, bottom - faceHeight * 0.18f, right, foreheadY + faceHeight * 0.42f)
                }
                HairStyle.HIGH_BOB -> {
                    cubicTo(left + faceWidth * 0.02f, bottom - faceHeight * 0.10f, left + faceWidth * 0.20f, bottom, centerX, bottom - faceHeight * 0.04f)
                    cubicTo(right - faceWidth * 0.20f, bottom, right - faceWidth * 0.02f, bottom - faceHeight * 0.10f, right, foreheadY + faceHeight * 0.42f)
                }
            }
            cubicTo(right, top + faceHeight * 0.42f, right - faceWidth * 0.18f, top, centerX, top)
            close()

            fillType = Path.FillType.EVEN_ODD
            val openingTop = foreheadY + faceHeight * 0.05f
            val openingBottom = when (style) {
                HairStyle.LONG_WAVE, HairStyle.SIDE_PART -> chinY + faceHeight * 0.14f
                else -> chinY + faceHeight * 0.06f
            }
            addOval(
                RectF(
                    centerX - faceWidth * 0.51f,
                    openingTop,
                    centerX + faceWidth * 0.51f,
                    openingBottom
                ),
                Path.Direction.CW
            )
        }
        return outer
    }

    private fun drawHighlights(
        canvas: Canvas,
        style: HairStyle,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        centerX: Float,
        foreheadY: Float,
        faceWidth: Float,
        faceHeight: Float,
        paint: Paint,
        finePaint: Paint
    ) {
        val leftFlow = Path().apply {
            moveTo(centerX - faceWidth * 0.16f, top + faceHeight * 0.08f)
            cubicTo(left + faceWidth * 0.30f, foreheadY, left + faceWidth * 0.15f, foreheadY + faceHeight * 0.40f, left + faceWidth * 0.20f, bottom - faceHeight * 0.12f)
        }
        val rightFlow = Path().apply {
            moveTo(centerX + faceWidth * 0.12f, top + faceHeight * 0.09f)
            cubicTo(right - faceWidth * 0.26f, foreheadY, right - faceWidth * 0.12f, foreheadY + faceHeight * 0.42f, right - faceWidth * 0.20f, bottom - faceHeight * 0.12f)
        }
        canvas.drawPath(leftFlow, paint)
        canvas.drawPath(rightFlow, paint)

        if (style == HairStyle.LONG_WAVE || style == HairStyle.SIDE_PART) {
            repeat(3) { index ->
                val offset = index * faceWidth * 0.08f
                val waveLeft = Path().apply {
                    moveTo(left + faceWidth * 0.14f + offset, foreheadY + faceHeight * 0.48f)
                    cubicTo(
                        left + faceWidth * 0.02f + offset,
                        foreheadY + faceHeight * 0.76f,
                        left + faceWidth * 0.30f + offset,
                        bottom - faceHeight * 0.18f,
                        left + faceWidth * 0.18f + offset,
                        bottom
                    )
                }
                canvas.drawPath(waveLeft, finePaint)

                val waveRight = Path().apply {
                    moveTo(right - faceWidth * 0.14f - offset, foreheadY + faceHeight * 0.48f)
                    cubicTo(
                        right - faceWidth * 0.02f - offset,
                        foreheadY + faceHeight * 0.76f,
                        right - faceWidth * 0.30f - offset,
                        bottom - faceHeight * 0.18f,
                        right - faceWidth * 0.18f - offset,
                        bottom
                    )
                }
                canvas.drawPath(waveRight, finePaint)
            }
        }
    }

    private fun drawFringe(
        canvas: Canvas,
        style: HairStyle,
        centerX: Float,
        foreheadY: Float,
        faceWidth: Float,
        faceHeight: Float,
        basePaint: Paint,
        highlightPaint: Paint
    ) {
        when (style) {
            HairStyle.AIR_BANGS -> {
                val strands = listOf(-0.32f, -0.17f, 0f, 0.17f, 0.32f)
                strands.forEachIndexed { index, fraction ->
                    val x = centerX + faceWidth * fraction
                    val strand = Path().apply {
                        moveTo(x - faceWidth * 0.09f, foreheadY - faceHeight * 0.24f)
                        cubicTo(
                            x - faceWidth * 0.05f,
                            foreheadY - faceHeight * 0.06f,
                            x + faceWidth * 0.04f,
                            foreheadY + faceHeight * (0.10f + index % 2 * 0.03f),
                            x + faceWidth * 0.08f,
                            foreheadY + faceHeight * 0.16f
                        )
                        cubicTo(
                            x + faceWidth * 0.02f,
                            foreheadY + faceHeight * 0.11f,
                            x - faceWidth * 0.02f,
                            foreheadY,
                            x - faceWidth * 0.09f,
                            foreheadY - faceHeight * 0.24f
                        )
                        close()
                    }
                    canvas.drawPath(strand, Paint(basePaint).apply { alpha = 220 })
                }
            }

            HairStyle.SIDE_PART -> {
                val sweep = Path().apply {
                    moveTo(centerX - faceWidth * 0.30f, foreheadY - faceHeight * 0.28f)
                    cubicTo(
                        centerX + faceWidth * 0.02f,
                        foreheadY - faceHeight * 0.22f,
                        centerX + faceWidth * 0.38f,
                        foreheadY - faceHeight * 0.02f,
                        centerX + faceWidth * 0.50f,
                        foreheadY + faceHeight * 0.35f
                    )
                    cubicTo(
                        centerX + faceWidth * 0.34f,
                        foreheadY + faceHeight * 0.16f,
                        centerX + faceWidth * 0.08f,
                        foreheadY + faceHeight * 0.02f,
                        centerX - faceWidth * 0.30f,
                        foreheadY - faceHeight * 0.28f
                    )
                    close()
                }
                canvas.drawPath(sweep, basePaint)
                canvas.drawPath(
                    Path().apply {
                        moveTo(centerX - faceWidth * 0.22f, foreheadY - faceHeight * 0.22f)
                        cubicTo(
                            centerX + faceWidth * 0.06f,
                            foreheadY - faceHeight * 0.14f,
                            centerX + faceWidth * 0.30f,
                            foreheadY + faceHeight * 0.02f,
                            centerX + faceWidth * 0.40f,
                            foreheadY + faceHeight * 0.26f
                        )
                    },
                    highlightPaint
                )
            }

            HairStyle.SOFT_BOB -> {
                val softSweep = Path().apply {
                    moveTo(centerX - faceWidth * 0.42f, foreheadY - faceHeight * 0.20f)
                    cubicTo(
                        centerX - faceWidth * 0.08f,
                        foreheadY - faceHeight * 0.30f,
                        centerX + faceWidth * 0.28f,
                        foreheadY - faceHeight * 0.12f,
                        centerX + faceWidth * 0.46f,
                        foreheadY + faceHeight * 0.20f
                    )
                    cubicTo(
                        centerX + faceWidth * 0.18f,
                        foreheadY + faceHeight * 0.02f,
                        centerX - faceWidth * 0.14f,
                        foreheadY + faceHeight * 0.02f,
                        centerX - faceWidth * 0.42f,
                        foreheadY - faceHeight * 0.20f
                    )
                    close()
                }
                canvas.drawPath(softSweep, basePaint)
            }

            HairStyle.HIGH_BOB -> {
                val shortFringe = Path().apply {
                    moveTo(centerX - faceWidth * 0.42f, foreheadY - faceHeight * 0.16f)
                    cubicTo(
                        centerX - faceWidth * 0.12f,
                        foreheadY - faceHeight * 0.28f,
                        centerX + faceWidth * 0.18f,
                        foreheadY - faceHeight * 0.20f,
                        centerX + faceWidth * 0.42f,
                        foreheadY + faceHeight * 0.05f
                    )
                    cubicTo(
                        centerX + faceWidth * 0.12f,
                        foreheadY - faceHeight * 0.03f,
                        centerX - faceWidth * 0.16f,
                        foreheadY + faceHeight * 0.02f,
                        centerX - faceWidth * 0.42f,
                        foreheadY - faceHeight * 0.16f
                    )
                    close()
                }
                canvas.drawPath(shortFringe, basePaint)
            }

            HairStyle.LONG_WAVE -> Unit
        }
    }
}
