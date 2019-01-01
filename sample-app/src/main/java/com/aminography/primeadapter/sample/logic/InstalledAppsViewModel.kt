package com.aminography.primeadapter.sample.logic

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File


/**
 * Created by aminography on 8/17/2018.
 */
class InstalledAppsViewModel : ViewModel() {

    private var dataList: MutableLiveData<MutableList<InstalledAppData>>? = null

    fun getDataList(context: Context): LiveData<MutableList<InstalledAppData>>? {
        if (dataList == null) {
            dataList = MutableLiveData()
            loadDataList(context)
        }
        return dataList
    }

    private fun loadDataList(context: Context) {
        doAsync {
            val list: MutableList<InstalledAppData> = mutableListOf()
            val packageManager = context.packageManager
            val installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            for (applicationInfo in installedApplications!!) {
                if (packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null) { // apps with launcher intent
                    when {
                        applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 1 -> { // updated system apps
                        }
                        applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1 -> { // system apps
                        }
                        else -> { // user installed apps
                            applicationInfo?.apply {
                                val appTitle = packageManager.getApplicationLabel(this).toString()
                                var drawable: Drawable? = null
                                try {
                                    drawable = packageManager.getApplicationIcon(packageName)
                                } catch (ignored: Exception) {
                                }
                                val size = File(publicSourceDir).length()
                                val intent = packageManager.getLaunchIntentForPackage(packageName)

                                val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                                val minSdkVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) applicationInfo.minSdkVersion else -1
                                val targetSdkVersion = applicationInfo.targetSdkVersion
                                @Suppress("DEPRECATION")
                                val versionCode = packageInfo.versionCode.toLong() /*longVersionCode*/
                                val versionName = packageInfo.versionName ?: ""

                                list.add(InstalledAppData(packageName, appTitle, drawable, size, minSdkVersion, targetSdkVersion, versionCode, versionName, intent))
                            }
                        }
                    }
                }
            }

            val sortedList = list.sortedWith(compareBy { it.appTitle }).toMutableList()
            uiThread {
                dataList?.value = sortedList
            }
        }
    }

}