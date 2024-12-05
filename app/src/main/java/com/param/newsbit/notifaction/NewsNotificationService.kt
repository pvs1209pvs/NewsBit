package com.param.newsbit.notifaction

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.param.newsbit.R
import com.param.newsbit.activities.MainActivity

class NewsNotificationService(private val context: Context) {

    companion object {
        const val NEWS_DOWNLOAD_CHANNEL = "news_download_channel"
    }

    private val TAG = javaClass.simpleName

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(newCount: String) {

        Log.i(TAG, "showNotification")

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NEWS_DOWNLOAD_CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("$newCount latest news")
            .setContentText("Stay updated!")
            .setChannelId(NEWS_DOWNLOAD_CHANNEL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1, notification)

    }


}