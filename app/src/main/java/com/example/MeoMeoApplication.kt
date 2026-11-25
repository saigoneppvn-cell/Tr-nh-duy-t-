package com.example

import android.app.Application
import com.example.browser.DownloadHelper
import com.example.browser.GeckoRuntimeManager
import com.example.data.local.AppDatabase
import com.example.data.preferences.BrowserPreferencesRepository
import com.example.data.repository.BrowserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MeoMeoApplication : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val database by lazy { AppDatabase.getInstance(this) }
    val preferencesRepository by lazy { BrowserPreferencesRepository(this) }
    val browserRepository by lazy { BrowserRepository(database) }
    val geckoRuntimeManager by lazy { GeckoRuntimeManager(this) }
    val downloadHelper by lazy { DownloadHelper(this, browserRepository, appScope) }
}
