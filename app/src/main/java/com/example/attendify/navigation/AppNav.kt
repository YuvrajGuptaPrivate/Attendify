package com.example.attendify.navigation


sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object AdminDashboard : Screen("admin_dashboard")
    data object StaffList : Screen("staff_list")
    data object AddStaff : Screen("add_staff")
    data object StaffProfile : Screen("staff_profile/{staffId}") {
        fun createRoute(staffId: Long) = "staff_profile/$staffId"
    }
    data object EnrollFace : Screen("enroll_face/{staffId}") {
        fun createRoute(staffId: Long) = "enroll_face/$staffId"
    }
    data object MarkAttendance : Screen("mark_attendance/{staffId}") {
        fun createRoute(staffId: Long) = "mark_attendance/$staffId"
    }
    data object StaffDashboard : Screen("staff_dashboard")
}