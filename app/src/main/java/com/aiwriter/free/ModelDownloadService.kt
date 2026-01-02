package com.aiwriter.free

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ModelDownloadService : Service() {
    
    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var downloadJob: Job? = null
    private var callback: DownloadCallback? = null
    
    private lateinit var notificationManager: NotificationManager
    private var isDownloading = false
    
    companion object {
        private const val TAG = "ModelDownloadService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "model_download_channel"
        private const val CHANNEL_NAME = "Model Downloads"
        
        const val ACTION_START_DOWNLOAD = "com.aiwriter.free.START_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "com.aiwriter.free.CANCEL_DOWNLOAD"
        
        private const val MODEL_URL = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-ONNX/resolve/main/qwen2.5-1.5b-instruct-q4.onnx"
        private const val MODEL_FILENAME = "qwen2.5-1.5b-q4.onnx"
    }
    
    inner class LocalBinder : Binder() {
        fun getService(): ModelDownloadService = this@ModelDownloadService
    }
    
    interface DownloadCallback {
        fun onProgress(downloaded: Long, total: Long, percent: Int)
        fun onComplete(success: Boolean)
        fun onError(error: String)
    }
    
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        Log.d(TAG, "Service created")
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called with action: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                Log.d(TAG, "ACTION_START_DOWNLOAD received, isDownloading=$isDownloading")
                if (!isDownloading) {
                    startDownload()
                } else {
                    Log.w(TAG, "Download already in progress")
                }
            }
            ACTION_CANCEL_DOWNLOAD -> {
                Log.d(TAG, "ACTION_CANCEL_DOWNLOAD received")
                cancelDownload()
            }
            null -> {
                Log.w(TAG, "onStartCommand called with null intent")
            }
            else -> {
                Log.w(TAG, "Unknown action: ${intent.action}")
            }
        }
        return START_STICKY
    }
    
    fun setCallback(callback: DownloadCallback?) {
        this.callback = callback
    }
    
    fun startDownload() {
        Log.d(TAG, "startDownload() called")
        
        if (isDownloading) {
            Log.d(TAG, "Download already in progress")
            return
        }
        
        Log.d(TAG, "Starting download process...")
        isDownloading = true
        val notification = createNotification("Starting download...", 0, false)
        
        try {
            startForeground(NOTIFICATION_ID, notification)
            Log.d(TAG, "Started foreground with notification")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground", e)
            isDownloading = false
            return
        }
        
        downloadJob = serviceScope.launch {
            try {
                Log.d(TAG, "Download coroutine started")
                val success = downloadModelFile()
                Log.d(TAG, "Download completed with success=$success")
                
                withContext(Dispatchers.Main) {
                    if (success) {
                        notificationManager.notify(
                            NOTIFICATION_ID,
                            createNotification("Download complete!", 100, true)
                        )
                        callback?.onComplete(true)
                    } else {
                        notificationManager.notify(
                            NOTIFICATION_ID,
                            createNotification("Download failed", 0, true)
                        )
                        callback?.onComplete(false)
                    }
                    isDownloading = false
                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download error", e)
                withContext(Dispatchers.Main) {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        createNotification("Error: ${e.message}", 0, true)
                    )
                    callback?.onError(e.message ?: "Unknown error")
                    isDownloading = false
                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf()
                }
            }
        }
    }
    
    fun cancelDownload() {
        downloadJob?.cancel()
        isDownloading = false
        callback?.onComplete(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    fun isDownloading(): Boolean = isDownloading
    
    private suspend fun downloadModelFile(): Boolean = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var outputStream: FileOutputStream? = null
        
        try {
            val outputFile = File(filesDir, MODEL_FILENAME)
            val tempFile = File(filesDir, "$MODEL_FILENAME.tmp")
            
            // Delete temp file if exists
            if (tempFile.exists()) {
                tempFile.delete()
            }
            
            val url = URL(MODEL_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.connect()
            
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Server returned HTTP ${connection.responseCode}")
                return@withContext false
            }
            
            val fileSize = connection.contentLengthLong
            Log.d(TAG, "File size: $fileSize bytes (${fileSize / 1024 / 1024} MB)")
            
            if (fileSize <= 0) {
                Log.e(TAG, "Invalid file size")
                return@withContext false
            }
            
            connection.inputStream.use { input ->
                outputStream = FileOutputStream(tempFile)
                outputStream?.use { output ->
                    val buffer = ByteArray(8192)
                    var downloaded = 0L
                    var count: Int
                    var lastNotificationTime = 0L
                    
                    while (input.read(buffer).also { count = it } != -1) {
                        if (!isDownloading) {
                            Log.d(TAG, "Download cancelled")
                            return@withContext false
                        }
                        
                        output.write(buffer, 0, count)
                        downloaded += count
                        
                        // Update progress every 500ms
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastNotificationTime > 500) {
                            val progress = (downloaded * 100 / fileSize).toInt()
                            
                            withContext(Dispatchers.Main) {
                                val notification = createNotification(
                                    "Downloading model... ${downloaded / 1024 / 1024} MB / ${fileSize / 1024 / 1024} MB",
                                    progress,
                                    false
                                )
                                notificationManager.notify(NOTIFICATION_ID, notification)
                                callback?.onProgress(downloaded, fileSize, progress)
                            }
                            
                            lastNotificationTime = currentTime
                        }
                    }
                }
            }
            
            // Move temp file to final location
            if (tempFile.exists() && tempFile.length() == fileSize) {
                if (outputFile.exists()) {
                    outputFile.delete()
                }
                tempFile.renameTo(outputFile)
                Log.d(TAG, "Download completed successfully")
                true
            } else {
                Log.e(TAG, "Downloaded file size mismatch")
                tempFile.delete()
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            false
        } finally {
            connection?.disconnect()
            outputStream?.close()
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification channel for AI model downloads"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(message: String, progress: Int, isComplete: Boolean): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AI Writer")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .setOngoing(!isComplete)
            .setAutoCancel(isComplete)
        
        if (!isComplete && progress > 0) {
            builder.setProgress(100, progress, false)
        } else if (!isComplete) {
            builder.setProgress(100, 0, true)
        }
        
        if (!isComplete) {
            val cancelIntent = Intent(this, ModelDownloadService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
            }
            val cancelPendingIntent = PendingIntent.getService(
                this,
                0,
                cancelIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Cancel",
                cancelPendingIntent
            )
        }
        
        return builder.build()
    }
    
    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }
}
