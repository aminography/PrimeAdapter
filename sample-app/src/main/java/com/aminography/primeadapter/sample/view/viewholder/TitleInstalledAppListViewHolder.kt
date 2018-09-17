package com.aminography.primeadapter.sample.view.viewholder

import com.aminography.primeadapter.PrimeViewHolder
import com.aminography.primeadapter.callback.PrimeDelegate
import com.aminography.primeadapter.sample.R
import com.aminography.primeadapter.sample.view.dataholder.TitleInstalledAppListDataHolder
import kotlinx.android.synthetic.main.list_item_title_installed_app_list.view.*

/**
 * Created by aminography on 9/3/2018.
 */
class TitleInstalledAppListViewHolder(
        delegate: PrimeDelegate
) : PrimeViewHolder<TitleInstalledAppListDataHolder>(delegate, R.layout.list_item_title_installed_app_list) {

    override fun bindDataToView(dataHolder: TitleInstalledAppListDataHolder) {
        with(itemView) {
            dataHolder.apply {
                appCountTextView.text = String.format("Installed apps on this device (%d)", appCount)
            }
        }
    }

}
