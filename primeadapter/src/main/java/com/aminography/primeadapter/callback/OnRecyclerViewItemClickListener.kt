package com.aminography.primeadapter.callback

import com.aminography.primeadapter.PrimeDataHolder

/**
 * Created by aminography on 6/6/2018.
 */
interface OnRecyclerViewItemClickListener {

    fun onItemClick(primeDataHolder: PrimeDataHolder)

    fun onItemLongClick(primeDataHolder: PrimeDataHolder)
}
