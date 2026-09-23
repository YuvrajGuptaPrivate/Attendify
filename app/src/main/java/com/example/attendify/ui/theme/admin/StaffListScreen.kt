package com.example.attendify.ui.theme.admin


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.attendify.data.StaffEntity
import com.example.attendify.ui.theme.SimpleViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffListScreen(
    onAddStaff: () -> Unit,
    onOpenStaff: (Long) -> Unit,
    onBack: () -> Unit
) {
    val viewModel: StaffListViewModel = viewModel(
        factory = SimpleViewModelFactory { StaffListViewModel(it) }
    )
    val staffList by viewModel.staffList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Staff List") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddStaff) {
                Icon(Icons.Default.Add, contentDescription = "Add Staff")
            }
        }
    ) { padding ->
        if (staffList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No staff yet. Tap + to add one.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(staffList, key = { it.id }) { staff ->
                    StaffRow(staff = staff, onClick = { onOpenStaff(staff.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun StaffRow(staff: StaffEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(staff.name, style = MaterialTheme.typography.bodyLarge)
            Text("ID: ${staff.employeeId}", style = MaterialTheme.typography.bodySmall)
        }
        val enrolled = staff.faceEmbedding != null
        AssistChip(
            onClick = {},
            label = { Text(if (enrolled) "Enrolled" else "Not enrolled") }
        )
    }
}