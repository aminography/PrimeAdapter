package com.aminography.primeadapter.draghelper

/**
 * Created by aminography on 8/19/2018.
 */
internal interface ItemTouchHelperAdapter {

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

    fun onItemDismiss(position: Int)
}
