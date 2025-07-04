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
import com.Login1.GiaoDienChinh.BudgetScreen
import com.Login1.GiaoDienChinh.AddBudgetScreen
import com.Login1.GiaoDienChinh.TransactionHistoryScreen
import com.Login1.GiaoDienChinh.PersonalScreen

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
        composable(route = "home_screen/{user_id}") { backStackEntry ->
            val user_id = backStackEntry.arguments?.getString("user_id") ?: ""
            HomeScreen(navController = navController, user_id)
        }
        composable(route = "add_transaction_screen/{user_id}") { backStackEntry ->
            val user_id = backStackEntry.arguments?.getString("user_id") ?: ""
            AddTransactionScreen(navController = navController, user_id)
        }
        composable(route = "transaction_history_screen/{user_id}") { backStackEntry ->
            val user_id = backStackEntry.arguments?.getString("user_id") ?: ""
            TransactionHistoryScreen(navController = navController, user_id)
        }
        composable(route = "budget_screen/{user_id}") { backStackEntry ->
            val user_id = backStackEntry.arguments?.getString("user_id") ?: ""
            BudgetScreen(navController = navController, user_id)
        }
        composable(route = "add_budget_screen/{user_id}") { backStackEntry ->
            val user_id = backStackEntry.arguments?.getString("user_id") ?: ""
            AddBudgetScreen(navController = navController, user_id)
        }
        composable(route = "personal_screen/{user_id}") { backStackEntry ->
            val user_id = backStackEntry.arguments?.getString("user_id") ?: ""
            PersonalScreen(navController = navController, user_id)
        }
    }
} 