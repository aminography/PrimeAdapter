package com.aminography.primeadapter.sample.tools;

import android.content.Context;
import android.content.Intent;

/**
 * Created by aminography on 8/10/2018.
 */
public class PackageUtils {

    public static void openApplication(Context context, String packageIdentifier) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageIdentifier);
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

}
