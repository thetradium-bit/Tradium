package com.example.ui.screens.signals

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RiskCalculatorSheet
import com.example.ui.components.SignalCard
import com.example.ui.components.SignalDetailSheet
import com.example.ui.components.TopAppBarWithGlow
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalsFeedScreen(
    viewModel: SignalsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val signals by viewModel.filteredSignals.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val riskCalcSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showRiskCalc = remember { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(uiState.tradeSuccessMessage) {
        uiState.tradeSuccessMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearTradeSuccessMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("signals_feed_screen"),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBarWithGlow(
                title = "Tradium",
                subtitle = "Live AI Alpha & High-Winrate Signals",
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkCardElevated)
                            .clickable { showRiskCalc.value = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("open_risk_calc_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Risk Calc",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Risk Calc",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
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
            // Hero AI Signal Generator Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(NeonPurple.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = NeonPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "AI Quantum Scanner",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = "Real-time Order Book & RSI Delta Scanner",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.generateNewAiSignal("BTC/USDT") },
                                enabled = !uiState.isGeneratingSignal,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = DarkSurface
                                ),
                                modifier = Modifier.testTag("scan_new_signal_button")
                            ) {
                                if (uiState.isGeneratingSignal) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = DarkSurface,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Scan",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Signal Filter Chips Row (All, Longs, Shorts, Spot, High Confidence)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SignalFilter.values().forEach { filter ->
                        val isSelected = uiState.selectedFilter == filter
                        val filterLabel = when (filter) {
                            SignalFilter.ALL -> "All Signals"
                            SignalFilter.LONGS -> "🟢 Longs"
                            SignalFilter.SHORTS -> "🔴 Shorts"
                            SignalFilter.SPOT -> "💎 Spot"
                            SignalFilter.HIGH_CONFIDENCE -> "⚡ 93%+ High Conf"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) NeonCyan else DarkCard)
                                .clickable { viewModel.setFilter(filter) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("filter_${filter.name}")
                        ) {
                            Text(
                                text = filterLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isSelected) DarkSurface else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            // Crypto Assets Sub-Filter (ALL, BTC, ETH, SOL, NEAR, AVAX, LINK)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val assets = listOf("ALL", "BTC", "ETH", "SOL", "NEAR", "AVAX", "LINK")
                    assets.forEach { asset ->
                        val isSelected = uiState.selectedAsset == asset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DarkCardElevated else DarkSurface)
                                .clickable { viewModel.setAssetFilter(asset) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = asset,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) NeonCyan else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            // Signal Count Indicator
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE ACTIVE SIGNALS (${signals.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NeonGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Auto-Synced (1s)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonGreen,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // Signals List
            if (signals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(DarkCard, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No signals found for selected filters.\nTry selecting 'All Signals' or scanning a new one!",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(signals, key = { it.id }) { signal ->
                    SignalCard(
                        signal = signal,
                        onCardClick = { viewModel.selectSignal(signal) },
                        onExecutePaperTrade = { viewModel.executePaperTrade(signal) }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet: Signal Breakdown
    if (uiState.selectedSignal != null) {
        SignalDetailSheet(
            signal = uiState.selectedSignal,
            sheetState = detailSheetState,
            onDismiss = { viewModel.selectSignal(null) },
            onExecuteTrade = { signal -> viewModel.executePaperTrade(signal) }
        )
    }

    // Modal Bottom Sheet: Risk Calculator
    if (showRiskCalc.value) {
        RiskCalculatorSheet(
            sheetState = riskCalcSheetState,
            accountBalance = 10000.0,
            onDismiss = { showRiskCalc.value = false }
        )
    }
}
