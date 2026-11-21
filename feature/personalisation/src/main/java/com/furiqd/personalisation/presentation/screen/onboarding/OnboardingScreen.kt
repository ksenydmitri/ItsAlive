package com.furiqd.personalisation.presentation.screen.onboarding

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.furiqd.personalisation.presentation.element.AvatarSelectionDialog
import com.furiqd.personalisation.R
import com.furiqd.personalisation.presentation.element.AvatarPickerSection
import com.furiqd.shared.ui.elements.LoadingScreen
import com.furiqd.shared.ui.elements.SharedTextField
import com.furiqd.shared.ui.elements.TextActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onEvent: (OnboardingEvent) -> Unit,
    state: OnboardingState
) {
    var isDialogVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading) {
            LoadingScreen()
        } else {
            OnboardingMainContent(
                state = state,
                innerPadding = innerPadding,
                onEvent = onEvent,
                onOpenDialog = { isDialogVisible = true }
            )
        }

        if (isDialogVisible) {
            AvatarSelectionDialog(
                onUploadCustomClick = {
                    onEvent(OnboardingEvent.UploadCustomAvatarClicked)
                },
                onDismissRequest = {
                    isDialogVisible = false
                }
            )
        }
    }
}

@Composable
private fun OnboardingMainContent(
    state: OnboardingState,
    innerPadding: PaddingValues,
    onEvent: (OnboardingEvent) -> Unit,
    onOpenDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.furiqd.shared.ui.elements.BackButton(onClick = {})
            Box(
                modifier = Modifier
                    .background(
                        color = androidx.compose.ui.graphics.Color(0xFFE9E7EA),
                        shape = RoundedCornerShape(99.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Step 1 of 2",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = androidx.compose.ui.graphics.Color(0xFF76727D)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Eyebrow
        Box(
            modifier = Modifier
                .align(Alignment.Start)
                .background(
                    color = androidx.compose.ui.graphics.Color(0xFFE7F9F7),
                    shape = RoundedCornerShape(99.dp)
                )
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = "MAKE IT YOURS",
                color = androidx.compose.ui.graphics.Color(0xFF258A83),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Complete Your\nProfile ✨",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.03).sp
            ),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tell your partner who you are.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(20.dp))

        val parsedPresetColor = try {
            androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(state.selectedPresetColorHex))
        } catch (_: Exception) {
            androidx.compose.ui.graphics.Color(0xFFFFD2C2)
        }

        AvatarPickerSection(
            localAvatarUri = state.localAvatarUri,
            isAvatarChosen = state.selectedCustomBytes != null,
            selectedPresetColor = parsedPresetColor,
            onPresetSelected = { color ->
                val red = (color.red * 255).toInt()
                val green = (color.green * 255).toInt()
                val blue = (color.blue * 255).toInt()
                val hex = String.format("#%02X%02X%02X", red, green, blue)
                onEvent(OnboardingEvent.PresetColorSelected(hex))
            },
            onClick = onOpenDialog
        )

        Spacer(modifier = Modifier.height(24.dp))

        SharedTextField(
            value = state.name,
            onValueChange = { onEvent(OnboardingEvent.NameChanged(it)) },
            label = "Your Name",
            placeholder = "Ksenia",
            isError = state.isNameError,
            shape = RoundedCornerShape(18.dp),
            errorMessage = if (state.isNameError) stringResource(R.string.onboarding_error_name) else null
        )

        Spacer(modifier = Modifier.height(16.dp))

        SharedTextField(
            value = state.nickname,
            onValueChange = { onEvent(OnboardingEvent.NicknameChanged(it)) },
            label = "Username / Nickname",
            placeholder = "ksenia_42",
            prefixText = "@",
            isError = state.isNicknameError || state.isNicknameTaken,
            shape = RoundedCornerShape(18.dp),
            errorMessage = when {
                state.isNicknameTaken -> "Username is already taken"
                state.isNicknameError -> stringResource(R.string.onboarding_error_nickname)
                else -> null
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        TextActionButton(
            text = "Save Profile & Continue →",
            enabled = state.isNextButtonEnabled,
            onClick = { onEvent(OnboardingEvent.NextClicked) }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun OnboardingRoute(
    onNavigateToNext: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val maxFileSize = 5 * 1024 * 1024 // 5 MB

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val fileSize = context.contentResolver.query(
                selectedUri, null, null, null, null
            )?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                if (sizeIndex != -1) cursor.getLong(sizeIndex) else 0L
            } ?: 0L

            if (fileSize > maxFileSize) {
                return@let
            }

            val bytes = context.contentResolver.openInputStream(selectedUri)?.use { it.readBytes() }
            if (bytes != null) {
                viewModel.handleEvent(OnboardingEvent.CustomAvatarSelected(bytes, selectedUri.toString()))
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OnboardingEffect.NavigateToNext -> onNavigateToNext()
                is OnboardingEffect.ShowError -> {}
                is OnboardingEffect.UploadAvatar -> {
                    imagePickerLauncher.launch("image/*")
                }
            }
        }
    }

    OnboardingScreen(
        state = state,
        onEvent = { event ->
            viewModel.handleEvent(event)
        }
    )
}