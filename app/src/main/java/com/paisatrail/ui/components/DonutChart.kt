package com.paisatrail.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

data class ChartSlice(val value: Float, val color: Color)

/**
 * Canvas-drawn donut chart (no chart library).
 *
 * @param holeFraction 0 draws a full pie, 0.6 a ring whose hole is 60% of the radius.
 * @param emptyColor ring colour used when there is nothing to plot.
 */
@Composable
fun DonutChart(
    slices: List<ChartSlice>,
    emptyColor: Color,
    modifier: Modifier = Modifier,
    holeFraction: Float = 0.56f
) {
    Canvas(modifier = modifier) {
        val diameter = minOf(size.width, size.height)
        if (diameter <= 0f) return@Canvas
        val ringWidth = diameter / 2f * (1f - holeFraction.coerceIn(0f, 0.95f))
        val arcDiameter = diameter - ringWidth
        val topLeft = Offset((size.width - arcDiameter) / 2f, (size.height - arcDiameter) / 2f)
        val arcSize = Size(arcDiameter, arcDiameter)
        val stroke = Stroke(width = ringWidth)

        val visible = slices.filter { it.value > 0f }
        val total = visible.sumOf { it.value.toDouble() }.toFloat()
        if (total <= 0f) {
            drawArc(emptyColor, 0f, 360f, false, topLeft, arcSize, style = stroke)
            return@Canvas
        }
        var startAngle = -90f
        for (slice in visible) {
            val sweep = slice.value / total * 360f
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
            startAngle += sweep
        }
    }
}
