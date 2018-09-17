package com.aminography.primeadapter.sample.view.viewholder;

import android.widget.TextView;

import com.aminography.primeadapter.PrimeViewHolder;
import com.aminography.primeadapter.callback.IPrimeAdapterDelegate;
import com.aminography.primeadapter.sample.R;
import com.aminography.primeadapter.sample.view.dataholder.TitleInstalledAppListDataHolder;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Created by aminography on 8/17/2018.
 */
public class TitleInstalledAppListViewHolder extends PrimeViewHolder<TitleInstalledAppListDataHolder> {

    private TextView appCountTextView;

    public TitleInstalledAppListViewHolder(@NotNull IPrimeAdapterDelegate delegate) {
        super(delegate, R.layout.list_item_title_installed_app_list);
        appCountTextView = itemView.findViewById(R.id.appCountTextView);
    }

    @Override
    protected void bindDataToView(@NotNull TitleInstalledAppListDataHolder dataHolder) {
        appCountTextView.setText(String.format(Locale.getDefault(), "Installed apps on this device (%d)", dataHolder.getAppCount()));
    }

}
