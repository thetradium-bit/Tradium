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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.DemoTrade
import com.example.domain.model.TradeSide
import com.example.domain.model.TradeStatus
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat

@Composable
fun PositionCard(
    trade: DemoTrade,
    currentMarketPrice: Double,
    onClosePosition: (DemoTrade) -> Unit,
    modifier: Modifier = Modifier
) {
    val df = DecimalFormat("#,##0.00")
    val isLong = trade.side == TradeSide.LONG
    val typeColor = if (isLong) NeonGreen else NeonRed

    val isOpen = trade.status == TradeStatus.OPEN
    val pnl = if (isOpen) trade.calculateUnrealizedPnl(currentMarketPrice) else trade.realizedPnl
    val roi = if (isOpen) trade.calculateRoiPercentage(currentMarketPrice) else ((trade.realizedPnl / trade.marginUsdt) * 100.0)

    val pnlColor = if (pnl >= 0) NeonGreen else NeonRed
    val pnlPrefix = if (pnl >= 0) "+" else ""

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("position_card_${trade.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, if (isOpen) DarkBorder else DarkBorder.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Pair, Side Pill, Leverage, PnL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = trade.pair,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(typeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = trade.side.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = typeColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkCardElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${trade.leverage}x",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // PnL display
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isOpen) "UNREALIZED PNL" else "REALIZED PNL",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$pnlPrefix$${df.format(pnl)}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = pnlColor,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "($pnlPrefix${DecimalFormat("#0.00").format(roi)}%)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = pnlColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats grid: Entry, Current/Exit, Margin, Liq Price
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Entry", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("$${df.format(trade.entryPrice)}", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
                Column {
                    Text(if (isOpen) "Market" else "Exit", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = "$${df.format(if (isOpen) currentMarketPrice else (trade.exitPrice ?: trade.entryPrice))}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                }
                Column {
                    Text("Margin", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("$${df.format(trade.marginUsdt)}", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
                Column {
                    Text("Liq. Price", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("$${df.format(trade.liquidationPrice)}", style = MaterialTheme.typography.labelMedium, color = NeonRed)
                }
            }

            if (isOpen) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onClosePosition(trade) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("close_position_${trade.id}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkCardElevated,
                            contentColor = TextPrimary
                        ),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text(
                            text = "Close Position at Market ($${df.format(currentMarketPrice)})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
