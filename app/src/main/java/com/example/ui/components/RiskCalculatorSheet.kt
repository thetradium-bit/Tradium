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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskCalculatorSheet(
    sheetState: SheetState,
    accountBalance: Double,
    onDismiss: () -> Unit
) {
    var balanceInput by remember { mutableStateOf(accountBalance.toInt().toString()) }
    var riskPercent by remember { mutableFloatStateOf(2f) }
    var entryInput by remember { mutableStateOf("96500") }
    var stopLossInput by remember { mutableStateOf("94200") }
    var targetInput by remember { mutableStateOf("99200") }
    var leverage by remember { mutableFloatStateOf(10f) }

    val df = DecimalFormat("#,##0.00")

    val balance = balanceInput.toDoubleOrNull() ?: 10000.0
    val entry = entryInput.toDoubleOrNull() ?: 96500.0
    val sl = stopLossInput.toDoubleOrNull() ?: 94200.0
    val target = targetInput.toDoubleOrNull() ?: 99200.0

    val maxRiskUsd = balance * (riskPercent / 100.0)
    val priceDiffSl = kotlin.math.abs(entry - sl)
    val slPercent = if (entry > 0) (priceDiffSl / entry) else 0.01

    val recommendedPositionSizeUsd = if (slPercent > 0) (maxRiskUsd / slPercent) else 0.0
    val recommendedMargin = (recommendedPositionSizeUsd / leverage).coerceAtMost(balance)

    val rewardUsd = if (entry > 0) (kotlin.math.abs(target - entry) / entry) * recommendedPositionSizeUsd else 0.0
    val rrRatio = if (maxRiskUsd > 0) (rewardUsd / maxRiskUsd) else 1.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Position Size & Risk Calculator",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Inputs
            OutlinedTextField(
                value = balanceInput,
                onValueChange = { balanceInput = it },
                label = { Text("Account Balance ($ USD)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calc_balance_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Risk % Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Risk per Trade:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text(
                        "${DecimalFormat("#0.0").format(riskPercent)}% ($${df.format(maxRiskUsd)})",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (riskPercent > 5) NeonRed else NeonCyan
                    )
                }
                Slider(
                    value = riskPercent,
                    onValueChange = { riskPercent = it },
                    valueRange = 0.5f..10f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = DarkCardElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = entryInput,
                    onValueChange = { entryInput = it },
                    label = { Text("Entry Price ($)") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("calc_entry_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = stopLossInput,
                    onValueChange = { stopLossInput = it },
                    label = { Text("Stop Loss ($)") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("calc_sl_input"),
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

            Spacer(modifier = Modifier.height(10.dp))

            // Leverage Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Selected Leverage:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text("${leverage.toInt()}x", style = MaterialTheme.typography.labelMedium, color = NeonGreen)
                }
                Slider(
                    value = leverage,
                    onValueChange = { leverage = it },
                    valueRange = 1f..25f,
                    steps = 23,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonGreen,
                        activeTrackColor = NeonGreen,
                        inactiveTrackColor = DarkCardElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Result Calculation Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "OPTIMAL POSITION SIZING",
                        style = MaterialTheme.typography.labelMedium.copy(color = NeonCyan, fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Position Value:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("$${df.format(recommendedPositionSizeUsd)}", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Recommended Margin (${leverage.toInt()}x):", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("$${df.format(recommendedMargin)}", style = MaterialTheme.typography.labelLarge, color = NeonGreen)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Max Loss at Stop Loss:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("-$${df.format(maxRiskUsd)} (${riskPercent.toInt()}%)", style = MaterialTheme.typography.labelLarge, color = NeonRed)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Est. Risk / Reward:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("1 : ${DecimalFormat("#0.0").format(rrRatio)}", style = MaterialTheme.typography.labelLarge, color = NeonCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = DarkSurface
                )
            ) {
                Text("Apply & Close", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkSurface))
            }
        }
    }
}
