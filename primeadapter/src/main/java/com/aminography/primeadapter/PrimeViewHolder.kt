package com.aminography.primeadapter

import android.graphics.Color
import android.support.annotation.LayoutRes
import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import com.aminography.primeadapter.callback.PrimeDelegate
import com.aminography.primeadapter.draghelper.ItemTouchHelperViewHolder
import com.aminography.primeadapter.tools.consume


/**
 * Created by aminography on 6/6/2018.
 */
abstract class PrimeViewHolder<T : PrimeDataHolder>(
        private val delegate: PrimeDelegate,
        @LayoutRes layoutResourceID: Int
) : RecyclerView.ViewHolder(delegate.getInflater().inflate(layoutResourceID, delegate.getParentView(), false)), ItemTouchHelperViewHolder {

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
        delegate.getOnItemClickListener()?.let { listener ->
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

    protected fun setDragHandle(view: View) {
        view.setOnTouchListener { _, event ->
            if (delegate.isDraggable()) {
                @Suppress("DEPRECATION")
                when (MotionEventCompat.getActionMasked(event)) {
                    MotionEvent.ACTION_DOWN -> delegate.onStartDrag(this)
                    MotionEvent.ACTION_UP -> {
                        delegate.ontDragReleased(this)
                    }
                }
            }
            false
        }
    }

    protected fun toggleExpansion() {
        dataHolder?.apply {
            delegate.toggleExpansion(this)
        }
    }

    protected fun isDraggable(): Boolean = delegate.isDraggable()

    protected fun isExpandable(): Boolean = delegate.isExpandable()

    override fun onItemDragged() {
        itemView.setBackgroundColor(Color.parseColor("#DFDFDF"))
    }

    override fun onItemReleased() {
        itemView.setBackgroundColor(0)
    }

}
