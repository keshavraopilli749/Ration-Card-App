package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RationCardEntity
import com.example.ui.theme.GovBluePrimary
import com.example.ui.theme.GovBlueSecondary
import com.example.ui.theme.GovOrangeAccent
import kotlin.math.abs

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF64748B), // Slate 500
            letterSpacing = 1.2.sp
        ),
        modifier = modifier.padding(top = 16.dp, bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun GovHeader(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF2563EB), Color(0xFF4338CA))
                )
            )
            .padding(top = 40.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
    ) {
        Column {
            Text(
                text = "GOVERNMENT OF INDIA • E-RATION SYSTEM",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.9f)
                )
            )
        }
    }
}

@Composable
fun RationCardBadge(card: RationCardEntity) {
    val gradient = when (card.category) {
        "AAY" -> Brush.linearGradient(listOf(Color(0xFFEA580C), Color(0xFFC2410C))) // Deep premium orange-red gradient
        "BPL" -> Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF4338CA))) // Beautiful blue-indigo gradient from High Density spec
        else -> Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF0F766E))) // Emerald-teal gradient
    }

    Card(
        shape = RoundedCornerShape(24.dp), // rounded-3xl
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("ration_card_visual_badge")
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "RATION CARD ID",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = card.cardNo,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${card.category} CATEGORY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Divider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "NEXT ALLOCATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "July 2026",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "VERIFIED STATUS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "E-KYC Completed",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommodityProgressMeter(name: String, collected: Double, entitled: Double, unit: String) {
    val ratio = if (entitled > 0) (collected / entitled).toFloat() else 0f
    val progressColor = if (ratio >= 1.0f) Color(0xFF10B981) else Color(0xFF2563EB) // Emerald for fully collected, Blue for in-progress

    val emoji = when {
        name.contains("Rice", ignoreCase = true) -> "🍚"
        name.contains("Wheat", ignoreCase = true) -> "🌾"
        name.contains("Sugar", ignoreCase = true) -> "🍭"
        else -> "🛢️"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)), // Slate 200 border
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                    ) {
                        Text(emoji, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = name.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B), // Slate 500
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${entitled.toInt()} $unit entitled",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${collected.toInt()} $unit".uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = if (ratio >= 1.0f) Color(0xFF10B981) else Color(0xFF0F172A)
                        )
                    )
                    Text(
                        text = "${(ratio * 100).toInt()}% COLLECTED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (ratio >= 1.0f) Color(0xFF10B981) else Color(0xFF64748B),
                            fontSize = 9.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { ratio.coerceIn(0f, 1f) },
                color = progressColor,
                trackColor = Color(0xFFF1F5F9),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

// Draw a highly realistic QR Code block-matrix on-the-fly inside dynamic Compose Canvas
@Composable
fun CanvasQRCode(token: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(160.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(2.dp, GovBluePrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val size = this.size.width
            val gridSize = 15 // 15x15 matrix representation
            val cellSize = size / gridSize

            // Simple deterministic hashing function to draw unique repeatable QR matrices
            val hash = abs(token.hashCode())

            for (row in 0 until gridSize) {
                for (col in 0 until gridSize) {
                    // Position Detection patterns in QR Code (Top-Left, Top-Right, Bottom-Left)
                    val isFinderPattern = (row < 4 && col < 4) ||
                            (row < 4 && col >= gridSize - 4) ||
                            (row >= gridSize - 4 && col < 4)

                    val isInnerFinder = (row == 0 || row == 3 || col == 0 || col == 3) &&
                            ((row < 4 && col < 4) ||
                                    (row < 4 && col >= gridSize - 4) ||
                                    (row >= gridSize - 4 && col < 4))

                    val isFinderCenter = (row == 1.5.toInt() && col == 1.5.toInt()) ||
                            (row == 1.5.toInt() && col == (gridSize - 2.5).toInt()) ||
                            (row == (gridSize - 2.5).toInt() && col == 1.5.toInt())

                    val fillCell = when {
                        isFinderCenter -> true
                        isInnerFinder -> true
                        isFinderPattern -> false
                        else -> {
                            // Deterministic pseudorandom noise
                            val cellIndex = row * gridSize + col
                            ((hash shr (cellIndex % 31)) and 1) == 1
                        }
                    }

                    if (fillCell) {
                        drawRect(
                            color = Color(0xFF101B2B),
                            topLeft = Offset(col * cellSize, row * cellSize),
                            size = Size(cellSize + 0.5f, cellSize + 0.5f) // overlapping subpixels to prevent tiny lines
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomAnalyticsChart(title: String, dataPoints: List<Float>, labels: List<String>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barCount = dataPoints.size
                val maxVal = dataPoints.maxOrNull() ?: 1.0f
                val spacing = 20.dp.toPx()
                val totalSpacing = spacing * (barCount + 1)
                val barWidth = (canvasWidth - totalSpacing) / barCount

                for (i in 0 until barCount) {
                    val barHeight = (dataPoints[i] / maxVal) * (canvasHeight - 20.dp.toPx())
                    val x = spacing + i * (barWidth + spacing)
                    val y = canvasHeight - barHeight

                    // Draw bar
                    drawRect(
                        brush = Brush.verticalGradient(listOf(GovBluePrimary, GovBlueSecondary)),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
