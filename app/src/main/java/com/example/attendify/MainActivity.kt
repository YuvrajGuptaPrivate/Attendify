package com.example.attendify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.attendify.navigation.Screen
import com.example.attendify.ui.theme.admin.AddStaffScreen
import com.example.attendify.ui.theme.admin.AdminDashboard
import com.example.attendify.ui.theme.admin.EnrollFaceScreen
import com.example.attendify.ui.theme.admin.MarkAttendanceScreen
import com.example.attendify.ui.theme.admin.StaffDashboard
import com.example.attendify.ui.theme.admin.StaffListScreen
import com.example.attendify.ui.theme.admin.StaffProfileScreen
import com.example.attendify.ui.theme.login.LoginScreen
import com.example.attendify.ui.theme.login.Role

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    // Clears the whole back stack and returns to Login, so the next
                    // person at this device (e.g. an admin) can sign in fresh.
                    val exitToLogin: () -> Unit = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }

                    NavHost(navController = navController, startDestination = Screen.Login.route) {
                        composable(Screen.Login.route) {
                            LoginScreen(
                                onLoginSuccess = { role ->
                                    val destination = if (role == Role.ADMIN) Screen.AdminDashboard.route else Screen.StaffDashboard.route
                                    navController.navigate(destination) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.AdminDashboard.route) {
                            AdminDashboard(
                                onAddStaff = { navController.navigate(Screen.AddStaff.route) },
                                onOpenStaff = { staffId -> navController.navigate(Screen.StaffProfile.createRoute(staffId)) },
                                onExitToLogin = exitToLogin)
                        }
                        composable(Screen.StaffList.route) {
                            StaffListScreen(
                                onAddStaff = { navController.navigate(Screen.AddStaff.route) },
                                onOpenStaff = { staffId -> navController.navigate(Screen.StaffProfile.createRoute(staffId)) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.AddStaff.route) {
                            AddStaffScreen(
                                onStaffAdded = { navController.popBackStack() },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.StaffProfile.route) { backStackEntry ->
                            val staffId = backStackEntry.arguments?.getString("staffId")?.toLongOrNull() ?: return@composable
                            StaffProfileScreen(
                                staffId = staffId,
                                onEnrollFace = { navController.navigate(Screen.EnrollFace.createRoute(staffId)) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.EnrollFace.route) { backStackEntry ->
                            val staffId = backStackEntry.arguments?.getString("staffId")?.toLongOrNull() ?: return@composable
                            EnrollFaceScreen(
                                staffId = staffId,
                                onDone = { navController.popBackStack() },
                                onCancel = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.StaffDashboard.route) {
                            StaffDashboard(
                                onMarkAttendance = { staffId ->
                                    navController.navigate(Screen.MarkAttendance.createRoute(staffId))
                                },
                                onExitToLogin = exitToLogin
                            )
                        }
                        composable(Screen.MarkAttendance.route) { backStackEntry ->
                            val staffId = backStackEntry.arguments?.getString("staffId")?.toLongOrNull() ?: return@composable
                            MarkAttendanceScreen(
                                staffId = staffId,
                                onFinished = { navController.popBackStack() },
                                onCancel = { navController.popBackStack() },
                                onExitToLogin = exitToLogin
                            )
                        }
                    }
                }
            }
        }
    }
}