package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Signal
import com.example.domain.model.SignalStatus
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

@Composable
fun SignalCard(
    signal: Signal,
    onCardClick: () -> Unit,
    onExecutePaperTrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    val df = DecimalFormat("#,##0.00")
    val isLong = signal.type == SignalType.LONG || signal.type == SignalType.SPOT_BUY
    val typeColor = if (isLong) NeonGreen else NeonRed

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("signal_card_${signal.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Pair, Action Pill, Leverage, Timeframe
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = signal.pair,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Leverage tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkCardElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = signal.leverage,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Timeframe tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = signal.timeframe,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary
                            )
                        )
                    }
                }

                // Action Type Badge (LONG / SHORT / SPOT BUY)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(typeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isLong) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = signal.type.name.replace("_", " "),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = typeColor,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pricing Grid: Entry Price | Current Price | Target 1 | Stop Loss
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ENTRY", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = "$${df.format(signal.entryPrice)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                }

                Column {
                    Text("TARGET 1", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = "$${df.format(signal.targetPrice1)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonGreen
                    )
                }

                Column {
                    Text("TARGET 2", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = "$${df.format(signal.targetPrice2)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonGreen
                    )
                }

                Column {
                    Text("STOP LOSS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = "$${df.format(signal.stopLoss)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Confidence & Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Score",
                        tint = NeonPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AI Confidence: ",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = "${signal.confidenceScore}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• R:R ${DecimalFormat("#0.0").format(signal.riskRewardRatio)}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                }

                // Signal Status Chip
                SignalStatusPill(status = signal.status)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row: View Analysis & Copy / Trade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCardClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("view_signal_${signal.id}"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextPrimary
                    )
                ) {
                    Text("AI Breakdown", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                }

                Button(
                    onClick = onExecutePaperTrade,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("trade_signal_${signal.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = DarkSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy to Paper", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun SignalStatusPill(status: SignalStatus) {
    val (bgColor, textColor, label) = when (status) {
        SignalStatus.ACTIVE -> Triple(NeonCyan.copy(alpha = 0.15f), NeonCyan, "ACTIVE")
        SignalStatus.TP1_HIT -> Triple(NeonGreen.copy(alpha = 0.15f), NeonGreen, "TP1 HIT ✓")
        SignalStatus.TP2_HIT -> Triple(NeonGreen.copy(alpha = 0.2f), NeonGreen, "TP2 HIT ✓✓")
        SignalStatus.TP3_HIT -> Triple(NeonGreen.copy(alpha = 0.25f), NeonGreen, "ALL TP HIT 🎯")
        SignalStatus.SL_HIT -> Triple(NeonRed.copy(alpha = 0.15f), NeonRed, "SL HIT ✕")
        SignalStatus.CLOSED -> Triple(DarkCardElevated, TextSecondary, "CLOSED")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
