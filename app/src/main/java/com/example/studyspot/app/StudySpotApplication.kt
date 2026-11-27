package com.example.studyspot.app

import android.app.Application
import com.example.studyspot.data.local.DatabaseSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

@HiltAndroidApp
class StudySpotApplication : Application() {
    @Inject lateinit var databaseSeeder: DatabaseSeeder
    lateinit var databaseInitialization: Deferred<Unit>
        private set

    override fun onCreate() {
        super.onCreate()
        databaseInitialization = CoroutineScope(Dispatchers.IO).async {
            databaseSeeder.seed()
        }
    }
}
