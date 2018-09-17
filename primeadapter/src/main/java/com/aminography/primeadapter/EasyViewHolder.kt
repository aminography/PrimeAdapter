package com.aminography.primeadapter

import android.graphics.Color
import android.support.annotation.LayoutRes
import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import com.aminography.primeadapter.callback.PrimeDelegate
import com.aminography.primeadapter.draghelper.ItemTouchHelperViewHolder
import com.aminography.primeadapter.tools.consume


/**
 * Created by aminography on 6/6/2018.
 */
abstract class PrimeViewHolder<T : PrimeDataHolder>(
        private val adapterDelegate: PrimeDelegate,
        @LayoutRes layoutResourceID: Int
) : RecyclerView.ViewHolder(adapterDelegate.getInflater().inflate(layoutResourceID, adapterDelegate.getParentView(), false)), ItemTouchHelperViewHolder {

    var dataHolder: T? = null
        set(holder) {
            if (holder is T) {
                field = holder
                holder.apply {
                    listPosition = adapterPosition
                    bindDataToView(holder)
                }
            } else {
                // It shouldn't happen!
            }
        }

    init {
        itemView.setOnTouchListener { _, event ->
            if (adapterDelegate.isDraggable()) {
                @Suppress("DEPRECATION")
                when (MotionEventCompat.getActionMasked(event)) {
                    MotionEvent.ACTION_DOWN -> adapterDelegate.onStartDrag(this)
                    MotionEvent.ACTION_UP -> {
                        adapterDelegate.ontDragReleased(this)
                    }
                }
            }
            false
        }

        adapterDelegate.getOnItemClickListener()?.let { listener ->
            itemView.apply {
                setOnClickListener { _ ->
                    dataHolder?.let {
                        listener.onItemClick(it)
                    }
                }
                setOnLongClickListener { _ ->
                    consume {
                        dataHolder?.let {
                            listener.onItemLongClick(it)
                        }
                    }
                }
            }
        }
    }

    protected abstract fun bindDataToView(dataHolder: T)

    protected fun toggleExpansion() {
        dataHolder?.apply {
            adapterDelegate.toggleExpansion(this)
        }
    }

    protected fun isDraggable(): Boolean = adapterDelegate.isDraggable()

    protected fun isExpandable(): Boolean = adapterDelegate.isExpandable()

    override fun onItemDragged() {
        itemView.setBackgroundColor(Color.LTGRAY)
    }

    override fun onItemReleased() {
        itemView.setBackgroundColor(0)
    }

}
