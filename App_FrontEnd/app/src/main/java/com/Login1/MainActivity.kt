package com.Login1

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.Login1.GiaoDienLogin.R
import com.Login1.GiaoDienLogin.ui.theme.GiaoDienLoginTheme
import com.Login1.navigation.SetupNavGraph

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("main", "Cash: ${R.drawable.cash}")
        Log.d("main", "Atm: ${R.drawable.atm}")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GiaoDienLoginTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.Companion.fillMaxSize()) {
                    SetupNavGraph(navController = navController)
                }
            }/*
            val navController = rememberNavController()
           TransactionHistoryScreen(navController = navController, account_id = "123")*/
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
            TransactionHistoryScreen(navController = navController, userId = "1")

        }
    }*/
}