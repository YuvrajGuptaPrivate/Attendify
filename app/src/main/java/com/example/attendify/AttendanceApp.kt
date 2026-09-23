package com.example.attendify


import android.app.Application
import com.example.attendify.data.AppDatabase
import com.example.attendify.data.AttendanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AttendanceApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val repository: AttendanceRepository by lazy {
        val db = AppDatabase.getInstance(this)
        AttendanceRepository(db.staffDao(), db.attendanceDao())
    }

    override fun onCreate() {
        super.onCreate()
        com.example.attendify.ui.theme.ContextHolder.appInstance = this

        applicationScope.launch {
            repository.seedDemoStaffIfEmpty()
        }
    }
}