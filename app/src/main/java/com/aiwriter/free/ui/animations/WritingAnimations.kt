package com.aiwriter.free.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

val aiEnterTransition: EnterTransition = fadeIn(tween(320)) +
    slideInVertically(tween(320)) { it / 5 }
val aiExitTransition: ExitTransition = fadeOut(tween(220)) +
    slideOutVertically(tween(220)) { -it / 5 }

/** Typewriter cursor blink */
@Composable
fun cursorBlinkAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "cursor")
    return transition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(500), RepeatMode.Reverse), "cursor_blink"
    ).value
}

/** Thinking/generating pulse for AI processing states */
@Composable
fun thinkingPulseScale(): Float {
    val transition = rememberInfiniteTransition(label = "think")
    return transition.animateFloat(
        0.95f, 1.05f,
        infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        "think_scale"
    ).value
}

/** Smooth word count counter animation */
@Composable
fun AnimatedWordCount(count: Int, content: @Composable (Int) -> Unit) {
    var displayCount by remember { mutableIntStateOf(count) }
    val animated by animateIntAsState(count, tween(400), label = "word_count")
    content(animated)
}

@Composable
fun SlideInPanel(visible: Boolean, modifier: Modifier = Modifier, content: @Composable AnimatedVisibilityScope.() -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium)) { it } + fadeIn(tween(200)),
        exit = slideOutHorizontally(tween(250)) { it } + fadeOut(tween(200)),
        modifier = modifier,
        content = content
    )
}

@Composable
fun ExpandCollapseSection(expanded: Boolean, modifier: Modifier = Modifier, content: @Composable AnimatedVisibilityScope.() -> Unit) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(tween(200)),
        exit = shrinkVertically(tween(250)) + fadeOut(tween(200)),
        modifier = modifier,
        content = content
    )
}
