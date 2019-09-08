package com.aminography.primeadapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.support.annotation.ColorInt
import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SnapHelper
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.ViewGroup
import com.aminography.primeadapter.callback.OnRecyclerViewItemClickListener
import com.aminography.primeadapter.callback.OnRecyclerViewItemExpansionListener
import com.aminography.primeadapter.callback.PrimeDelegate
import com.aminography.primeadapter.divider.SkipDividerItemDecorator
import com.aminography.primeadapter.draghelper.DragHelper
import com.aminography.primeadapter.draghelper.IDragHelperCallback
import com.aminography.primeadapter.draghelper.OnRecyclerViewItemDragListener
import com.aminography.primeadapter.exception.ViewHolderNotFoundException
import com.aminography.primeadapter.tools.PrimeAdapterUtils


/**
 * Created by aminography on 6/6/2018.
 *
 * NOTE: It is an experimental class!!!
 */
abstract class PrimeAdapterAsyncDiffer : RecyclerView.Adapter<PrimeViewHolder<PrimeDataHolder>>(), PrimeDelegate {

    protected lateinit var context: Context
    protected lateinit var layoutInflater: LayoutInflater
    //    protected var dataList = mutableListOf<PrimeDataHolder>()
    private var itemClickListener: OnRecyclerViewItemClickListener? = null
    private var itemDragListener: OnRecyclerViewItemDragListener? = null
    private var itemExpansionListener: OnRecyclerViewItemExpansionListener? = null
    private var itemTouchHelper: ItemTouchHelper? = null
    private var dragHelper: DragHelper? = null
    private var recyclerView: RecyclerView? = null
    protected var recycledViewPool: RecyclerView.RecycledViewPool? = null
    private var isDraggable: Boolean = false
    private var isExpandable: Boolean = false
    private var isSwipeableToDismiss: Boolean = false
    private var isLongPressDraggable: Boolean = false
    private var isOnlySameViewTypeReplaceable: Boolean = true
    @Suppress("LeakingThis")
    private var differ = AsyncListDiffer(this, defaultDiffUtilCallback)

    override fun onBindViewHolder(viewHolder: PrimeViewHolder<PrimeDataHolder>, position: Int) {
        if (position < differ.currentList.size) {
            differ.currentList[position].apply {
                listPosition = position
                expanded = expanded && isExpandable
                viewHolder.dataHolder = this
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrimeViewHolder<PrimeDataHolder> {
        val dataHolderClass = PrimeAdapterUtils.reverseViewTypeMap[viewType]
        val viewHolder = makeViewHolder(dataHolderClass)
        if (viewHolder == null) {
            throw ViewHolderNotFoundException(dataHolderClass, this::class.java)
        } else {
            @Suppress("UNCHECKED_CAST")
            return viewHolder as PrimeViewHolder<PrimeDataHolder>
        }
    }

    abstract fun makeViewHolder(dataHolderClass: Class<*>?): PrimeViewHolder<*>?

    override fun getInflater(): LayoutInflater = layoutInflater

    override fun getParentView(): ViewGroup? = recyclerView

    override fun getOnItemClickListener(): OnRecyclerViewItemClickListener? = itemClickListener

    override fun getViewPool(): RecyclerView.RecycledViewPool? = recycledViewPool

    override fun isDraggable(): Boolean {
        return isDraggable
    }

    fun setDraggable(isDraggable: Boolean) {
        this.isDraggable = isDraggable
        if (isDraggable) initItemTouchHelper()
    }

    fun setSwipeableToDismiss(isSwipeableToDismiss: Boolean) {
        this.isSwipeableToDismiss = isSwipeableToDismiss
        if (isSwipeableToDismiss) initItemTouchHelper()
    }

    fun setLongPressDraggable(isLongPressDraggable: Boolean) {
        this.isLongPressDraggable = isLongPressDraggable
        if (isLongPressDraggable) initItemTouchHelper()
    }

    /**
     * @param isOnlySameViewTypeReplaceable
     */
    fun setOnlySameViewTypeReplaceable(isOnlySameViewTypeReplaceable: Boolean) {
        this.isOnlySameViewTypeReplaceable = isOnlySameViewTypeReplaceable
    }

    private fun setDiffUtilCallback(diffUtilCallback: PrimeDiffUtilCallback) {
        differ = AsyncListDiffer(this, diffUtilCallback)
    }

    private fun initItemTouchHelper() {
        if (itemTouchHelper == null) {
            val itemTouchHelperCallback = object : IDragHelperCallback {

                override fun isOnlySameViewTypeReplaceable(): Boolean = isOnlySameViewTypeReplaceable

                override fun isLongPressDraggable(): Boolean = isLongPressDraggable

                override fun isSwipeableToDismiss(): Boolean = isSwipeableToDismiss

                override fun swipeToDismissFlags(): Int = ItemTouchHelper.START or ItemTouchHelper.END

                override fun onItemMoved(fromPosition: Int, toPosition: Int): Boolean {
                    val dataList = ArrayList(differ.currentList)
                    dataList.add(toPosition, dataList.removeAt(fromPosition))

                    if (fromPosition < toPosition) for (i in fromPosition..toPosition) dataList[i].listPosition = i
                    else for (i in toPosition..fromPosition) dataList[i].listPosition = i

                    differ.submitList(dataList)
//                    notifyItemMoved(fromPosition, toPosition)
                    itemDragListener?.onItemMoved(fromPosition, toPosition)
                    return true
                }

                override fun onItemSwiped(position: Int, direction: Int) {
                    removeItem(position, true)
                    itemDragListener?.onItemSwiped(position, direction)
                }
            }

            dragHelper = DragHelper(itemTouchHelperCallback)
            itemTouchHelper = ItemTouchHelper(dragHelper!!)
            itemTouchHelper?.attachToRecyclerView(recyclerView)
        }
    }

    /**
     * @param dataHolder
     * @return isExpandable
     */
    override fun toggleExpansion(dataHolder: PrimeDataHolder): Boolean {
        if (isExpandable) {
            dataHolder.expanded = !dataHolder.expanded
            notifyItemChanged(dataHolder.listPosition)
            itemExpansionListener?.onItemExpansion(dataHolder)
        }
        return isExpandable
    }

    override fun isExpandable(): Boolean {
        return isExpandable
    }

    fun setExpandable(isExpandable: Boolean) {
        this.isExpandable = isExpandable
//        (recyclerView?.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        (recyclerView?.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = true
        notifyDataSetChanged()
    }

    fun setDivider(dividerDrawable: Drawable?, insetLeft: Int = 0, insetTop: Int = 0, insetRight: Int = 0, insetBottom: Int = 0) {
        val itemDecorationCount = recyclerView?.itemDecorationCount ?: 0
        if (itemDecorationCount > 0) {
            recyclerView?.removeItemDecorationAt(0)
//            for (i in 0..(itemDecorationCount - 1)) recyclerView?.removeItemDecorationAt(i)
        }
        if (recyclerView?.layoutManager is LinearLayoutManager) {
            dividerDrawable?.let {
                val insetDrawable = InsetDrawable(it, insetLeft, insetTop, insetRight, insetBottom)
                val dividerItemDecoration = SkipDividerItemDecorator(insetDrawable)
                recyclerView?.addItemDecoration(dividerItemDecoration)
            }
        }
    }

    fun setDivider(@ColorInt color: Int = Color.parseColor("#BDBDBD"), thickness: Int = 1, insetLeft: Int = 0, insetTop: Int = 0, insetRight: Int = 0, insetBottom: Int = 0) {
        val dividerDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setSize(thickness, thickness)
            setColor(color)
        }
        setDivider(dividerDrawable, insetLeft, insetTop, insetRight, insetBottom)
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
    }

    override fun ontDragReleased(viewHolder: RecyclerView.ViewHolder) {
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun getItemViewType(position: Int): Int = PrimeAdapterUtils.viewTypeMap[differ.currentList[position]::class.java]!!

    fun getItem(position: Int): PrimeDataHolder = differ.currentList[position]

    fun addItem(dataHolder: PrimeDataHolder, position: Int = 0, animate: Boolean = true) {
        val dataList = ArrayList(differ.currentList)
        dataList.add(position, dataHolder)
        for (i in position..(dataList.size - 1)) dataList[i].listPosition = i

        differ.submitList(dataList)

//        if (animate) {
//            notifyItemInserted(position)
//        } else {
//            notifyDataSetChanged()
//        }
    }

    fun addItemToLast(dataHolder: PrimeDataHolder, animate: Boolean = true) = addItem(dataHolder, itemCount, animate)

    fun removeItem(position: Int, animate: Boolean) {
        val dataList = ArrayList(differ.currentList)
        dataList.removeAt(position)
        for (i in position..(dataList.size - 1)) dataList[i].listPosition = i

        differ.submitList(dataList)

//        if (animate) {
//            notifyItemRemoved(position)
//        } else {
//            notifyDataSetChanged()
//        }
    }

    fun removeItem(dataHolder: PrimeDataHolder, animate: Boolean) {
        val position = dataHolder.listPosition
        if (position != RecyclerView.NO_POSITION) {
            removeItem(position, animate)
        }
    }

    open fun replaceDataList(modelList: List<PrimeDataHolder>) {
        differ.submitList(modelList)
    }

    class AdapterBuilder(private val recyclerView: RecyclerView) {

        private var layoutManager: RecyclerView.LayoutManager? = null
        private var snapHelper: SnapHelper? = null
        private var recycledViewPool: RecyclerView.RecycledViewPool? = null
        private var itemClickListener: OnRecyclerViewItemClickListener? = null
        private var itemDragListener: OnRecyclerViewItemDragListener? = null
        private var itemExpansionListener: OnRecyclerViewItemExpansionListener? = null
        private var dividerDrawable: Drawable? = null
        private var dividerDrawableInsetLeft: Int = 0
        private var dividerDrawableInsetTop: Int = 0
        private var dividerDrawableInsetRight: Int = 0
        private var dividerDrawableInsetBottom: Int = 0
        private var hasFixedSize: Boolean? = null
        private var isNestedScrollingEnabled: Boolean? = null
        private var isDraggable: Boolean? = null
        private var isExpandable: Boolean? = null
        private var isSwipeableToDismiss: Boolean? = null
        private var isLongPressDraggable: Boolean? = null
        private var isOnlySameViewTypeReplaceable: Boolean? = null
        private var diffUtilCallback: PrimeDiffUtilCallback? = null
        private var set: Boolean = false

        fun set(): AdapterBuilder {
            set = true
            return this
        }

        fun setDiffUtilCallback(diffUtilCallback: PrimeDiffUtilCallback): AdapterBuilder {
            this.diffUtilCallback = diffUtilCallback
            return this
        }

        fun setHasFixedSize(hasFixedSize: Boolean): AdapterBuilder {
            this.hasFixedSize = hasFixedSize
            return this
        }

        fun setDraggable(isDraggable: Boolean): AdapterBuilder {
            this.isDraggable = isDraggable
            return this
        }

        fun setExpandable(isExpandable: Boolean): AdapterBuilder {
            this.isExpandable = isExpandable
            return this
        }

        fun setSwipeableToDismiss(isSwipeableToDismiss: Boolean): AdapterBuilder {
            this.isSwipeableToDismiss = isSwipeableToDismiss
            return this
        }

        fun setLongPressDraggable(isLongPressDraggable: Boolean): AdapterBuilder {
            this.isLongPressDraggable = isLongPressDraggable
            return this
        }

        fun setOnlySameViewTypeReplaceable(isOnlySameViewTypeReplaceable: Boolean): AdapterBuilder {
            this.isOnlySameViewTypeReplaceable = isOnlySameViewTypeReplaceable
            return this
        }

        fun setNestedScrollingEnabled(isNestedScrollingEnabled: Boolean): AdapterBuilder {
            this.isNestedScrollingEnabled = isNestedScrollingEnabled
            return this
        }

        fun setViewPool(recycledViewPool: RecyclerView.RecycledViewPool?): AdapterBuilder {
            this.recycledViewPool = recycledViewPool
            return this
        }

        fun setLayoutManager(layoutManager: RecyclerView.LayoutManager): AdapterBuilder {
            this.layoutManager = layoutManager
            return this
        }

        fun setSnapHelper(snapHelper: SnapHelper): AdapterBuilder {
            this.snapHelper = snapHelper
            return this
        }

        fun setItemClickListener(itemClickListener: OnRecyclerViewItemClickListener): AdapterBuilder {
            this.itemClickListener = itemClickListener
            return this
        }

        fun setItemDragListener(itemDragListener: OnRecyclerViewItemDragListener): AdapterBuilder {
            this.itemDragListener = itemDragListener
            return this
        }

        fun setItemExpandListener(itemExpansionListener: OnRecyclerViewItemExpansionListener): AdapterBuilder {
            this.itemExpansionListener = itemExpansionListener
            return this
        }

        fun setDivider(dividerDrawable: Drawable?, insetLeft: Int = 0, insetTop: Int = 0, insetRight: Int = 0, insetBottom: Int = 0): AdapterBuilder {
            this.dividerDrawable = dividerDrawable
            dividerDrawableInsetLeft = insetLeft
            dividerDrawableInsetTop = insetTop
            dividerDrawableInsetRight = insetRight
            dividerDrawableInsetBottom = insetBottom
            return this
        }

        fun setDivider(@ColorInt color: Int = Color.parseColor("#BDBDBD"), thickness: Int = 1, insetLeft: Int = 0, insetTop: Int = 0, insetRight: Int = 0, insetBottom: Int = 0): AdapterBuilder {
            this.dividerDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setSize(thickness, thickness)
                setColor(color)
            }
            dividerDrawableInsetLeft = insetLeft
            dividerDrawableInsetTop = insetTop
            dividerDrawableInsetRight = insetRight
            dividerDrawableInsetBottom = insetBottom
            return this
        }

        fun <T : PrimeAdapterAsyncDiffer> build(adapterClass: Class<T>): T {
            val t = adapterClass.newInstance()
            t.recyclerView = recyclerView
            t.context = recyclerView.context
            t.layoutInflater = LayoutInflater.from(recyclerView.context)
            t.recycledViewPool = recycledViewPool
            t.itemClickListener = itemClickListener
            t.itemDragListener = itemDragListener
            t.itemExpansionListener = itemExpansionListener

            snapHelper?.attachToRecyclerView(recyclerView)

            recycledViewPool?.let {
                recyclerView.setRecycledViewPool(it)
            }
            layoutManager?.let {
                recyclerView.layoutManager = it
            }
            hasFixedSize?.let {
                recyclerView.setHasFixedSize(it)
            }
            isNestedScrollingEnabled?.let {
                recyclerView.isNestedScrollingEnabled = it
            }
            isDraggable?.let {
                t.setDraggable(it)
            }
            isExpandable?.let {
                t.setExpandable(it)
            }
            isSwipeableToDismiss?.let {
                t.setSwipeableToDismiss(it)
            }
            isLongPressDraggable?.let {
                t.setLongPressDraggable(it)
            }
            isOnlySameViewTypeReplaceable?.let {
                t.setOnlySameViewTypeReplaceable(it)
            }
            diffUtilCallback?.let {
                t.setDiffUtilCallback(it)
            }
            dividerDrawable.let {
                t.setDivider(it, dividerDrawableInsetLeft, dividerDrawableInsetTop, dividerDrawableInsetRight, dividerDrawableInsetBottom)
            }

            if (set) {
                recyclerView.adapter = t
            }
            return t
        }
    }

    companion object {

        private val defaultDiffUtilCallback = object : PrimeDiffUtilCallback() {

            override fun areItemsTheSame(new: PrimeDataHolder, old: PrimeDataHolder): Boolean {
                return new === old
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(new: PrimeDataHolder, old: PrimeDataHolder): Boolean {
                return new == old
            }

        }

        private fun init() {
            if (PrimeAdapterUtils.viewTypeMap.isEmpty()) {
                PrimeAdapterUtils.instantiateViewTypeManager()
            }
        }

        fun with(recyclerView: RecyclerView): AdapterBuilder {
            init()
            return AdapterBuilder(recyclerView)
        }

    }

}
