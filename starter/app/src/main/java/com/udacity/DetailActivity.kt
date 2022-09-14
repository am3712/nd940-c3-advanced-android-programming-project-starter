package com.udacity

import android.app.DownloadManager
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        initFields()
    }

    private fun initFields() {
        file_name.text =
            intent.extras?.getString(MainActivity.DOWNLOAD_FILE_NAME_EXTRA, "").orEmpty()
        val status = intent.extras?.getInt(MainActivity.DOWNLOAD_STATUS_EXTRA, -1) ?: -1
        // 8 -> represent Success / DownloadManager.STATUS_SUCCESSFUL
        val isDownloadSuccess = status == DownloadManager.STATUS_SUCCESSFUL
        download_status.text = getString(if (isDownloadSuccess) R.string.success else R.string.fail)
        if (!isDownloadSuccess)
            download_status.setTextColor(Color.RED)
    }

}
