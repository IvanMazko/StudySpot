package com.example.studyspot.presentation.activities

import android.os.Bundle
import android.app.AlertDialog
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.studyspot.app.StudySpotApplication
import com.example.studyspot.presentation.screens.seatmap.SeatMapRoute
import com.example.studyspot.presentation.theme.StudySpotTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            try {
                (application as StudySpotApplication).databaseInitialization.await()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage("Не удалось подготовить базу данных.")
                    .setPositiveButton("Закрыть") { _, _ -> finish() }
                    .setCancelable(false)
                    .show()
                return@launch
            }
            setContent { StudySpotApp() }
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
