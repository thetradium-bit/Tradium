package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Signal
import com.example.domain.model.SignalType
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalDetailSheet(
    signal: Signal?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onExecuteTrade: (Signal) -> Unit
) {
    if (signal == null) return

    val df = DecimalFormat("#,##0.00")
    val isLong = signal.type == SignalType.LONG || signal.type == SignalType.SPOT_BUY
    val typeColor = if (isLong) NeonGreen else NeonRed

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Pair, Leverage, and Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(typeColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = signal.pair,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 22.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkCardElevated)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${signal.type.name} • ${signal.leverage}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = typeColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Confidence Highlight Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NeonPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Neural Confidence Score: ",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                            Text(
                                text = "${signal.confidenceScore}%",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                        Text(
                            text = "Calculated across 14 technical oscillators and order flow depth.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(14.dp))

            // AI Candlestick Chart Visualization Preview
            Text(
                text = "CANDLESTICK PATTERN VISION",
                style = MaterialTheme.typography.labelMedium.copy(color = TextMuted, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Generate synthetic real-time candles around signal entry for visualization
            val previewCandles = remember(signal.id) {
                val base = signal.entryPrice.toFloat()
                val isBull = isLong
                val list = mutableListOf<com.example.domain.model.CandleData>()
                val count = 28
                var price = base * if (isBull) 0.965f else 1.035f
                val delta = (base - price) / count
                for (i in 0 until count) {
                    val open = price
                    val close = open + delta + (if (i % 2 == 0) 1f else -1f) * (base * 0.003f)
                    val high = maxOf(open, close) + (base * 0.004f)
                    val low = minOf(open, close) - (base * 0.004f)
                    list.add(
                        com.example.domain.model.CandleData(
                            timestamp = System.currentTimeMillis() - (count - i) * 3600000L,
                            open = open,
                            high = high,
                            low = low,
                            close = close,
                            volume = 100f + (i * 5f)
                        )
                    )
                    price = close
                }
                list
            }

            val patternOverlays = remember(signal.id) {
                listOf(
                    com.example.domain.model.AIPatternOverlay(
                        id = "sig_${signal.id}",
                        type = if (isLong) com.example.domain.model.AIPatternType.FAIR_VALUE_GAP else com.example.domain.model.AIPatternType.BEARISH_ORDER_BLOCK,
                        startCandleIndex = 16,
                        endCandleIndex = 25,
                        topPrice = maxOf(signal.entryPrice.toFloat(), signal.targetPrice1.toFloat()),
                        bottomPrice = minOf(signal.entryPrice.toFloat(), signal.stopLoss.toFloat()),
                        label = "${signal.type.name} Liquidity Zone",
                        confidence = signal.confidenceScore,
                        targetPrice = signal.targetPrice1.toFloat(),
                        stopPrice = signal.stopLoss.toFloat(),
                        description = signal.aiReasoning
                    )
                )
            }

            val priceLevels = remember(signal.id) {
                listOf(
                    com.example.domain.model.AIPriceLevel("Entry", signal.entryPrice.toFloat(), com.example.domain.model.AILevelType.ENTRY),
                    com.example.domain.model.AIPriceLevel("TP1", signal.targetPrice1.toFloat(), com.example.domain.model.AILevelType.TAKE_PROFIT),
                    com.example.domain.model.AIPriceLevel("SL", signal.stopLoss.toFloat(), com.example.domain.model.AILevelType.STOP_LOSS)
                )
            }

            CandlestickChart(
                candles = previewCandles,
                chartHeight = 180.dp,
                showEma = true,
                showVolume = true,
                showAiOverlays = true,
                aiPatterns = patternOverlays,
                aiPriceLevels = priceLevels
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Trade Execution Levels (Entry, TPs, SL)
            Text(
                text = "EXECUTION TARGETS",
                style = MaterialTheme.typography.labelMedium.copy(color = TextMuted, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TargetRow("Entry Price", "$${df.format(signal.entryPrice)}", TextPrimary)
                    TargetRow("Target 1 (Conservative)", "$${df.format(signal.targetPrice1)}", NeonGreen)
                    TargetRow("Target 2 (Standard)", "$${df.format(signal.targetPrice2)}", NeonGreen)
                    TargetRow("Target 3 (Runner / Max)", "$${df.format(signal.targetPrice3)}", NeonGreen)
                    TargetRow("Stop Loss (Strict)", "$${df.format(signal.stopLoss)}", NeonRed)
                    TargetRow("Risk / Reward Ratio", "1 : ${DecimalFormat("#0.0").format(signal.riskRewardRatio)}", NeonCyan, isLast = true)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Technical Thesis & Catalyst
            Text(
                text = "AI TECHNICAL THESIS",
                style = MaterialTheme.typography.labelMedium.copy(color = TextMuted, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = signal.aiReasoning,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            lineHeight = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Key Indicator Readings:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    signal.technicalIndicators.forEach { indicator ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = indicator,
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Button: One-Tap Paper Trade
            Button(
                onClick = {
                    onExecuteTrade(signal)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("execute_paper_trade_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = DarkSurface
                )
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open Demo Position ($500 Margin)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DarkSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TargetRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(
                color = valueColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
    if (!isLast) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(DarkBorder.copy(alpha = 0.5f))
                .height(0.8.dp)
        )
    }
}
