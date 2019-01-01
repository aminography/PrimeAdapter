package com.aminography.primeadapter.sample.view.adapter

import com.aminography.primeadapter.PrimeAdapter
import com.aminography.primeadapter.PrimeViewHolder
import com.aminography.primeadapter.sample.view.dataholder.InstalledAppListDataHolder
import com.aminography.primeadapter.sample.view.dataholder.TitleInstalledAppListDataHolder
import com.aminography.primeadapter.sample.view.viewholder.InstalledAppListViewHolder
import com.aminography.primeadapter.sample.view.viewholder.TitleInstalledAppListViewHolder

/**
 * Created by aminography on 4/17/2018.
 */
class InstalledAppsListAdapter : PrimeAdapter() {

    override fun makeViewHolder(dataHolderClass: Class<*>?): PrimeViewHolder<*>? {
        return when (dataHolderClass) {
            InstalledAppListDataHolder::class -> InstalledAppListViewHolder(this)
            TitleInstalledAppListDataHolder::class -> TitleInstalledAppListViewHolder(this)
            else -> null
        }
    }

}
