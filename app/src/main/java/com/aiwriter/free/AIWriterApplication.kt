package com.aiwriter.free

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AIWriterApplication : Application() {
    
    lateinit var aiEngine: LocalAIEngine
        private set
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        aiEngine = LocalAIEngine(this)
        
        // Initialize AI engine in background
        applicationScope.launch {
            aiEngine.initialize()
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        aiEngine.release()
    }
}
