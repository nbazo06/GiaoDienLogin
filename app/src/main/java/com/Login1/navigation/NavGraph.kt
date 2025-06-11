package com.Login1.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.Login1.GiaoDienLogin.LoginScreen
import com.Login1.GiaoDienLogin.RegisterScreen
import com.Login1.GiaoDienLogin.ForgotPasswordScreen
import com.Login1.GiaoDienLogin.EmailConfirmation
import com.Login1.GiaoDienLogin.NewPasswordScreen
import com.Login1.GiaoDienChinh.HomeScreen
import com.Login1.GiaoDienChinh.AddTransactionScreen

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
        composable(route = "forgot_password_screen") {
            ForgotPasswordScreen(navController = navController)
        }
        composable(route = "email_confirmation_screen/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            EmailConfirmation(navController = navController, email)
        }
        composable(route = "new_password_screen/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            NewPasswordScreen(navController = navController, email)
        }
        composable(route = "home_screen/{account_id}") { backStackEntry ->
            val account_id = backStackEntry.arguments?.getString("account_id") ?: ""
            HomeScreen(navController = navController, account_id)
        }
        composable(route = "add_transaction_screen/{account_id}") { backStackEntry ->
            val account_id = backStackEntry.arguments?.getString("account_id") ?: ""
            AddTransactionScreen(navController = navController, account_id)
        }
    }
} 