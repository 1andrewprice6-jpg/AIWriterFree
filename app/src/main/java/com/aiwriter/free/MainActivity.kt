package com.aiwriter.free

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var aiEngine: LocalAIEngine
    
    private lateinit var statusText: TextView
    private lateinit var downloadButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    
    private var downloadService: ModelDownloadService? = null
    private var isBound = false
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startDownloadService()
        } else {
            Toast.makeText(this, "Notification permission is required for background download", Toast.LENGTH_LONG).show()
        }
    }
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ModelDownloadService.LocalBinder
            downloadService = binder.getService()
            isBound = true
            
            downloadService?.setCallback(object : ModelDownloadService.DownloadCallback {
                override fun onProgress(downloaded: Long, total: Long, percent: Int) {
                    runOnUiThread {
                        progressBar.progress = percent
                        progressText.text = "${downloaded / 1024 / 1024} MB / ${total / 1024 / 1024} MB"
                    }
                }
                
                override fun onComplete(success: Boolean) {
                    runOnUiThread {
                        if (success) {
                            scope.launch {
                                aiEngine.initialize()
                                statusText.text = "✓ Download Complete!\n\nAI Writer is ready to use."
                                downloadButton.visibility = View.GONE
                                progressBar.visibility = View.GONE
                                progressText.visibility = View.GONE
                            }
                        } else {
                            statusText.text = "Download failed. Please try again."
                            downloadButton.isEnabled = true
                            progressBar.visibility = View.GONE
                            progressText.visibility = View.GONE
                        }
                    }
                }
                
                override fun onError(error: String) {
                    runOnUiThread {
                        statusText.text = "Error: $error\n\nPlease try again."
                        downloadButton.isEnabled = true
                        progressBar.visibility = View.GONE
                        progressText.visibility = View.GONE
                    }
                }
            })
            
            // If service is already downloading, update UI
            if (downloadService?.isDownloading() == true) {
                downloadButton.isEnabled = false
                progressBar.visibility = View.VISIBLE
                progressText.visibility = View.VISIBLE
                statusText.text = "Downloading AI model..."
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            downloadService = null
            isBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        aiEngine = (application as AIWriterApplication).aiEngine
        
        statusText = findViewById(R.id.statusText)
        downloadButton = findViewById(R.id.downloadButton)
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
        
        downloadButton.setOnClickListener {
            downloadModel()
        }
        
        updateUI()
    }
    
    override fun onStart() {
        super.onStart()
        // Bind to service
        val intent = Intent(this, ModelDownloadService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    override fun onStop() {
        super.onStop()
        if (isBound) {
            downloadService?.setCallback(null)
            unbindService(serviceConnection)
            isBound = false
        }
    }
    
    private fun updateUI() {
        if (aiEngine.isModelDownloaded()) {
            statusText.text = "✓ AI Model Ready\n\nSelect text anywhere and choose 'AI Writer' from the menu."
            downloadButton.visibility = View.GONE
            progressBar.visibility = View.GONE
            progressText.visibility = View.GONE
        } else {
            statusText.text = "AI Writer needs to download a 1.5GB model file.\n\nThis is a one-time download."
            downloadButton.visibility = View.VISIBLE
            downloadButton.text = "Download Model (1.5GB)"
            progressBar.visibility = View.GONE
            progressText.visibility = View.GONE
        }
    }
    
    private fun downloadModel() {
        // Show info that model download is not needed
        Toast.makeText(this, "This version uses cloud AI - no download needed!", Toast.LENGTH_LONG).show()
        
        statusText.text = """✓ AI Writer Ready!
            
Uses cloud AI (no download needed)

Select text anywhere and choose:
• AI Writer (for cloud AI features)
• Formatting (works offline)""".trimIndent()
        
        downloadButton.visibility = View.GONE
        progressBar.visibility = View.GONE
        progressText.visibility = View.GONE
    }
    
    private fun startDownloadService() {
        try {
            Toast.makeText(this, "startDownloadService() called", Toast.LENGTH_LONG).show()
            
            downloadButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
            progressText.visibility = View.VISIBLE
            statusText.text = "Downloading AI model..."
            
            val intent = Intent(this, ModelDownloadService::class.java).apply {
                action = ModelDownloadService.ACTION_START_DOWNLOAD
            }
            
            Toast.makeText(this, "Intent created, starting service...", Toast.LENGTH_LONG).show()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
                Toast.makeText(this, "Foreground service started!", Toast.LENGTH_LONG).show()
            } else {
                startService(intent)
                Toast.makeText(this, "Service started!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            val errorMsg = "ERROR starting service: ${e.message}\n${e.stackTraceToString()}"
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            Log.e("MainActivity", errorMsg, e)
            downloadButton.isEnabled = true
            progressBar.visibility = View.GONE
            progressText.visibility = View.GONE
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
