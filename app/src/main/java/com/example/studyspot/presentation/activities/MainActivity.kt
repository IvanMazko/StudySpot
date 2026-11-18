package com.example.studyspot.presentation.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.studyspot.domain.models.TimeSlot
import com.example.studyspot.presentation.screens.seatmap.SeatMapRoute
import com.example.studyspot.presentation.theme.StudySpotTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime

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
    // Temporary parameters until date and time selection is implemented.
    val date = remember { LocalDate.now() }
    val timeSlot = remember {
        TimeSlot("morning", LocalTime.of(9, 0), LocalTime.of(12, 0))
    }
    StudySpotTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            SeatMapRoute(
                date = date,
                timeSlot = timeSlot,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
