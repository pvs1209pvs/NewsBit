package com.param.newsbit.notifaction

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.param.newsbit.R
import com.param.newsbit.activities.MainActivity

class NewsNotificationService(context: Context) {

    companion object {
        const val NEWS_DOWNLOAD_CHANNEL = "news_download_channel"
    }

    private val TAG = javaClass.simpleName

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val pendingIntent = PendingIntent.getActivity(
        context,
        1,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE,
    )

    private val notification = NotificationCompat.Builder(context, NEWS_DOWNLOAD_CHANNEL)
        .setChannelId(NEWS_DOWNLOAD_CHANNEL)
        .setSmallIcon(R.mipmap.ic_launcher_foreground)
        .setAutoCancel(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setContentTitle("Latest news downloaded")
        .setContentText("Stay updated")
        .setContentIntent(pendingIntent)
        .build()

    fun show() {
        notificationManager.notify(1, notification)
    }


}