package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AILevelType
import com.example.domain.model.AIPatternOverlay
import com.example.domain.model.AIPriceLevel
import com.example.domain.model.CandleData
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat

/**
 * Highly reusable Compose Candlestick Chart Component with comprehensive placeholder support,
 * real-time AI pattern visualization overlays, indicator toggles, volume profile,
 * and interactive crosshair inspection.
 *
 * Designed to lay the groundwork for real-time AI pattern visualization in CryptoSignal AI.
 */
@Composable
fun CandlestickChart(
    candles: List<CandleData>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 230.dp,
    showEma: Boolean = true,
    showVolume: Boolean = true,
    showAiOverlays: Boolean = true,
    aiPatterns: List<AIPatternOverlay> = emptyList(),
    aiPriceLevels: List<AIPriceLevel> = emptyList(),
    isScanningAi: Boolean = false,
    selectedPattern: AIPatternOverlay? = null,
    onPatternSelected: ((AIPatternOverlay?) -> Unit)? = null,
    placeholderCandleCount: Int = 35
) {
    // If no real candles are passed, display an animated synthetic placeholder candlestick layout
    if (candles.isEmpty()) {
        CandlestickChartPlaceholder(
            modifier = modifier,
            chartHeight = chartHeight,
            candleCount = placeholderCandleCount,
            isScanning = isScanningAi
        )
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var userToggledEma by remember { mutableStateOf(showEma) }
    var userToggledAi by remember { mutableStateOf(showAiOverlays) }
    var userToggledVolume by remember { mutableStateOf(showVolume) }

    val df = remember { DecimalFormat("#,##0.00") }
    val activeCandle = selectedIndex?.let { candles.getOrNull(it) } ?: candles.lastOrNull()

    // Pulse animation for AI pattern bounding boxes & scan line
    val infiniteTransition = rememberInfiniteTransition(label = "ai_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ai_overlay_pulse"
    )

    val scanSweepY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ai_scan_sweep"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("candlestick_chart")
    ) {
        // Top Toolbar: Active OHLC Data + Toggle Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // OHLC Values
            if (activeCandle != null) {
                val isBullish = activeCandle.close >= activeCandle.open
                val candleColor = if (isBullish) NeonGreen else NeonRed

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("O:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(df.format(activeCandle.open), style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))

                    Text("H:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(df.format(activeCandle.high), style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))

                    Text("L:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(df.format(activeCandle.low), style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))

                    Text("C:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(df.format(activeCandle.close), style = MaterialTheme.typography.labelSmall, color = candleColor, fontWeight = FontWeight.Bold)
                }
            }

            // Quick Chart Overlays Toggles
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // AI Overlays indicator toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (userToggledAi) NeonPurple.copy(alpha = 0.25f) else DarkCardElevated)
                        .border(
                            width = 1.dp,
                            color = if (userToggledAi) NeonPurple.copy(alpha = 0.6f) else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Toggle AI Pattern Vision",
                            tint = if (userToggledAi) NeonPurple else TextMuted,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (aiPatterns.isNotEmpty()) "AI Patterns (${aiPatterns.size})" else "AI Vision",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = if (userToggledAi) NeonPurple else TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // EMA Indicators Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkCardElevated)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Box(modifier = Modifier.height(2.dp).width(7.dp).background(NeonCyan))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("20", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, color = NeonCyan))
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.height(2.dp).width(7.dp).background(NeonGold))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("50", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, color = NeonGold))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Candlestick + Pattern Overlay Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(candles) {
                        detectTapGestures(
                            onTap = { offset ->
                                val candleWidth = size.width / candles.size
                                val index = (offset.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                selectedIndex = index

                                // Check if user tapped inside an AI pattern zone
                                val clickedPattern = aiPatterns.find { p ->
                                    index in p.startCandleIndex..p.endCandleIndex
                                }
                                onPatternSelected?.invoke(clickedPattern)
                            }
                        )
                    }
            ) {
                val width = size.width
                val height = size.height

                val priceChartHeight = if (userToggledVolume) height * 0.78f else height
                val volumeChartHeight = if (userToggledVolume) height * 0.20f else 0f

                val minPrice = candles.minOf { it.low }
                val maxPrice = candles.maxOf { it.high }
                val priceRange = if (maxPrice > minPrice) (maxPrice - minPrice) else 1f
                val maxVolume = candles.maxOf { it.volume }.coerceAtLeast(1f)

                val candleSlotWidth = width / candles.size
                val candleBodyWidth = (candleSlotWidth * 0.65f).coerceAtLeast(2f)

                // 1. Draw subtle background horizontal price gridlines
                val gridLines = 4
                for (g in 0..gridLines) {
                    val y = (priceChartHeight / gridLines) * g
                    drawLine(
                        color = DarkBorder.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                // 2. Render AI Horizontal Price Levels (Support, Resistance, Entry, TP, SL)
                if (userToggledAi && aiPriceLevels.isNotEmpty()) {
                    aiPriceLevels.forEach { lvl ->
                        if (lvl.price in (minPrice * 0.95f)..(maxPrice * 1.05f)) {
                            val levelY = priceChartHeight - ((lvl.price - minPrice) / priceRange) * priceChartHeight
                            val (levelColor, strokeWidth, pathEffect) = when (lvl.levelType) {
                                AILevelType.SUPPORT -> Triple(NeonGreen, 1.5f, PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
                                AILevelType.RESISTANCE -> Triple(NeonRed, 1.5f, PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
                                AILevelType.ENTRY -> Triple(NeonCyan, 2f, PathEffect.dashPathEffect(floatArrayOf(14f, 6f)))
                                AILevelType.TAKE_PROFIT -> Triple(NeonGreen, 1.5f, null)
                                AILevelType.STOP_LOSS -> Triple(NeonRed, 1.5f, null)
                            }

                            drawLine(
                                color = levelColor.copy(alpha = 0.75f),
                                start = Offset(0f, levelY),
                                end = Offset(width, levelY),
                                strokeWidth = strokeWidth,
                                pathEffect = pathEffect
                            )
                        }
                    }
                }

                // 3. Render AI Pattern Visual Overlays (FVG, Order Blocks, Liquidity Sweeps)
                if (userToggledAi && aiPatterns.isNotEmpty()) {
                    aiPatterns.forEach { pattern ->
                        val startIdx = pattern.startCandleIndex.coerceIn(0, candles.size - 1)
                        val endIdx = pattern.endCandleIndex.coerceIn(startIdx, candles.size - 1)

                        val startX = startIdx * candleSlotWidth
                        val endX = (endIdx + 1) * candleSlotWidth
                        val zoneWidth = endX - startX

                        val topY = priceChartHeight - ((pattern.topPrice - minPrice) / priceRange) * priceChartHeight
                        val bottomY = priceChartHeight - ((pattern.bottomPrice - minPrice) / priceRange) * priceChartHeight

                        val zoneTop = minOf(topY, bottomY).coerceIn(0f, priceChartHeight)
                        val zoneBottom = maxOf(topY, bottomY).coerceIn(0f, priceChartHeight)
                        val zoneHeight = maxOf(zoneBottom - zoneTop, 6f)

                        val patternColor = if (pattern.type.isBullish) NeonCyan else NeonPurple
                        val isCurrentSelected = pattern.id == selectedPattern?.id

                        // Fill translucent shaded box
                        drawRoundRect(
                            color = patternColor.copy(alpha = if (isCurrentSelected) 0.35f else pulseAlpha * 0.4f),
                            topLeft = Offset(startX, zoneTop),
                            size = Size(zoneWidth, zoneHeight),
                            cornerRadius = CornerRadius(4f, 4f)
                        )

                        // Border for pattern box
                        drawRoundRect(
                            color = patternColor.copy(alpha = if (isCurrentSelected) 0.9f else 0.7f),
                            topLeft = Offset(startX, zoneTop),
                            size = Size(zoneWidth, zoneHeight),
                            cornerRadius = CornerRadius(4f, 4f),
                            style = Stroke(
                                width = if (isCurrentSelected) 2f else 1.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f))
                            )
                        )

                        // Highlight Target price line if available
                        pattern.targetPrice?.let { target ->
                            val targetY = priceChartHeight - ((target - minPrice) / priceRange) * priceChartHeight
                            drawLine(
                                color = NeonGreen.copy(alpha = 0.8f),
                                start = Offset(startX, targetY),
                                end = Offset(width, targetY),
                                strokeWidth = 1.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                            )
                        }
                    }
                }

                // 4. Draw Candlesticks & Volume Bars
                val emaPoints20 = mutableListOf<Offset>()
                val emaPoints50 = mutableListOf<Offset>()

                var prevEma20 = candles.first().close
                var prevEma50 = candles.first().close
                val k20 = 2f / (20f + 1f)
                val k50 = 2f / (50f + 1f)

                candles.forEachIndexed { i, candle ->
                    val xCenter = i * candleSlotWidth + (candleSlotWidth / 2f)

                    // Price to Y coordinate conversion
                    val highY = priceChartHeight - ((candle.high - minPrice) / priceRange) * priceChartHeight
                    val lowY = priceChartHeight - ((candle.low - minPrice) / priceRange) * priceChartHeight
                    val openY = priceChartHeight - ((candle.open - minPrice) / priceRange) * priceChartHeight
                    val closeY = priceChartHeight - ((candle.close - minPrice) / priceRange) * priceChartHeight

                    val isBullish = candle.close >= candle.open
                    val color = if (isBullish) NeonGreen else NeonRed

                    // Candlestick Upper & Lower Wick
                    drawLine(
                        color = color,
                        start = Offset(xCenter, highY),
                        end = Offset(xCenter, lowY),
                        strokeWidth = 2f
                    )

                    // Candlestick Body
                    val bodyTop = minOf(openY, closeY)
                    val bodyHeight = maxOf(kotlin.math.abs(closeY - openY), 2.5f)

                    drawRect(
                        color = color,
                        topLeft = Offset(xCenter - (candleBodyWidth / 2f), bodyTop),
                        size = Size(candleBodyWidth, bodyHeight)
                    )

                    // Volume Bar (Lower pane)
                    if (userToggledVolume && volumeChartHeight > 0f) {
                        val volHeight = (candle.volume / maxVolume) * volumeChartHeight
                        drawRect(
                            color = color.copy(alpha = 0.40f),
                            topLeft = Offset(xCenter - (candleBodyWidth / 2f), height - volHeight),
                            size = Size(candleBodyWidth, volHeight)
                        )
                    }

                    // EMA Calculations
                    val ema20 = candle.close * k20 + prevEma20 * (1f - k20)
                    prevEma20 = ema20
                    val ema20Y = priceChartHeight - ((ema20 - minPrice) / priceRange) * priceChartHeight
                    emaPoints20.add(Offset(xCenter, ema20Y))

                    val ema50 = candle.close * k50 + prevEma50 * (1f - k50)
                    prevEma50 = ema50
                    val ema50Y = priceChartHeight - ((ema50 - minPrice) / priceRange) * priceChartHeight
                    emaPoints50.add(Offset(xCenter, ema50Y))
                }

                // 5. Render Smooth EMA Indicator Curves
                if (userToggledEma && emaPoints20.size > 1) {
                    val path20 = Path().apply {
                        moveTo(emaPoints20.first().x, emaPoints20.first().y)
                        for (p in 1 until emaPoints20.size) {
                            lineTo(emaPoints20[p].x, emaPoints20[p].y)
                        }
                    }
                    drawPath(path20, color = NeonCyan, style = Stroke(width = 2.2f))

                    val path50 = Path().apply {
                        moveTo(emaPoints50.first().x, emaPoints50.first().y)
                        for (p in 1 until emaPoints50.size) {
                            lineTo(emaPoints50[p].x, emaPoints50[p].y)
                        }
                    }
                    drawPath(path50, color = NeonGold, style = Stroke(width = 1.8f))
                }

                // 6. Active AI Scanning Laser Sweep (when AI scanner is running)
                if (isScanningAi) {
                    val sweepY = priceChartHeight * scanSweepY
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, NeonCyan, NeonPurple, Color.Transparent)
                        ),
                        start = Offset(0f, sweepY),
                        end = Offset(width, sweepY),
                        strokeWidth = 2.5f
                    )
                }

                // 7. Interactive Crosshair & Cursor Inspector
                selectedIndex?.let { idx ->
                    if (idx in candles.indices) {
                        val x = idx * candleSlotWidth + (candleSlotWidth / 2f)
                        val candle = candles[idx]
                        val closeY = priceChartHeight - ((candle.close - minPrice) / priceRange) * priceChartHeight

                        // Vertical inspection line
                        drawLine(
                            color = TextSecondary.copy(alpha = 0.6f),
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = 1.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )

                        // Horizontal price line
                        drawLine(
                            color = TextSecondary.copy(alpha = 0.5f),
                            start = Offset(0f, closeY),
                            end = Offset(width, closeY),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )

                        // Center focal circle
                        drawCircle(
                            color = NeonCyan,
                            radius = 4f,
                            center = Offset(x, closeY)
                        )
                    }
                }
            }
        }

        // Active Pattern Banner if one is selected or detected
        val bannerPattern = selectedPattern ?: aiPatterns.lastOrNull()
        if (userToggledAi && bannerPattern != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_pattern_badge_${bannerPattern.id}"),
                shape = RoundedCornerShape(8.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (bannerPattern.type.isBullish) NeonGreen else NeonRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = bannerPattern.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${bannerPattern.confidence}% AI Confidence",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonPurple,
                                fontSize = 10.sp
                            )
                        )
                    }

                    bannerPattern.targetPrice?.let { target ->
                        Text(
                            text = "Target: $${df.format(target)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonGreen,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reusable animated Candlestick Chart Placeholder component.
 * Displays synthetic rhythmic candle bars with a glowing AI radar sweep,
 * establishing a ready-to-render canvas placeholder before live network data arrives.
 */
@Composable
fun CandlestickChartPlaceholder(
    modifier: Modifier = Modifier,
    chartHeight: Dp = 230.dp,
    candleCount: Int = 30,
    isScanning: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "placeholder_sweep")
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "placeholder_radar"
    )

    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("candlestick_chart_placeholder")
    ) {
        // Placeholder Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = shimmerAlpha))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI Candlestick Neural Visualizer",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkCardElevated)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isScanning) "SYNCHRONIZING..." else "READY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NeonCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Synthetic Candlestick Placeholder Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val priceChartHeight = height * 0.80f
                val volumeChartHeight = height * 0.18f

                // Draw horizontal guide lines
                val gridLines = 4
                for (g in 0..gridLines) {
                    val y = (priceChartHeight / gridLines) * g
                    drawLine(
                        color = DarkBorder.copy(alpha = 0.35f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                val slotWidth = width / candleCount
                val bodyWidth = (slotWidth * 0.65f).coerceAtLeast(2f)

                // Render rhythmic synthetic candles
                for (i in 0 until candleCount) {
                    val x = i * slotWidth + (slotWidth / 2f)

                    // Wave pattern for harmonic aesthetic
                    val wave = kotlin.math.sin(i * 0.35).toFloat()
                    val candleMidY = (priceChartHeight * 0.5f) + wave * (priceChartHeight * 0.28f)
                    val candleBodyH = (14f + (i % 5) * 8f).coerceAtMost(priceChartHeight * 0.3f)
                    val wickExtra = 12f + (i % 3) * 6f

                    val isBullish = i % 2 == 0 || wave > 0
                    val color = if (isBullish) NeonGreen else NeonRed
                    val baseAlpha = if (i.toFloat() / candleCount < sweepProgress) 0.85f else 0.35f

                    // Wick
                    drawLine(
                        color = color.copy(alpha = baseAlpha * shimmerAlpha),
                        start = Offset(x, candleMidY - candleBodyH / 2f - wickExtra),
                        end = Offset(x, candleMidY + candleBodyH / 2f + wickExtra),
                        strokeWidth = 1.8f
                    )

                    // Body
                    drawRect(
                        color = color.copy(alpha = baseAlpha * shimmerAlpha),
                        topLeft = Offset(x - bodyWidth / 2f, candleMidY - candleBodyH / 2f),
                        size = Size(bodyWidth, candleBodyH)
                    )

                    // Volume
                    val volH = (volumeChartHeight * ((i % 7 + 3) / 10f)).coerceAtLeast(4f)
                    drawRect(
                        color = color.copy(alpha = 0.25f * shimmerAlpha),
                        topLeft = Offset(x - bodyWidth / 2f, height - volH),
                        size = Size(bodyWidth, volH)
                    )
                }

                // AI Sweep Radar Line
                val sweepX = width * sweepProgress
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.8f), NeonPurple.copy(alpha = 0.8f), Color.Transparent)
                    ),
                    start = Offset(sweepX, 0f),
                    end = Offset(sweepX, height),
                    strokeWidth = 2.5f
                )
            }

            // Foreground Status Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkSurface.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Real-time AI Pattern Vision Engine Initializing...",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}
