package com.example.attendify.ui.theme.admin


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.attendify.face.FaceEmbedder
import com.example.attendify.ui.theme.ContextHolder
import com.example.attendify.ui.theme.camera.CapturedFrame
import com.example.attendify.ui.theme.camera.FaceCaptureScreen
import kotlinx.coroutines.launch

private sealed class EnrollState {
    data object Capturing : EnrollState()
    data object Processing : EnrollState()
    data object Success : EnrollState()
    data class Error(val message: String) : EnrollState()
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.camera.core.ExperimentalGetImage
@Composable
fun EnrollFaceScreen(
    staffId: Long,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    var state by remember { mutableStateOf<EnrollState>(EnrollState.Capturing) }
    val scope = rememberCoroutineScope()
    val embedder = remember { FaceEmbedder(ContextHolder.appInstance) }
    val repository = ContextHolder.appInstance.repository

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enroll Face", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val current = state) {
                is EnrollState.Capturing -> {
                    FaceCaptureScreen(
                        instructionText = "Look straight at the camera to enroll your face",
                        onCapture = { frame: CapturedFrame ->
                            state = EnrollState.Processing
                            scope.launch {
                                val embedding = embedder.embed(frame.bitmap)
                                if (embedding == null) {
                                    state = EnrollState.Error(
                                        "Couldn't read the face clearly. Try better lighting and face the camera directly."
                                    )
                                } else {
                                    repository.enrollFace(staffId, embedding)
                                    state = EnrollState.Success
                                }
                            }
                        },
                        onCancel = onCancel
                    )
                }
                is EnrollState.Processing -> EnrollStatusCard(
                    title = "Processing face...",
                    subtitle = "This will just take a moment",
                    tone = EnrollTone.NEUTRAL,
                    showSpinner = true
                )
                is EnrollState.Success -> EnrollStatusCard(
                    title = "Face enrolled successfully!",
                    subtitle = "This staff member can now check in with face verification.",
                    tone = EnrollTone.SUCCESS,
                    icon = Icons.Default.CheckCircle
                ) {
                    Button(
                        onClick = onDone,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.SemiBold)
                    }
                }
                is EnrollState.Error -> EnrollStatusCard(
                    title = "Enrollment failed",
                    subtitle = current.message,
                    tone = EnrollTone.ERROR,
                    icon = Icons.Default.Warning
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        Button(onClick = { state = EnrollState.Capturing }, modifier = Modifier.weight(1f)) { Text("Retry") }
                    }
                }
            }
        }
    }
}

private enum class EnrollTone { NEUTRAL, SUCCESS, ERROR }

@Composable
private fun EnrollStatusCard(
    title: String,
    subtitle: String,
    tone: EnrollTone,
    icon: ImageVector? = null,
    showSpinner: Boolean = false,
    actions: (@Composable ColumnScope.() -> Unit)? = null
) {
    val (containerColor, contentColor) = when (tone) {
        EnrollTone.NEUTRAL -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) to MaterialTheme.colorScheme.primary
        EnrollTone.SUCCESS -> Color(0xFFDDF3E4) to Color(0xFF1E9E4E)
        EnrollTone.ERROR -> Color(0xFFFBE1E1) to Color(0xFFE0453C)
    }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (showSpinner) {
                        CircularProgressIndicator(color = contentColor, modifier = Modifier.size(28.dp))
                    } else if (icon != null) {
                        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(30.dp))
                    } else {
                        Icon(Icons.Default.Face, contentDescription = null, tint = contentColor, modifier = Modifier.size(30.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (actions != null) {
                    Spacer(Modifier.height(20.dp))
                    actions()
                }
            }
        }
    }
}