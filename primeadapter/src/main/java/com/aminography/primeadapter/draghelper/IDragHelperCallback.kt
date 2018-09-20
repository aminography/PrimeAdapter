package com.aminography.primeadapter.draghelper

/**
 * Created by aminography on 8/19/2018.
 */
internal interface IDragHelperCallback {

    fun onItemMoved(fromPosition: Int, toPosition: Int): Boolean

    fun onItemSwiped(position: Int, direction: Int)

    fun isSwipeableToDismiss(): Boolean

    fun isLongPressDraggable(): Boolean

    fun isOnlySameViewTypeReplaceable(): Boolean
}
