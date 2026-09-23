package com.example.attendify.ui.theme.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.attendify.data.StaffEntity
import com.example.attendify.ui.theme.SimpleViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onAddStaff: () -> Unit,
    onOpenStaff: (Long) -> Unit
) {
    val viewModel: StaffListViewModel = viewModel(
        factory = SimpleViewModelFactory { StaffListViewModel(it) }
    )
    val staffList by viewModel.staffList.collectAsState()
    var query by remember { mutableStateOf("") }

    val filtered = remember(staffList, query) {
        if (query.isBlank()) staffList
        else staffList.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.employeeId.contains(query, ignoreCase = true)
        }
    }
    val enrolledCount = remember(staffList) { staffList.count { it.faceEmbedding != null } }
    val notEnrolledCount = staffList.size - enrolledCount

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            ManageTeamBanner()

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = staffList.size.toString(),
                    label = "Total Staff",
                    icon = Icons.Default.Groups,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = enrolledCount.toString(),
                    label = "Enrolled",
                    icon = Icons.Default.CheckCircle,
                    containerColor = Color(0xFFDDF3E4),
                    contentColor = Color(0xFF1E9E4E)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = notEnrolledCount.toString(),
                    label = "Not Enrolled",
                    icon = Icons.Default.Warning,
                    containerColor = Color(0xFFFBE1E1),
                    contentColor = Color(0xFFE0453C)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Staff Members",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search by name or employee ID...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (staffList.isEmpty()) "No staff yet. Tap \"Add Staff Member\" below."
                        else "No matches for \"$query\"",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(filtered, key = { it.id }) { staff ->
                        StaffCard(staff = staff, onClick = { onOpenStaff(staff.id) })
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onAddStaff,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Staff Member", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ManageTeamBanner() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Groups,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column {
                Text("Manage your team", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(
                    "Add staff, enroll faces and view attendance records.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = contentColor)
            Text(label, style = MaterialTheme.typography.bodySmall, color = contentColor)
        }
    }
}

@Composable
private fun StaffCard(staff: StaffEntity, onClick: () -> Unit) {
    val enrolled = staff.faceEmbedding != null
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initialsOf(staff.name),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(staff.name, fontWeight = FontWeight.SemiBold)
                Text(
                    staff.employeeId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AssistChip(
                onClick = {},
                label = { Text(if (enrolled) "Enrolled" else "Not Enrolled") },
                leadingIcon = {
                    Icon(
                        if (enrolled) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (enrolled) Color(0xFFDDF3E4) else Color(0xFFFBE1E1),
                    labelColor = if (enrolled) Color(0xFF1E9E4E) else Color(0xFFE0453C),
                    leadingIconContentColor = if (enrolled) Color(0xFF1E9E4E) else Color(0xFFE0453C)
                )
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun initialsOf(name: String): String =
    name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }