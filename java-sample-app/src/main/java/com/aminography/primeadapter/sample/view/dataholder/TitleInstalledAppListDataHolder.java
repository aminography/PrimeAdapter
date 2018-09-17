package com.aminography.primeadapter.sample.view.dataholder;

import com.aminography.primeadapter.PrimeDataHolder;
import com.aminography.primeadapter.annotation.DataHolder;
import com.aminography.primeadapter.sample.logic.InstalledAppData;

/**
 * Created by aminography on 8/17/2018.
 */
@DataHolder
public class TitleInstalledAppListDataHolder extends PrimeDataHolder {

    private int mAppCount;

    public TitleInstalledAppListDataHolder(int appCount){
        mAppCount = appCount;
    }

    public int getAppCount() {
        return mAppCount;
    }

}
