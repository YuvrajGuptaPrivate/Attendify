package com.example.attendify.ui.theme


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.attendify.AttendanceApp
import com.example.attendify.data.AttendanceRepository

class SimpleViewModelFactory(
    private val create: (AttendanceRepository) -> ViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = ContextHolder.appInstance
        return create(app.repository) as T
    }
}

object ContextHolder {
    lateinit var appInstance: AttendanceApp
}