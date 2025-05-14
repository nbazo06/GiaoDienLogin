package com.Login1.GiaoDienLogin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.Login1.GiaoDienLogin.LoginScreen
import com.Login1.GiaoDienLogin.RegisterScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: String = "register_screen"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = "register_screen") {
            RegisterScreen(navController = navController)
        }
        composable(route = "login_screen") {
            LoginScreen(navController = navController)
        }
    }
} 