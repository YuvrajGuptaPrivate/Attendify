package com.example.attendify.ui.theme.admin




import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.attendify.ui.theme.SimpleViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStaffScreen(
    onStaffAdded: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: StaffListViewModel = viewModel(
        factory = SimpleViewModelFactory { StaffListViewModel(it) }
    )
    // We reuse StaffListViewModel just for its repository access isn't ideal long-term,
    // but adding a dedicated AddStaffViewModel is 1 extra file for 1 extra function.
    // Kept simple deliberately — see repository call below via a local scope instead.

    var name by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val repository = com.example.attendify.ui.theme.ContextHolder.appInstance.repository

    Scaffold(topBar = { TopAppBar(title = { Text("Add Staff") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = employeeId,
                onValueChange = { employeeId = it; errorMessage = null },
                label = { Text("Employee ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                enabled = !isSaving,
                onClick = {
                    if (name.isBlank() || employeeId.isBlank()) {
                        errorMessage = "Both fields are required"
                        return@Button
                    }
                    isSaving = true
                    scope.launch {
                        repository.addStaff(employeeId.trim(), name.trim())
                        isSaving = false
                        onStaffAdded()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSaving) "Saving..." else "Save")
            }
        }
    }
}