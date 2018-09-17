package com.aminography.primeadapter.sample.view.dataholder;

import com.aminography.primeadapter.PrimeDataHolder;
import com.aminography.primeadapter.annotation.DataHolder;
import com.aminography.primeadapter.sample.logic.InstalledAppData;

/**
 * Created by aminography on 8/17/2018.
 */
@DataHolder
public class InstalledAppListDataHolder extends PrimeDataHolder {

    private InstalledAppData mInstalledAppData;

    public InstalledAppListDataHolder(InstalledAppData installedAppData){
        mInstalledAppData = installedAppData;
    }

    public InstalledAppData getInstalledAppData() {
        return mInstalledAppData;
    }

}
