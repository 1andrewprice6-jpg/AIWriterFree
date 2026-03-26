package com.aiwriter.free.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiwriter.free.ui.theme.AIViolet60
import com.aiwriter.free.ui.theme.NeuralCyan40
import com.aiwriter.free.ui.theme.GlowPurple

/** Glowing AI response card with purple gradient */
@Composable
fun AIResponseCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(AIViolet60.copy(0.6f), NeuralCyan40.copy(0.6f))))
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

/** Animated "AI is thinking" indicator */
@Composable
fun AIThinkingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(modifier = modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        (0..2).forEach { i ->
            val scale by transition.animateFloat(
                1f, 1.6f,
                infiniteRepeatable(tween(400, delayMillis = i * 130), RepeatMode.Reverse),
                "dot_$i"
            )
            Box(
                modifier = Modifier.size(10.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .clip(RoundedCornerShape(50))
                    .background(AIViolet60)
            )
        }
    }
}

/** Gradient writing surface with HD backdrop */
@Composable
fun HDWritingCanvas(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surface
            )))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        content = content
    )
}

/** Feature chip with glow on selection */
@Composable
fun HDFeatureChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glowAlpha by animateFloatAsState(if (selected) 0.4f else 0f, tween(300), label = "chip_glow")
    Box(modifier = modifier) {
        if (selected) {
            Box(Modifier.matchParentSize().clip(RoundedCornerShape(50))
                .background(AIViolet60.copy(alpha = glowAlpha * 0.3f)))
        }
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AIViolet60.copy(0.2f),
                selectedLabelColor = AIViolet60
            )
        )
    }
}

/** Shimmer for AI loading */
@Composable
fun AILoadingShimmer(lineCount: Int = 4, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "ai_shimmer")
    val alpha by transition.animateFloat(0.2f, 0.8f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), "alpha")
    Column(modifier = modifier.padding(16.dp)) {
        repeat(lineCount) { i ->
            val widthFraction = when (i % 3) { 0 -> 1f; 1 -> 0.85f; else -> 0.7f }
            Box(Modifier.fillMaxWidth(widthFraction).height(16.dp).clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha)))
            if (i < lineCount - 1) Spacer(Modifier.height(10.dp))
        }
    }
}
