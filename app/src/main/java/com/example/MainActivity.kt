package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.screens.SrgCarHireMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CarHireViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CarHireViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkMode.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                SrgCarHireMainScreen(viewModel = viewModel)
            }
        }
    }
}
