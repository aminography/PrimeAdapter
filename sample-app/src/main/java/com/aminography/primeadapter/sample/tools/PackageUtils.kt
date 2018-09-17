package com.aminography.primeadapter.sample.tools

import android.content.Context
import android.content.Intent


/**
 * Created by aminography on 8/10/2018.
 */
class PackageUtils {

    companion object {

        fun openApplication(context: Context, packageIdentifier: String) {
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(packageIdentifier)
                intent?.apply {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(this)
                }
            } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }
    }

}
