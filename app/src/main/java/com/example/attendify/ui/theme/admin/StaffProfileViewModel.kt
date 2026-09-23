package com.example.attendify.ui.theme.admin


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.attendify.data.AttendanceEntity
import com.example.attendify.data.AttendanceRepository
import com.example.attendify.data.StaffEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StaffProfileViewModel(
    repository: AttendanceRepository,
    staffId: Long
) : ViewModel() {
    val staff: StateFlow<StaffEntity?> = repository.getStaffById(staffId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val attendanceHistory: StateFlow<List<AttendanceEntity>> = repository.getAttendanceForStaff(staffId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}