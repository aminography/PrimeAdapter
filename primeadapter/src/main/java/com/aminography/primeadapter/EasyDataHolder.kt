package com.aminography.primeadapter

import android.support.v7.widget.RecyclerView

/**
 * Created by aminography on 6/6/2018.
 */
abstract class PrimeDataHolder {
    var listPosition: Int = RecyclerView.NO_POSITION
    var expanded: Boolean = false
    var hasDivider: Boolean = true
}
