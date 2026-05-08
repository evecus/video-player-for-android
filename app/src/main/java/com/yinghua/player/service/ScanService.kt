package com.yinghua.player.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.yinghua.player.R
import com.yinghua.player.data.repository.VideoRepository
import com.yinghua.player.utils.MediaScanner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class ScanService : Service() {

    @Inject lateinit var scanner: MediaScanner
    @Inject lateinit var videoRepository: VideoRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID = "scan_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_SCAN_COMPLETE = "com.yinghua.player.SCAN_COMPLETE"
        const val EXTRA_VIDEO_COUNT = "video_count"
        const val EXTRA_FOLDER_COUNT = "folder_count"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("正在扫描视频文件…"))
        serviceScope.launch {
            try {
                val result = scanner.scan()
                videoRepository.replaceAll(result.videos, result.folders)

                // Broadcast completion
                val broadcastIntent = Intent(ACTION_SCAN_COMPLETE).apply {
                    putExtra(EXTRA_VIDEO_COUNT, result.videos.size)
                    putExtra(EXTRA_FOLDER_COUNT, result.folders.size)
                    setPackage(packageName)
                }
                sendBroadcast(broadcastIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.channel_scan_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_scan_desc)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()
}
