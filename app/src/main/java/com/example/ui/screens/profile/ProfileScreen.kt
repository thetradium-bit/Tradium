package com.example.ui.screens.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RiskCalculatorSheet
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val authState by viewModel.authState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val riskCalcSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showRiskCalc = remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBarWithGlow(
                title = "Trader Dashboard",
                subtitle = "Stats, Notification Rules & AI Config"
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
            // Profile Card
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(DarkCardElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "CS",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = NeonCyan,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile.username,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "Verified",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonPurple.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = profile.accountTier,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NeonPurple,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            if (authState.isAuthenticated) {
                                val context = androidx.compose.ui.platform.LocalContext.current
                                OutlinedButton(
                                    onClick = { viewModel.signOut(context) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.5f)),
                                    modifier = Modifier.height(34.dp).testTag("sign_out_button")
                                ) {
                                    Text("Sign Out", style = MaterialTheme.typography.labelSmall, color = NeonRed)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.setShowAuthDialog(true) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkSurface),
                                    modifier = Modifier.height(34.dp).testTag("sign_in_modal_button")
                                ) {
                                    Text("Sign In", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = DarkSurface)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkCard)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Win Rate", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${profile.winRate}%", style = MaterialTheme.typography.labelLarge, color = NeonGreen)
                            }
                            Column {
                                Text("Trades", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${profile.totalTrades}", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                            }
                            Column {
                                Text("Best ROI", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("+${DecimalFormat("#0.0").format(profile.bestTradeRoi)}%", style = MaterialTheme.typography.labelLarge, color = NeonCyan)
                            }
                        }
                    }
                }
            }

            // Quick Tools: Risk Calculator Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRiskCalc.value = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(DarkCardElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Position Size & Risk Calculator",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Calculate optimal 1-2% risk lot sizes & stop losses",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )
                            }
                        }
                    }
                }
            }

            // Notification Settings Section
            item {
                Text(
                    text = "NOTIFICATION ALERTS",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextMuted, fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        NotificationToggleRow(
                            title = "High-Confidence AI Signals",
                            subtitle = "Instant alert when 93%+ score signal triggers",
                            checked = uiState.notifyNewSignals,
                            onCheckedChange = { viewModel.toggleNotifyNewSignals(it) }
                        )

                        NotificationToggleRow(
                            title = "Take Profit (TP) Hits",
                            subtitle = "Notify when Target 1, 2, or 3 is reached",
                            checked = uiState.notifyTpHits,
                            onCheckedChange = { viewModel.toggleNotifyTpHits(it) }
                        )

                        NotificationToggleRow(
                            title = "Stop Loss Alerts",
                            subtitle = "Strict risk alerts on adverse price movements",
                            checked = uiState.notifySlAlerts,
                            onCheckedChange = { viewModel.toggleNotifySlAlerts(it) }
                        )

                        NotificationToggleRow(
                            title = "Alpha Community Mentions",
                            subtitle = "Discussions and breakout alerts from top traders",
                            checked = uiState.notifyCommunityAlpha,
                            onCheckedChange = { viewModel.toggleNotifyCommunityAlpha(it) },
                            isLast = true
                        )
                    }
                }
            }

            // AI Neural Engine Engine Status Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonPurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = NeonPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AI Neural Engine",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Gemini Flash 2.5 Multi-Timeframe Model Active",
                                    style = MaterialTheme.typography.bodySmall.copy(color = NeonGreen)
                                )
                            }
                        }
                    }
                }
            }

            // Reset Virtual Demo Account Button
            item {
                Button(
                    onClick = { viewModel.resetDemoAccount() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("reset_demo_account_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkCardElevated,
                        contentColor = NeonRed
                    ),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reset Demo Wallet to $10,000 USDT",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (showRiskCalc.value) {
        RiskCalculatorSheet(
            sheetState = riskCalcSheetState,
            accountBalance = profile.virtualBalance,
            onDismiss = { showRiskCalc.value = false }
        )
    }

    if (uiState.showAuthDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        com.example.ui.screens.auth.AuthModalDialog(
            isLoading = uiState.authLoading,
            errorMessage = uiState.authError,
            onGoogleSignIn = { viewModel.signInWithGoogle(context) },
            onSignIn = { email, pass -> viewModel.signInWithEmail(email, pass) },
            onRegister = { email, pass, user -> viewModel.registerWithEmail(email, pass, user) },
            onForgotPassword = { email -> viewModel.sendPasswordReset(email) },
            onDismiss = { viewModel.setShowAuthDialog(false) }
        )
    }
}

@Composable
fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
            Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonCyan,
                checkedTrackColor = DarkSurface,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkCardElevated
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
