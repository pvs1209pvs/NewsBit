package com.param.newsbit.notifaction

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.param.newsbit.R

class NewsNotificationService(context: Context) {

    companion object {
        const val NEWS_DOWNLOAD_CHANNEL = "news_download_channel"
    }

    private val TAG = javaClass.simpleName

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val notification = NotificationCompat.Builder(context, NEWS_DOWNLOAD_CHANNEL)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Latest news downloaded")
        .setContentText("Time to read the latest news to stay updated")
        .setChannelId(NEWS_DOWNLOAD_CHANNEL)
        .build()

    fun showNotification() {
        Log.i(TAG, "showNotification")
        notificationManager.notify(1, notification)
    }

}