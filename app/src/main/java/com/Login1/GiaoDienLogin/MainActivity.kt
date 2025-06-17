package com.Login1.GiaoDienLogin

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.Login1.GiaoDienChinh.AddBudgetScreen
import com.Login1.GiaoDienChinh.AddTransactionScreen
import com.Login1.GiaoDienChinh.BudgetScreen
import com.Login1.GiaoDienChinh.HomeScreen
import com.Login1.navigation.SetupNavGraph
import com.Login1.GiaoDienChinh.TransactionHistoryScreen
//import com.Login1.GiaoDienLogin.navigation.SetupNavGraph
import com.Login1.GiaoDienLogin.ui.theme.GiaoDienLoginTheme
import com.Login1.service.insertDrawableIconsIntoDatabase

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        insertDrawableIconsIntoDatabase(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GiaoDienLoginTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    SetupNavGraph(navController = navController)
                }
            }/*
            val navController = rememberNavController()
            TransactionHistoryScreen(navController = navController, account_id = "123")
            AddTransactionScreen()
            BudgetScreen(navController = navController, account_id = "123")*/
        }
    }
    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            insertDrawableIconsIntoDatabase(this)
            val navController = rememberNavController()
            HomeScreen(navController = navController, account_id = "1")
            //AddTransactionScreen(navController = navController, account_id =  "1")
            //TransactionHistoryScreen(navController = navController, userId = "1")

        }
    }*/
}




