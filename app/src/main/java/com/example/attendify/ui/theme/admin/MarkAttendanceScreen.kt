package com.example.attendify.ui.theme.admin


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Logout
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
import com.example.attendify.data.StaffEntity
import com.example.attendify.face.FaceEmbedder
import com.example.attendify.face.FaceMatcher
import com.example.attendify.face.saveSelfieToPrivateStorage
import com.example.attendify.location.LocationHelper
import com.example.attendify.ui.theme.ContextHolder
import com.example.attendify.ui.theme.camera.CapturedFrame
import com.example.attendify.ui.theme.camera.FaceCaptureScreen
import com.example.attendify.ui.theme.camera.RequestLocationPermissionOnce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private sealed class VerifyState {
    data object Capturing : VerifyState()
    data object Processing : VerifyState()
    data class Matched(val score: Float, val frame: CapturedFrame) : VerifyState()
    data class Mismatched(val score: Float) : VerifyState()
    data object AwaitingLocationPermission : VerifyState()
    data object SavingAttendance : VerifyState()
    data class Completed(val hasLocation: Boolean) : VerifyState()
    data class Error(val message: String) : VerifyState()
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.camera.core.ExperimentalGetImage
@Composable
fun MarkAttendanceScreen(
    staffId: Long,
    onFinished: () -> Unit,
    onCancel: () -> Unit,
    onExitToLogin: () -> Unit = {}
) {
    var state by remember { mutableStateOf<VerifyState>(VerifyState.Capturing) }
    val scope = rememberCoroutineScope()
    val embedder = remember { FaceEmbedder(ContextHolder.appInstance) }
    val locationHelper = remember { LocationHelper(ContextHolder.appInstance) }
    val repository = ContextHolder.appInstance.repository

    var pendingMatch by remember { mutableStateOf<Pair<CapturedFrame, Float>?>(null) }

    fun saveAttendance(frame: CapturedFrame, score: Float) {
        state = VerifyState.SavingAttendance
        scope.launch {
            val location = locationHelper.getCurrentLocation()
            val selfiePath = saveSelfieToPrivateStorage(ContextHolder.appInstance, frame.bitmap, staffId)
            repository.recordAttendance(
                staffId = staffId,
                latitude = location?.latitude,
                longitude = location?.longitude,
                selfiePath = selfiePath,
                matchScore = score
            )
            state = VerifyState.Completed(hasLocation = location != null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mark Attendance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onExitToLogin) {
                        Icon(Icons.Default.Logout, contentDescription = "Switch user / Admin login")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val current = state) {
                is VerifyState.Capturing -> {
                    FaceCaptureScreen(
                        instructionText = "Position your face inside the frame to mark attendance",
                        onCapture = { frame: CapturedFrame ->
                            state = VerifyState.Processing
                            scope.launch {
                                val staff: StaffEntity? = repository.getStaffById(staffId).first()
                                val enrolled = staff?.faceEmbedding
                                if (enrolled == null) {
                                    state = VerifyState.Error("No enrolled face found for this staff member. Ask an admin to enroll your face first.")
                                    return@launch
                                }
                                val liveEmbedding = embedder.embed(frame.bitmap)
                                if (liveEmbedding == null) {
                                    state = VerifyState.Error("Couldn't read the face clearly. Try better lighting and try again.")
                                    return@launch
                                }
                                val score = FaceMatcher.similarity(enrolled, liveEmbedding)
                                state = if (score >= FaceMatcher.MATCH_THRESHOLD) {
                                    VerifyState.Matched(score, frame)
                                } else {
                                    VerifyState.Mismatched(score)
                                }
                            }
                        },
                        onCancel = onCancel
                    )
                }
                is VerifyState.Processing -> StatusCard(
                    title = "Verifying face...",
                    subtitle = "Hold still for a moment",
                    tone = CardTone.NEUTRAL,
                    showSpinner = true
                )
                is VerifyState.Matched -> {
                    pendingMatch = current.frame to current.score
                    if (locationHelper.hasLocationPermission()) {
                        LaunchedEffect(current) { saveAttendance(current.frame, current.score) }
                    } else {
                        state = VerifyState.AwaitingLocationPermission
                    }
                    StatusCard(
                        title = "Face matched",
                        subtitle = "Saving your attendance...",
                        tone = CardTone.SUCCESS,
                        showSpinner = true
                    )
                }
                is VerifyState.AwaitingLocationPermission -> {
                    RequestLocationPermissionOnce {
                        pendingMatch?.let { (frame, score) -> saveAttendance(frame, score) }
                    }
                    StatusCard(
                        title = "Requesting location...",
                        subtitle = "This helps confirm where attendance was marked",
                        tone = CardTone.NEUTRAL,
                        showSpinner = true
                    )
                }
                is VerifyState.SavingAttendance -> StatusCard(
                    title = "Saving attendance...",
                    subtitle = "Almost done",
                    tone = CardTone.NEUTRAL,
                    showSpinner = true
                )
                is VerifyState.Completed -> StatusCard(
                    title = "Attendance marked",
                    subtitle = if (current.hasLocation)
                        "You're all set for today."
                    else
                        "Saved without location — location was unavailable.",
                    tone = CardTone.SUCCESS,
                    icon = Icons.Default.CheckCircle
                ) {
                    Button(
                        onClick = onFinished,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.SemiBold)
                    }
                }
                is VerifyState.Mismatched -> StatusCard(
                    title = "Face didn't match",
                    subtitle = "This doesn't look like the enrolled face for this staff member. (Score: ${"%.3f".format(current.score)})",
                    tone = CardTone.ERROR,
                    icon = Icons.Default.Warning
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        Button(onClick = { state = VerifyState.Capturing }, modifier = Modifier.weight(1f)) { Text("Retry") }
                    }
                }
                is VerifyState.Error -> StatusCard(
                    title = "Something went wrong",
                    subtitle = current.message,
                    tone = CardTone.ERROR,
                    icon = Icons.Default.Warning
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        Button(onClick = { state = VerifyState.Capturing }, modifier = Modifier.weight(1f)) { Text("Retry") }
                    }
                }
            }
        }
    }
}

private enum class CardTone { NEUTRAL, SUCCESS, ERROR }

@Composable
private fun StatusCard(
    title: String,
    subtitle: String,
    tone: CardTone,
    icon: ImageVector? = null,
    showSpinner: Boolean = false,
    actions: (@Composable ColumnScope.() -> Unit)? = null
) {
    val (containerColor, contentColor) = when (tone) {
        CardTone.NEUTRAL -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) to MaterialTheme.colorScheme.primary
        CardTone.SUCCESS -> Color(0xFFDDF3E4) to Color(0xFF1E9E4E)
        CardTone.ERROR -> Color(0xFFFBE1E1) to Color(0xFFE0453C)
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