package com.example.attendify.ui.theme.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.attendify.data.StaffEntity
import com.example.attendify.ui.theme.SimpleViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboard(
    onMarkAttendance: (Long) -> Unit,
    onExitToLogin: () -> Unit = {}
) {
    val viewModel: StaffDashboardViewModel = viewModel(
        factory = SimpleViewModelFactory { StaffDashboardViewModel(it) }
    )
    val staffList by viewModel.staffList.collectAsState()
    var selected by remember { mutableStateOf<StaffEntity?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onExitToLogin) {
                        Icon(Icons.Default.Logout, contentDescription = "Switch user / Admin login")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Who are you?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Select your name to mark today's attendance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            if (staffList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "No staff registered yet. Ask an admin to add you first.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selected?.let { "${it.name} (${it.employeeId})" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select your name") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        staffList.forEach { staff ->
                            DropdownMenuItem(
                                text = { Text("${staff.name} (${staff.employeeId})") },
                                onClick = { selected = staff; expanded = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    enabled = selected != null,
                    onClick = { selected?.let { onMarkAttendance(it.id) } },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Mark Attendance", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}