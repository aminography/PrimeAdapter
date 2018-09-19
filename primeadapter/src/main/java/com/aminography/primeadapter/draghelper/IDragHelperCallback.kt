package com.aminography.primeadapter.draghelper

/**
 * Created by aminography on 8/19/2018.
 */
internal interface IDragHelperCallback {

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

    fun onItemSwiped(position: Int, direction: Int)

    fun isItemViewSwipeEnabled(): Boolean

    fun isLongPressDragEnabled(): Boolean

    fun isOnlySameViewTypeCanReplaceable(): Boolean
}
