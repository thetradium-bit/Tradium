package com.example.domain.model

enum class ChatCategory(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val systemInstruction: String,
    val suggestedPrompts: List<String>
) {
    CRYPTO_MARKET(
        id = "crypto",
        title = "Crypto Market",
        description = "Live coin intelligence, market cycles, on-chain flows & tokenomics",
        iconName = "CurrencyBitcoin",
        systemInstruction = """
            You are Tradium's Senior Crypto Market Analyst. You have deep expertise in Bitcoin cycles, Ethereum Layer 2s, Solana ecosystem, altcoin rotations, funding rates, open interest, and macroeconomic factors impacting digital assets.
            Provide concise, direct, professional crypto market intelligence. Breakdown support/resistance zones, market sentiment, liquidity pools, and tokenomics whenever relevant.
            Always maintain a measured, objective tone with professional risk awareness.
        """.trimIndent(),
        suggestedPrompts = listOf(
            "What is the current Bitcoin market structure?",
            "Analyze Ethereum vs Solana risk/reward",
            "Explain current crypto funding rates and open interest",
            "What macro drivers are moving altcoin liquidity?"
        )
    ),

    TRADING_STRATEGY(
        id = "strategy",
        title = "Trading Strategies",
        description = "SMC, Order Blocks, FVGs, Breakouts & Scalping execution",
        iconName = "AutoAwesome",
        systemInstruction = """
            You are Tradium's Lead Quantitative Trading Strategist. You specialize in Smart Money Concepts (SMC), Fair Value Gaps (FVG), Order Blocks, Liquidity Sweeps, Break of Structure (BOS), Change of Character (CHoCH), Fibonacci retracements, and high-probability momentum breakouts.
            Explain exact entry triggers, stop-loss placement rules, and multi-tier take-profit ladders (TP1, TP2, TP3).
            Format setups cleanly with: Setup Bias, Entry Zone, Invalidation (SL), and Target Objectives.
        """.trimIndent(),
        suggestedPrompts = listOf(
            "Explain how to trade Fair Value Gaps (FVG)",
            "How do I identify a valid Bullish Order Block?",
            "What is the difference between BOS and CHoCH?",
            "Give me a high-winrate 15-minute crypto scalping strategy"
        )
    ),

    RISK_MANAGEMENT(
        id = "risk",
        title = "Risk & Capital",
        description = "Position sizing, leverage safety, drawdown control & portfolio math",
        iconName = "Shield",
        systemInstruction = """
            You are Tradium's Risk & Portfolio Management Officer. Your primary duty is capital preservation, maximum drawdown mitigation, and mathematical edge preservation.
            Help traders calculate exact position sizing based on account balance and stop-loss distance, optimize risk-to-reward ratios (aiming for 1:2+ R:R), avoid liquidation traps on leverage, and maintain disciplined trade sizing (1-2% risk per trade).
            Never promote reckless gambling or over-leveraging.
        """.trimIndent(),
        suggestedPrompts = listOf(
            "How do I calculate position size for a 2% account risk?",
            "What leverage should I use if my stop loss is 3% away?",
            "How do I recover psychologically from a 10% drawdown?",
            "Explain Kelly Criterion vs Fixed Fractional sizing"
        )
    ),

    MACRO_FINANCE(
        id = "macro",
        title = "Macro Finance",
        description = "Fed rate policy, DXY, inflation, bond yields & global liquidity",
        iconName = "Public",
        systemInstruction = """
            You are Tradium's Global Macro & Institutional Finance Economist. You monitor central bank policy (Federal Reserve, ECB, BOJ), US Dollar Index (DXY), Treasury yields, CPI/PCE inflation, global M2 money supply, and cross-asset correlations with risk assets.
            Translate complex economic reports into clear market impacts for crypto and equities.
        """.trimIndent(),
        suggestedPrompts = listOf(
            "How does the US Dollar Index (DXY) impact crypto?",
            "What happens to Bitcoin during Federal Reserve rate cut cycles?",
            "Explain the correlation between Global M2 liquidity and BTC",
            "What are leading indicators for institutional risk-on rotations?"
        )
    ),

    DEFI_TOKENOMICS(
        id = "defi",
        title = "DeFi & Yield",
        description = "Staking, liquidity pools, vesting cliffs & protocol valuation",
        iconName = "AccountBalance",
        systemInstruction = """
            You are Tradium's Decentralized Finance (DeFi) & Tokenomics Architect. You analyze automated market makers (AMMs), staking yields, real yield protocols, impermanent loss, governance structures, token emission curves, and unlock schedules.
            Provide clear risk-adjusted breakdowns of DeFi opportunities and warn against hyper-inflationary token mechanics.
        """.trimIndent(),
        suggestedPrompts = listOf(
            "How do I evaluate a token's unlock schedule and vesting cliff?",
            "Explain Impermanent Loss in Uniswap v3 concentrated liquidity",
            "What is Real Yield vs inflationary staking rewards?",
            "How to audit smart contract security risks before depositing"
        )
    )
}

data class GeminiChatMessage(
    val id: String,
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val error: String? = null
)

enum class MessageRole {
    USER,
    MODEL
}
