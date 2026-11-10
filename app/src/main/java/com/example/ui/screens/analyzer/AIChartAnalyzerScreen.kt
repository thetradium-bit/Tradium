package com.example.ui.screens.analyzer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AIAnalysisResult
import com.example.ui.components.CandlestickChart
import com.example.ui.components.TopAppBarWithGlow
import com.example.ui.theme.DarkBackground
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
fun AIChartAnalyzerScreen(
    viewModel: AIAnalyzerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val df = DecimalFormat("#,##0.00")

    LaunchedEffect(uiState.tradeSuccessMessage) {
        uiState.tradeSuccessMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearTradeSuccessMessage()
        }
    }

    val pairs = listOf("BTC/USDT", "ETH/USDT", "SOL/USDT", "NEAR/USDT", "AVAX/USDT", "LINK/USDT", "XRP/USDT")
    val timeframes = listOf("15m", "1h", "4h", "1D")
    val strategies = listOf(
        "Smart Money Concepts (SMC)",
        "Breakout & Momentum",
        "RSI & Divergence Scanner",
        "Harmonic Liquidity Flow"
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_chart_analyzer_screen"),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBarWithGlow(
                title = "AI Chart Analyzer",
                subtitle = "Deep Neural Pattern & Oscillator Vision"
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Pair Selector Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pairs.forEach { pair ->
                        val isSelected = uiState.selectedPair == pair
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonCyan else DarkCard)
                                .clickable { viewModel.setPair(pair) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("pair_select_$pair")
                        ) {
                            Text(
                                text = pair,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isSelected) DarkSurface else TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Timeframe & Refresh Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        timeframes.forEach { tf ->
                            val isSelected = uiState.selectedTimeframe == tf
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) DarkCardElevated else DarkSurface)
                                    .clickable { viewModel.setTimeframe(tf) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tf,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) NeonCyan else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.runAnalysis() },
                        enabled = !uiState.isAnalyzing,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkCardElevated,
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier.height(34.dp).testTag("reanalyze_button")
                    ) {
                        if (uiState.isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = NeonCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = NeonCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan AI", style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Interactive Candlestick Chart View with AI Pattern Vision
            item {
                CandlestickChart(
                    candles = uiState.candles,
                    showEma = true,
                    showVolume = true,
                    showAiOverlays = true,
                    aiPatterns = uiState.aiPatterns,
                    aiPriceLevels = uiState.aiPriceLevels,
                    isScanningAi = uiState.isAnalyzing,
                    selectedPattern = uiState.selectedPattern,
                    onPatternSelected = { pattern ->
                        viewModel.selectPattern(pattern)
                    }
                )
            }

            // Strategy Selector
            item {
                Text(
                    text = "AI SCANNING STRATEGY",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextMuted, fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    strategies.forEach { strat ->
                        val isSelected = uiState.selectedStrategy == strat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonPurple.copy(alpha = 0.2f) else DarkCard)
                                .clickable { viewModel.setStrategy(strat) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = strat,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) NeonPurple else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            // AI Scanner Result Breakdown
            uiState.analysisResult?.let { result ->
                item {
                    AIAnalysisResultCard(
                        result = result,
                        onTradeClick = { viewModel.executePaperTradeFromAnalysis() }
                    )
                }
            }
        }
    }
}

@Composable
fun AIAnalysisResultCard(
    result: AIAnalysisResult,
    onTradeClick: () -> Unit
) {
    val df = DecimalFormat("#,##0.00")
    val isBullish = result.bias.contains("BULLISH")
    val biasColor = if (isBullish) NeonGreen else NeonRed

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_analysis_result_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Bias Header & Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(biasColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = result.bias,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = biasColor,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Confidence: ${result.confidence}%",
                        style = MaterialTheme.typography.labelMedium.copy(color = NeonCyan, fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "R:R ${result.riskReward}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detected Pattern Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface)
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeonPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Detected Pattern: ${result.patternDetected}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = result.summary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recommended Action Levels Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Rec. Entry", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("$${df.format(result.recommendedEntry)}", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
                Column {
                    Text("Take Profit 1", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("$${df.format(result.recommendedTp1)}", style = MaterialTheme.typography.labelMedium, color = NeonGreen)
                }
                Column {
                    Text("Take Profit 2", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("$${df.format(result.recommendedTp2)}", style = MaterialTheme.typography.labelMedium, color = NeonGreen)
                }
                Column {
                    Text("Stop Loss", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("$${df.format(result.recommendedSl)}", style = MaterialTheme.typography.labelMedium, color = NeonRed)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Indicator Checklist
            Text(
                text = "TECHNICAL OSCILLATOR BREAKDOWN",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(6.dp))

            result.indicators.forEach { indicator ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (indicator.interpretation.contains("BULLISH")) NeonGreen else NeonRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(indicator.name, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                    }
                    Text(indicator.value, style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action: Copy to Demo Trade
            Button(
                onClick = onTradeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("execute_ai_analysis_trade_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = DarkSurface
                )
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Execute Recommended Trade on Demo ($500 Margin)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DarkSurface
                    )
                )
            }
        }
    }
}
