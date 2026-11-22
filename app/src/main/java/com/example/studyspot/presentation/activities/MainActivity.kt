package com.example.studyspot.presentation.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.studyspot.presentation.screens.seatmap.SeatMapRoute
import com.example.studyspot.presentation.theme.StudySpotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudySpotApp()
        }
    }
}

@Composable
private fun StudySpotApp() {
    StudySpotTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            SeatMapRoute(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
