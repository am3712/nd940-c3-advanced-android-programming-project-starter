package com.udacity

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var downloadManager: DownloadManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private lateinit var selectedFileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        createNotificationChannel()
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            when (downloadOptionsRadioGroup.checkedRadioButtonId) {
                View.NO_ID -> Toast.makeText(this, R.string.error_option, Toast.LENGTH_SHORT).show()
                R.id.radio_button_glide -> {
                    selectedFileName = getString(R.string.glide_option)
                    download(GLIDE_URL)
                }
                R.id.radio_button_loadApp -> {
                    selectedFileName = getString(R.string.load_app_option)
                    download(APP_URL)
                }
                R.id.radio_button_retrofit -> {
                    selectedFileName = getString(R.string.retrofit_option)
                    download(RETROFIT_URL)
                }
            }
        }
    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
            Log.i(TAG, "onReceive: downloadId-> $downloadId")
            // https://code.luasoftware.com/tutorials/android/android-download-file-with-downloadmanager-and-check-status/
            if (downloadId == -1L)
                return
            // query download status
            val cursor: Cursor =
                downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
            if (cursor.moveToFirst()) {
                val status: Int =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                // if  status == DownloadManager.STATUS_SUCCESSFUL -> download is successful
                pushStatusNotification(status)
            } else {
                // download is cancelled
                pushStatusNotification(DownloadManager.STATUS_FAILED)
            }
        }
    }


    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initPendingIntentWithStatus(status: Int) {
        // Create an Intent for the activity you want to start
        val detailsIntent = Intent(this, DetailActivity::class.java).apply {
            putExtra(DOWNLOAD_STATUS_EXTRA, status)
            putExtra(DOWNLOAD_FILE_NAME_EXTRA, selectedFileName)
        }
        // Create the TaskStackBuilder
        pendingIntent = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(detailsIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }

    private fun initNotificationAction() {
        action = NotificationCompat.Action(
            R.drawable.ic_assistant_black_24dp,
            getString(R.string.check_the_status),
            pendingIntent
        )
    }

    private fun pushStatusNotification(status: Int) {
        // cancel all previous notifications
        notificationManager.cancelAll()

        // pass status extra
        initPendingIntentWithStatus(status)
        initNotificationAction()

        // build notification
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.content_title))
            .setContentText(getString(R.string.content_text))
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .addAction(action)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    companion object {
        private const val GLIDE_URL = "https://github.com/bumptech/glide"
        private const val APP_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val RETROFIT_URL = "https://github.com/square/retrofit"
        private const val CHANNEL_ID = "loadAppChannelId"
        private const val NOTIFICATION_ID = 1234
        const val DOWNLOAD_STATUS_EXTRA = "downloadStatus"
        const val DOWNLOAD_FILE_NAME_EXTRA = "downloadFileName"
    }

}
