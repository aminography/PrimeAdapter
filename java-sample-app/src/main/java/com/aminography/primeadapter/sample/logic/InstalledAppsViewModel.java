package com.aminography.primeadapter.sample.logic;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by aminography on 8/17/2018.
 */
public class InstalledAppsViewModel extends ViewModel {

    private MutableLiveData<List<InstalledAppData>> dataList = null;

    public LiveData<List<InstalledAppData>> getDataList(Activity activity) {
        if (dataList == null) {
            dataList = new MutableLiveData<>();
            loadDataList(activity);
        }
        return dataList;
    }

    private void loadDataList(final Activity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<InstalledAppData> list = new ArrayList<>();
                PackageManager packageManager = activity.getPackageManager();
                List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
                for (ApplicationInfo applicationInfo : installedApplications) {
                    if (packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null) { // apps with launcher intent
                        if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1) {
                            // updated system apps
                        } else if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                            // system apps
                        } else { // user installed apps
                            String appTitle = packageManager.getApplicationLabel(applicationInfo).toString();
                            Drawable drawable = null;
                            try {
                                drawable = packageManager.getApplicationIcon(applicationInfo.packageName);
                            } catch (Exception ignored) {
                            }
                            long size = new File(applicationInfo.publicSourceDir).length();
                            Intent intent = packageManager.getLaunchIntentForPackage(applicationInfo.packageName);

                            PackageInfo packageInfo = null;
                            try {
                                packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                            int minSdkVersion = -1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                minSdkVersion = applicationInfo.minSdkVersion;
                            }
                            int targetSdkVersion = applicationInfo.targetSdkVersion;
                            long versionCode = packageInfo != null ? packageInfo.versionCode : 0; /*longVersionCode*/
                            String versionName = packageInfo != null ? packageInfo.versionName : "";

                            list.add(new InstalledAppData(applicationInfo.packageName, appTitle, drawable, size, minSdkVersion, targetSdkVersion, versionCode, versionName, intent));
                        }
                    }
                }

                Collections.sort(list, new Comparator<InstalledAppData>() {
                    @Override
                    public int compare(InstalledAppData a, InstalledAppData b) {
                        return a.getAppTitle().compareTo(b.getAppTitle());
                    }
                });

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dataList.setValue(list);
                    }
                });
            }
        }).start();
    }
}
