package com.example.ui.screens.trading

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.TradeSide
import com.example.ui.components.PositionCard
import com.example.ui.components.TopAppBarWithGlow
import com.example.ui.theme.DarkBackground
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
fun DemoTradingScreen(
    viewModel: DemoTradingViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val openTrades by viewModel.openTrades.collectAsState()
    val closedTrades by viewModel.closedTrades.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val df = DecimalFormat("#,##0.00")

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("demo_trading_screen"),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBarWithGlow(
                title = "Virtual Paper Trading",
                subtitle = "Risk-Free Execution & PnL Simulator"
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
            // Portfolio Balance Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "VIRTUAL DEMO WALLET",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkCardElevated)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = profile.accountTier,
                                    style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "$${df.format(profile.virtualBalance)} USDT",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontSize = 28.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats: Open Positions, Win Rate, Realized PnL
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkCard)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Active Positions", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${openTrades.size}", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                            }
                            Column {
                                Text("Historical Trades", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${closedTrades.size}", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                            }
                            Column {
                                val totalPnl = closedTrades.sumOf { it.realizedPnl }
                                Text("Total Realized", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(
                                    "${if (totalPnl >= 0) "+" else ""}$${df.format(totalPnl)}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (totalPnl >= 0) NeonGreen else NeonRed
                                )
                            }
                        }
                    }
                }
            }

            // Tab Selector: 0 = Open Positions, 1 = Trade History, 2 = Manual Order
            item {
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = DarkSurface,
                    contentColor = NeonCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                            color = NeonCyan
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.setTab(0) },
                        text = {
                            Text(
                                "Open (${openTrades.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    )
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.setTab(1) },
                        text = {
                            Text(
                                "History (${closedTrades.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    )
                    Tab(
                        selected = uiState.selectedTab == 2,
                        onClick = { viewModel.setTab(2) },
                        text = {
                            Text(
                                "+ New Order",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    )
                }
            }

            // Tab Content: 0 = Open Positions
            if (uiState.selectedTab == 0) {
                if (openTrades.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(DarkCard, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No active open positions.\nCopy an AI Signal from the Signals tab or open a new manual trade!",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(openTrades, key = { it.id }) { trade ->
                        val currentMarket = when (trade.pair) {
                            "BTC/USDT" -> uiState.currentBtcPrice
                            "ETH/USDT" -> uiState.currentEthPrice
                            "SOL/USDT" -> uiState.currentSolPrice
                            else -> trade.entryPrice * 1.01
                        }
                        PositionCard(
                            trade = trade,
                            currentMarketPrice = currentMarket,
                            onClosePosition = { viewModel.closePosition(trade) }
                        )
                    }
                }
            }

            // Tab Content: 1 = Trade History
            if (uiState.selectedTab == 1) {
                if (closedTrades.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(DarkCard, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No closed trades recorded yet.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                        }
                    }
                } else {
                    items(closedTrades, key = { it.id }) { trade ->
                        PositionCard(
                            trade = trade,
                            currentMarketPrice = trade.exitPrice ?: trade.entryPrice,
                            onClosePosition = {}
                        )
                    }
                }
            }

            // Tab Content: 2 = Manual Order Form
            if (uiState.selectedTab == 2) {
                item {
                    ManualOrderForm(
                        uiState = uiState,
                        onPairChange = { viewModel.setOrderPair(it) },
                        onSideChange = { viewModel.setOrderSide(it) },
                        onLeverageChange = { viewModel.setOrderLeverage(it) },
                        onMarginChange = { viewModel.setOrderMargin(it) },
                        onTpChange = { viewModel.setOrderTp(it) },
                        onSlChange = { viewModel.setOrderSl(it) },
                        onSubmit = { viewModel.submitNewOrder() }
                    )
                }
            }
        }
    }
}

@Composable
fun ManualOrderForm(
    uiState: DemoTradingUiState,
    onPairChange: (String) -> Unit,
    onSideChange: (TradeSide) -> Unit,
    onLeverageChange: (Int) -> Unit,
    onMarginChange: (String) -> Unit,
    onTpChange: (String) -> Unit,
    onSlChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val pairs = listOf("BTC/USDT", "ETH/USDT", "SOL/USDT", "NEAR/USDT", "AVAX/USDT")
    val isLong = uiState.orderSide == TradeSide.LONG

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "OPEN NEW DEMO POSITION",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )

            // Side Selector (LONG vs SHORT)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onSideChange(TradeSide.LONG) },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLong) NeonGreen else DarkCardElevated,
                        contentColor = if (isLong) DarkSurface else TextPrimary
                    )
                ) {
                    Text("BUY / LONG", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onSideChange(TradeSide.SHORT) },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isLong) NeonRed else DarkCardElevated,
                        contentColor = if (!isLong) TextPrimary else TextSecondary
                    )
                ) {
                    Text("SELL / SHORT", fontWeight = FontWeight.Bold)
                }
            }

            // Pair Selection
            Text("Select Pair:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pairs.forEach { pair ->
                    val isSelected = uiState.orderPair == pair
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) NeonCyan else DarkSurface)
                            .clickable { onPairChange(pair) }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = pair,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) DarkSurface else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Leverage Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Leverage:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text("${uiState.orderLeverage}x", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                }
                Slider(
                    value = uiState.orderLeverage.toFloat(),
                    onValueChange = { onLeverageChange(it.toInt()) },
                    valueRange = 1f..25f,
                    steps = 23,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = DarkCardElevated
                    )
                )
            }

            // Margin Input
            OutlinedTextField(
                value = uiState.orderMargin,
                onValueChange = onMarginChange,
                label = { Text("Margin (USDT)") },
                modifier = Modifier.fillMaxWidth().testTag("order_margin_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // TP and SL Inputs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.orderTp,
                    onValueChange = onTpChange,
                    label = { Text("Take Profit ($)") },
                    modifier = Modifier.weight(1f).testTag("order_tp_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = uiState.orderSl,
                    onValueChange = onSlChange,
                    label = { Text("Stop Loss ($)") },
                    modifier = Modifier.weight(1f).testTag("order_sl_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonRed,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_order_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLong) NeonGreen else NeonRed,
                    contentColor = DarkSurface
                )
            ) {
                Text(
                    text = "Confirm ${if (isLong) "Long" else "Short"} Order",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkSurface)
                )
            }
        }
    }
}
