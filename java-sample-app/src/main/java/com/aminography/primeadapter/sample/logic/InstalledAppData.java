package com.aminography.primeadapter.sample.logic;

import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * Created by aminography on 9/3/2018.
 */
public class InstalledAppData {

    private String packageIdentifier;
    private String appTitle;
    private Drawable appIcon;
    private long appSize;
    private int minSdkVersion;
    private int targetSdkVersion;
    private long versionCode;
    private String versionName;
    private Intent intent;

    public InstalledAppData(
            String packageIdentifier,
            String appTitle,
            Drawable appIcon,
            long appSize,
            int minSdkVersion,
            int targetSdkVersion,
            long versionCode,
            String versionName,
            Intent intent
    ) {
        this.packageIdentifier = packageIdentifier;
        this.appTitle = appTitle;
        this.appIcon = appIcon;
        this.appSize = appSize;
        this.minSdkVersion = minSdkVersion;
        this.targetSdkVersion = targetSdkVersion;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.intent = intent;
    }

    public String getPackageIdentifier() {
        return packageIdentifier;
    }

    public String getAppTitle() {
        return appTitle;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public long getAppSize() {
        return appSize;
    }

    public int getMinSdkVersion() {
        return minSdkVersion;
    }

    public int getTargetSdkVersion() {
        return targetSdkVersion;
    }

    public long getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public Intent getIntent() {
        return intent;
    }

}
