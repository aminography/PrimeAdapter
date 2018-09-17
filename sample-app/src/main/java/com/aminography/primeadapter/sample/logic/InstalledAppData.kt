package com.aminography.primeadapter.sample.logic

import android.content.Intent
import android.graphics.drawable.Drawable

/**
 * Created by aminography on 9/3/2018.
 */
data class InstalledAppData(
        val packageIdentifier: String,
        val appTitle: String,
        val appIcon: Drawable?,
        val appSize: Long,
        val minSdkVersion: Int,
        val targetSdkVersion: Int,
        val versionCode: Long,
        val versionName: String,
        val intent: Intent?
)