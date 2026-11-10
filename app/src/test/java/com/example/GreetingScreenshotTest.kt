package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.model.Signal
import com.example.domain.model.SignalStatus
import com.example.domain.model.SignalType
import com.example.ui.components.SignalCard
import com.example.ui.theme.CryptoSignalTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun signal_card_screenshot() {
    val sampleSignal = Signal(
      id = "sig-sample",
      pair = "BTC/USDT",
      assetSymbol = "BTC",
      type = SignalType.LONG,
      status = SignalStatus.ACTIVE,
      entryPrice = 96500.0,
      currentPrice = 97100.0,
      targetPrice1 = 98200.0,
      targetPrice2 = 99800.0,
      targetPrice3 = 102500.0,
      stopLoss = 94800.0,
      leverage = "10x",
      timeframe = "4h",
      confidenceScore = 95,
      riskLevel = com.example.domain.model.RiskLevel.MEDIUM,
      aiReasoning = "Bullish flag breakout confirmed by volume.",
      technicalIndicators = listOf("RSI: 58.4", "MACD: Bullish Cross"),
      timestamp = System.currentTimeMillis()
    )

    composeTestRule.setContent {
      CryptoSignalTheme {
        SignalCard(
          signal = sampleSignal,
          onCardClick = {},
          onExecutePaperTrade = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
