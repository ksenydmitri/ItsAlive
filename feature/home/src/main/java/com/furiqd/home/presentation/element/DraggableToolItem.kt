package com.furiqd.home.presentation.element

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DraggableToolItem(
    label: String,
    targetBounds: Rect?,
    onHoverChanged: (Boolean) -> Unit,
    onDropSuccess: () -> Unit,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val animOffsetX = remember { Animatable(0f) }
    val animOffsetY = remember { Animatable(0f) }

    var initialCenter by remember { mutableStateOf<Offset?>(null) }
    var isDragging by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    if (!isDragging) {
                        initialCenter = coordinates.boundsInRoot().center
                    }
                }
                .size(72.dp)
                .zIndex(if (isDragging) 99f else 1f)
                .offset {
                    IntOffset(
                        animOffsetX.value.roundToInt(),
                        animOffsetY.value.roundToInt()
                    )
                }
                .pointerInput(targetBounds) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                animOffsetX.snapTo(animOffsetX.value + dragAmount.x)
                                animOffsetY.snapTo(animOffsetY.value + dragAmount.y)

                                val baseCenter = initialCenter
                                if (baseCenter != null && targetBounds != null) {
                                    val currentCenter = Offset(
                                        x = baseCenter.x + animOffsetX.value,
                                        y = baseCenter.y + animOffsetY.value
                                    )
                                    val isInside = targetBounds.contains(currentCenter)
                                    onHoverChanged(isInside)
                                }
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            onHoverChanged(false)

                            val baseCenter = initialCenter
                            val droppedOnTarget = if (baseCenter != null && targetBounds != null) {
                                val finalCenter = Offset(
                                    x = baseCenter.x + animOffsetX.value,
                                    y = baseCenter.y + animOffsetY.value
                                )
                                targetBounds.contains(finalCenter)
                            } else false

                            if (droppedOnTarget) {
                                onDropSuccess()
                            }

                            coroutineScope.launch {
                                launch { animOffsetX.animateTo(0f, spring(dampingRatio = 0.7f)) }
                                launch { animOffsetY.animateTo(0f, spring(dampingRatio = 0.7f)) }
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            onHoverChanged(false)
                            coroutineScope.launch {
                                launch { animOffsetX.animateTo(0f, spring(dampingRatio = 0.7f)) }
                                launch { animOffsetY.animateTo(0f, spring(dampingRatio = 0.7f)) }
                            }
                        }
                    )
                }
                .graphicsLayer {
                    val scale = if (isDragging) 1.2f else 1.0f
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(
                    elevation = if (isDragging) 12.dp else 2.dp,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}