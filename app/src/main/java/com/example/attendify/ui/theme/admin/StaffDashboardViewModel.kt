package com.example.attendify.ui.theme.admin


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.attendify.data.AttendanceRepository
import com.example.attendify.data.StaffEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StaffDashboardViewModel(repository: AttendanceRepository) : ViewModel() {
    val staffList: StateFlow<List<StaffEntity>> = repository.getAllStaff()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}