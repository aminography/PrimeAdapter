package com.aminography.primeadapter.draghelper

/**
 * Created by aminography on 8/19/2018.
 */
interface OnRecyclerViewItemDragListener {

    fun onItemMoved(fromPosition: Int, toPosition: Int)

    fun onItemSwiped(position: Int, direction: Int)
}
