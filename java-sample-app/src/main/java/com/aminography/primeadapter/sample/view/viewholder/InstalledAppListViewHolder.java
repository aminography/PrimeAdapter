package com.aminography.primeadapter.sample.view.viewholder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.aminography.primeadapter.PrimeViewHolder;
import com.aminography.primeadapter.callback.PrimeDelegate;
import com.aminography.primeadapter.sample.R;
import com.aminography.primeadapter.sample.logic.InstalledAppData;
import com.aminography.primeadapter.sample.tools.TextUtils;
import com.aminography.primeadapter.sample.tools.UIUtils;
import com.aminography.primeadapter.sample.view.dataholder.InstalledAppListDataHolder;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Created by aminography on 8/17/2018.
 */
public class InstalledAppListViewHolder extends PrimeViewHolder<InstalledAppListDataHolder> {

    private PrimeDelegate delegate;
    private TextView appNameTextView;
    private ImageView appIconImageView;
    private TextView appSizeTextView;
    private TextView bottomTextView;
    private ImageButton expansionImageButton;

    public InstalledAppListViewHolder(@NotNull PrimeDelegate delegate) {
        super(delegate, R.layout.list_item_installed_app_list);
        this.delegate = delegate;
        appNameTextView = itemView.findViewById(R.id.appNameTextView);
        appIconImageView = itemView.findViewById(R.id.appIconImageView);
        appSizeTextView = itemView.findViewById(R.id.appSizeTextView);
        bottomTextView = itemView.findViewById(R.id.bottomTextView);
        expansionImageButton = itemView.findViewById(R.id.expansionImageButton);
        setDragHandle(itemView);
    }

    @Override
    protected void bindDataToView(@NotNull InstalledAppListDataHolder dataHolder) {
        InstalledAppData installedAppData = dataHolder.getInstalledAppData();

        appNameTextView.setText(installedAppData.getAppTitle());
        UIUtils.loadImage(appIconImageView, installedAppData.getAppIcon());
        appSizeTextView.setText(TextUtils.formatFileSize(installedAppData.getAppSize()));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(Locale.getDefault(), "packageIdentifier: %s", installedAppData.getPackageIdentifier())).append("\n");
        stringBuilder.append(String.format(Locale.getDefault(), "versionCode: %d", installedAppData.getVersionCode())).append("\n");
        stringBuilder.append(String.format(Locale.getDefault(), "versionName: %s", installedAppData.getVersionName())).append("\n");
        if (installedAppData.getMinSdkVersion() > 0) {
            stringBuilder.append(String.format(Locale.getDefault(), "minSdkVersion: %d", installedAppData.getMinSdkVersion())).append("\n");
        }
        stringBuilder.append(String.format(Locale.getDefault(), "targetSdkVersion: %d", installedAppData.getTargetSdkVersion())).append("\n");

        bottomTextView.setText(stringBuilder.toString());
        bottomTextView.setVisibility(dataHolder.getExpanded() ? View.VISIBLE : View.GONE);

        expansionImageButton.setImageResource(dataHolder.getExpanded() ? R.drawable.ic_keyboard_arrow_up_black_24dp : R.drawable.ic_keyboard_arrow_down_black_24dp);
        expansionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleExpansion();
            }
        });
        expansionImageButton.setVisibility(delegate.isExpandable() ? View.VISIBLE : View.GONE);
    }

}
