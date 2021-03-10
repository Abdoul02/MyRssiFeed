package com.abdoul.myrssifeed.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.abdoul.myrssifeed.R
import com.abdoul.myrssifeed.model.DeviceInfo
import com.abdoul.myrssifeed.other.AppUtility
import com.abdoul.myrssifeed.other.AppUtility.Companion.CHANNEL_ID
import com.abdoul.myrssifeed.other.AppUtility.Companion.EXTRA_KEY
import com.abdoul.myrssifeed.other.AppUtility.Companion.NOTIFICATION_ID
import com.abdoul.myrssifeed.repository.RssiRepository
import com.abdoul.myrssifeed.ui.MainActivity
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

@HiltWorker
class WifiLogWorker @AssistedInject constructor(
    @dagger.assisted.Assisted val appContext: Context,
    @dagger.assisted.Assisted workerParams: WorkerParameters,
    private val repository: RssiRepository,
    private val appUtility: AppUtility
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (appUtility.hasLocationPermission(appContext) && appUtility.isLocationEnabled()) {
            uploadAndNotify()
            return Result.success()
        }
        return Result.failure()
    }

    private suspend fun uploadAndNotify() {
        withContext(Dispatchers.IO) {
            repository.uploadWifiInfo()
                .collect {
                    if (it.getOrNull() != null) {
                        showNotification(appContext, it.getOrNull())
                    } else {
                        showNotification(appContext, null, true)
                    }
                }
        }
    }

    private fun showNotification(
        context: Context,
        deviceInfo: DeviceInfo?,
        error: Boolean = false
    ) {

        createNotificationChannel()
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.putExtra(EXTRA_KEY, deviceInfo)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(appContext, NOTIFICATION_ID, notificationIntent, 0);
        val builder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
        val notification = builder.setContentTitle("Wifi info")
            .setContentText(if (error) context.getString(R.string.notification_error) else "${deviceInfo?.wifiInformation!!.size} available wifi information stored")
            .setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .build()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                CHANNEL_ID,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mChannel.description = "Description for network notification "

            val notificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }
}