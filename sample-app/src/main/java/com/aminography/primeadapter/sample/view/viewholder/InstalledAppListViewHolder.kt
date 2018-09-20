package com.aminography.primeadapter.sample.view.viewholder

import android.view.View
import com.aminography.primeadapter.PrimeViewHolder
import com.aminography.primeadapter.callback.PrimeDelegate
import com.aminography.primeadapter.sample.R
import com.aminography.primeadapter.sample.tools.TextUtils
import com.aminography.primeadapter.sample.tools.loadImage
import com.aminography.primeadapter.sample.view.dataholder.InstalledAppListDataHolder
import kotlinx.android.synthetic.main.list_item_installed_app_list.view.*

/**
 * Created by aminography on 8/17/2018.
 */
class InstalledAppListViewHolder(
        delegate: PrimeDelegate
) : PrimeViewHolder<InstalledAppListDataHolder>(delegate, R.layout.list_item_installed_app_list) {

    init {
        setDragHandle(itemView)
    }

    override fun bindDataToView(dataHolder: InstalledAppListDataHolder) {
        with(itemView) {
            dataHolder.apply {
                appNameTextView.text = installedAppData.appTitle
                appIconImageView.loadImage(installedAppData.appIcon)
                appSizeTextView.text = TextUtils.formatFileSize(installedAppData.appSize)

                val stringBuilder = StringBuilder()
                stringBuilder.apply {
                    append("packageIdentifier: ${installedAppData.packageIdentifier}").append("\n")
                    append("versionCode: ${installedAppData.versionCode}").append("\n")
                    append("versionName: ${installedAppData.versionName}").append("\n")
                    if (installedAppData.minSdkVersion > 0) append("minSdkVersion: ${installedAppData.minSdkVersion}").append("\n")
                    append("targetSdkVersion: ${installedAppData.targetSdkVersion}")
                }

                bottomTextView.text = stringBuilder.toString()
                bottomTextView.visibility = if (expanded) View.VISIBLE else View.GONE

                expansionImageButton.setImageResource(if (expanded) R.drawable.ic_keyboard_arrow_up_black_24dp else R.drawable.ic_keyboard_arrow_down_black_24dp)
                expansionImageButton.setOnClickListener { toggleExpansion() }
                expansionImageButton.visibility = if (isExpandable()) View.VISIBLE else View.GONE
            }
        }
    }

}
