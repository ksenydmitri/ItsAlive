package com.furiqd.home.presentation.screen.petdashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.furiqd.home.presentation.element.DraggableToolItem
import com.furiqd.shared.config.PetResourceConfig
import com.furiqd.shared.domain.model.JointPet
import com.furiqd.shared.domain.model.PetStage
import com.furiqd.shared.ui.elements.BackButton
import com.furiqd.shared.ui.elements.ErrorSnackbar
import com.furiqd.shared.ui.theme.ItsAliveTheme

@Composable
fun PetDashboardScreen(
    modifier: Modifier = Modifier,
    state: PetDashboardState,
    onEvent: (PetDashboardEvent) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            val pet = state.pet ?: JointPet()
            var petTargetBounds by remember { mutableStateOf<Rect?>(null) }
            var isHoveringPet by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BackButton(
                            onClick = { onNavigateBack() }
                        )

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = "Lvl ${pet.level} • ${pet.stage.name}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                petTargetBounds = coordinates.boundsInRoot()
                            }
                    ) {
                        PetAvatarContainer(
                            pet = pet,
                            isHovered = isHoveringPet
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = pet.name,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Experience",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${pet.experience % 100} / 100 XP",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val animatedXpProgress by animateFloatAsState(
                            targetValue = (pet.experience % 100) / 100f,
                            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                            label = "xpProgress"
                        )

                        LinearProgressIndicator(
                            progress = { animatedXpProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                DraggableToolItem(
                                    label = "Покормить",
                                    targetBounds = petTargetBounds,
                                    onHoverChanged = { isHoveringPet = it },
                                    onDropSuccess = { onEvent(PetDashboardEvent.FeedPet) }
                                ) {
                                    Text("🍏", fontSize = 32.sp)
                                }

                                DraggableToolItem(
                                    label = "Играть",
                                    targetBounds = petTargetBounds,
                                    onHoverChanged = { isHoveringPet = it },
                                    onDropSuccess = { onEvent(PetDashboardEvent.PlayWithPet) }
                                ) {
                                    Text("⚽", fontSize = 32.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun PetAvatarContainer(
    pet: JointPet,
    isHovered: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "idleBreathe")
    val idleScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idleScale"
    )

    val reactionScale by animateFloatAsState(
        targetValue = if (isHovered) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "reactionScale"
    )

    Box(
        modifier = modifier
            .size(200.dp)
            .scale(idleScale * reactionScale)
            .shadow(
                elevation = if (isHovered) 16.dp else 4.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.primary
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            )
            .border(
                width = if (isHovered) 3.dp else 1.dp,
                color = if (isHovered) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        val petDrawableRes = remember(pet.stage, pet.petEggType) {
            PetResourceConfig.getPetDrawable(
                stage = pet.stage,
                eggType = pet.petEggType
            )
        }

        Image(
            painter = painterResource(id = petDrawableRes),
            contentDescription = pet.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
        )
    }
}

@Composable
fun PetDashboardRoute(
    onNavigateBack: () -> Unit
) {
    val viewModel: PetDashboardViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState()}

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data -> ErrorSnackbar(snackbarData = data) }
            )
        }
    ) { paddingValues ->
        PetDashboardScreen(
            state = state,
            onEvent = viewModel::handleEvent,
            modifier = Modifier.padding(paddingValues),
            onNavigateBack = onNavigateBack
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PetDashboardScreenPreview() {
    val mockState = PetDashboardState(
        pet = JointPet(name = "Fluffy",
            level = 2,
            experience = 140,
            stage = PetStage.CHILD),
        isLoading = false
    )
    ItsAliveTheme {
        PetDashboardScreen(
            state = mockState,
            onEvent = {}
        )
    }
}