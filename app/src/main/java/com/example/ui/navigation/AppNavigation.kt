package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ui.screens.analyzer.AIAnalyzerViewModel
import com.example.ui.screens.analyzer.AIChartAnalyzerScreen
import com.example.ui.screens.chat.CommunityChatScreen
import com.example.ui.screens.chat.CommunityChatViewModel
import com.example.ui.screens.chat.FinanceChatScreen
import com.example.ui.screens.chat.FinanceChatViewModel
import com.example.ui.screens.leaderboard.LeaderboardScreen
import com.example.ui.screens.leaderboard.LeaderboardViewModel
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.ProfileViewModel
import com.example.ui.screens.signals.SignalsFeedScreen
import com.example.ui.screens.signals.SignalsViewModel
import com.example.ui.screens.trading.DemoTradingScreen
import com.example.ui.screens.trading.DemoTradingViewModel
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun AppNavigation(
    navController: NavHostController,
    signalsViewModel: SignalsViewModel,
    analyzerViewModel: AIAnalyzerViewModel,
    financeChatViewModel: FinanceChatViewModel,
    tradingViewModel: DemoTradingViewModel,
    chatViewModel: CommunityChatViewModel,
    leaderboardViewModel: LeaderboardViewModel,
    profileViewModel: ProfileViewModel
) {
    val items = listOf(
        Screen.Signals,
        Screen.Analyzer,
        Screen.AiBot,
        Screen.Trading,
        Screen.Chat,
        Screen.Leaderboard,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Signals.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                containerColor = DarkSurface,
                tonalElevation = 0.dp
            ) {
                items.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = DarkCardElevated
                        ),
                        modifier = Modifier.testTag("nav_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Signals.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Signals.route) {
                SignalsFeedScreen(viewModel = signalsViewModel)
            }
            composable(Screen.Analyzer.route) {
                AIChartAnalyzerScreen(viewModel = analyzerViewModel)
            }
            composable(Screen.AiBot.route) {
                FinanceChatScreen(viewModel = financeChatViewModel)
            }
            composable(Screen.Trading.route) {
                DemoTradingScreen(viewModel = tradingViewModel)
            }
            composable(Screen.Chat.route) {
                CommunityChatScreen(viewModel = chatViewModel)
            }
            composable(Screen.Leaderboard.route) {
                LeaderboardScreen(viewModel = leaderboardViewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(viewModel = profileViewModel)
            }
        }
    }
}
