package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Signals : Screen("signals", "Signals", Icons.Default.Bolt)
    object Analyzer : Screen("analyzer", "AI Chart", Icons.Default.AutoAwesome)
    object AiBot : Screen("ai_bot", "AI Chatbot", Icons.Default.SmartToy)
    object Trading : Screen("trading", "Paper Trade", Icons.Default.SwapHoriz)
    object Chat : Screen("chat", "Alpha Chat", Icons.Default.Chat)
    object Leaderboard : Screen("leaderboard", "Leaderboard", Icons.Default.Leaderboard)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}
