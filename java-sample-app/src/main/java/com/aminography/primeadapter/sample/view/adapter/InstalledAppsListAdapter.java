package com.aminography.primeadapter.sample.view.adapter;

import com.aminography.primeadapter.PrimeAdapter;
import com.aminography.primeadapter.PrimeViewHolder;
import com.aminography.primeadapter.sample.view.dataholder.InstalledAppListDataHolder;
import com.aminography.primeadapter.sample.view.dataholder.TitleInstalledAppListDataHolder;
import com.aminography.primeadapter.sample.view.viewholder.InstalledAppListViewHolder;
import com.aminography.primeadapter.sample.view.viewholder.TitleInstalledAppListViewHolder;

import org.jetbrains.annotations.Nullable;

/**
 * Created by aminography on 4/17/2018.
 */
public class InstalledAppsListAdapter extends PrimeAdapter {

    @Nullable
    @Override
    public PrimeViewHolder<?> makeViewHolder(@Nullable Class<?> dataHolderClass) {
        if (dataHolderClass == InstalledAppListDataHolder.class) {
            return new InstalledAppListViewHolder(this);
        } else if (dataHolderClass == TitleInstalledAppListDataHolder.class) {
            return new TitleInstalledAppListViewHolder(this);
        } else {
            return null;
        }
    }
    
}
