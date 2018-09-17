package com.aminography.primeadapter.callback

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.aminography.primeadapter.PrimeDataHolder

/**
 * Created by aminography on 8/19/2018.
 */
interface PrimeDelegate {

    fun isDraggable(): Boolean

    fun isExpandable(): Boolean

    fun toggleExpansion(dataHolder: PrimeDataHolder): Boolean

    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)

    fun ontDragReleased(viewHolder: RecyclerView.ViewHolder)

    fun getInflater(): LayoutInflater

    fun getViewPool(): RecyclerView.RecycledViewPool?

    fun getParentView(): ViewGroup?

    fun getOnItemClickListener(): OnRecyclerViewItemClickListener?

}
