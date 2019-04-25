package com.aminography.primeadapter.sample.tools

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.ListView
import java.util.*
import kotlin.collections.ArrayList

class SwipeMenuHandler(
        private val activity: Activity,
        private val recyclerView: RecyclerView
) : RecyclerView.OnItemTouchListener {

    private var unSwipeableRows: ArrayList<Int>? = null
    private var optionViews: ArrayList<Int>? = null
    private val ignoredViewTypes: Set<Int>

    private val touchSlop: Int = ViewConfiguration.get(recyclerView.context).scaledTouchSlop
    private var touchedX: Float = 0f
    private var touchedY: Float = 0f
    private var defaultSwipingSlop: Int = 0
    private var touchedPosition: Int = 0
    private var touchedView: View? = null

    private var swipeable = false
    private var inSwiping: Boolean = false
    private var paused: Boolean = false

    private var foregroundPartialViewClicked: Boolean = false
    private var backgroundVisible: Boolean = false
    private var backgroundVisiblePosition: Int = 0
    private var backgroundVisibleView: View? = null
    private var heightOutsideRecyclerView: Int = 0
    private var screenHeight: Int = 0
    private var direction: Direction = Direction.RIGHT
    private var onSwipeOptionsClickListener: OnSwipeOptionsClickListener? = null

    private var foregroundView: View? = null
    private var rightBackgroundView: View? = null
    private var leftBackgroundView: View? = null

    private var foregroundViewId: Int = 0
    private var rightBackgroundViewId: Int = 0
    private var leftBackgroundViewId: Int = 0

    private var rightBackgroundViewWidth = 1 // 1 and not 0 to prevent dividing by zero
    private var leftBackgroundViewWidth = 1 // 1 and not 0 to prevent dividing by zero

    init {
        backgroundVisible = false
        backgroundVisiblePosition = -1
        backgroundVisibleView = null
        foregroundPartialViewClicked = false

        unSwipeableRows = ArrayList()
        ignoredViewTypes = HashSet()
        optionViews = ArrayList()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                setEnabled(newState != RecyclerView.SCROLL_STATE_DRAGGING)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {}
        })
    }

    fun setEnabled(enabled: Boolean) {
        paused = !enabled
    }

    override fun onInterceptTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent): Boolean {
        return handleTouchEvent(motionEvent)
    }

    override fun onTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) {
        handleTouchEvent(motionEvent)
    }

    fun setSwipeable(foregroundViewId: Int, rightBackgroundViewId: Int, leftBackgroundViewId: Int, onSwipeOptionsClickListener: OnSwipeOptionsClickListener): SwipeMenuHandler {
        swipeable = true
        if (this.foregroundViewId != 0 && foregroundViewId != this.foregroundViewId) {
            throw IllegalArgumentException("foregroundViewId does not match previously set id")
        }
        this.foregroundViewId = foregroundViewId
        this.rightBackgroundViewId = rightBackgroundViewId
        this.leftBackgroundViewId = leftBackgroundViewId
        this.onSwipeOptionsClickListener = onSwipeOptionsClickListener

        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenHeight = displayMetrics.heightPixels

        return this
    }

    fun setSwipeOptionViews(vararg viewIds: Int): SwipeMenuHandler {
        this.optionViews = ArrayList()
        viewIds.forEach { this.optionViews?.add(it) }
        return this
    }

    fun setUnSwipeableRows(vararg rows: Int): SwipeMenuHandler {
        this.unSwipeableRows = ArrayList()
        rows.forEach { this.unSwipeableRows?.add(it) }
        return this
    }

    //-------------- Checkers for preventing ---------------//

    private fun getOptionViewId(motionEvent: MotionEvent): Int {
        optionViews?.let { ids ->
            for (i in ids.indices) {
                touchedView?.apply {
                    val rect = Rect()
                    val x = motionEvent.rawX.toInt()
                    val y = motionEvent.rawY.toInt()
                    findViewById<View>(ids[i])?.getGlobalVisibleRect(rect)
                    if (rect.contains(x, y)) {
                        return ids[i]
                    }
                }
            }
        }
        return -1
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    @Deprecated("")
    fun closeVisibleBackground() {
        if (backgroundVisibleView == null) {
            return
        }
        backgroundVisibleView?.animate()?.apply {
            duration = DURATION_CLOSE
            translationX(0f)
            setListener(null)
        }

        backgroundVisible = false
        backgroundVisibleView = null
        backgroundVisiblePosition = -1
    }

    private fun closeVisibleBackground(listener: OnSwipeListener?) {
        if (backgroundVisibleView == null) {
            return
        }

        ObjectAnimator.ofFloat(backgroundVisibleView, View.TRANSLATION_X, 0f).apply {
            duration = DURATION_CLOSE
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    listener?.onSwipeOptionsClosed()
                    removeAllListeners()
                }
            })
            start()
        }

        backgroundVisible = false
        backgroundVisibleView = null
        backgroundVisiblePosition = -1
    }

    private fun animateForeground(animateType: Animation, duration: Long, listener: OnSwipeListener? = null) {
        var translateAnimator: ObjectAnimator? = null
        when (animateType) {
            Animation.OPEN -> {
                val distance = if (direction == Direction.LEFT) leftBackgroundViewWidth else -rightBackgroundViewWidth
                translateAnimator = ObjectAnimator.ofFloat(foregroundView, View.TRANSLATION_X, distance.toFloat())
                translateAnimator.duration = duration
                translateAnimator.interpolator = DecelerateInterpolator(1.5f)
                translateAnimator.start()
            }
            Animation.CLOSE -> {
                translateAnimator = ObjectAnimator.ofFloat(foregroundView, View.TRANSLATION_X, 0f)
                translateAnimator.duration = duration
                translateAnimator.interpolator = DecelerateInterpolator(1.5f)
                translateAnimator.start()
            }
        }

        translateAnimator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                listener?.apply {
                    if (animateType == Animation.OPEN) {
                        onSwipeOptionsOpened()
                    } else if (animateType == Animation.CLOSE) {
                        onSwipeOptionsClosed()
                    }
                }
                translateAnimator.removeAllListeners()
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun handleTouchEvent(motionEvent: MotionEvent): Boolean {
        var touchConsumed = false
        if (swipeable) {
            if (rightBackgroundViewWidth < 2) {
                activity.findViewById<View>(rightBackgroundViewId)?.let {
                    rightBackgroundViewWidth = it.width
                }
            }
            if (leftBackgroundViewWidth < 2) {
                activity.findViewById<View>(leftBackgroundViewId)?.let {
                    leftBackgroundViewWidth = it.width
                }
            }
            heightOutsideRecyclerView = screenHeight - recyclerView.height
        }

        when (motionEvent.actionMasked) {

            // When finger touches screen
            MotionEvent.ACTION_DOWN -> {
                if (!paused) {

                    // Find the child view that was touched (perform a hit test)
                    val rect = Rect()
                    val childCount = recyclerView.childCount
                    val coordinates = IntArray(2)
                    recyclerView.getLocationOnScreen(coordinates)
                    // x and y values respective to the recycler view
                    var x = motionEvent.rawX.toInt() - coordinates[0]
                    var y = motionEvent.rawY.toInt() - coordinates[1]
                    var child: View

                    /*
                     * check for every child (row) in the recycler view whether the touched co-ordinates belong to that
                     * respective child and if it does, register that child as the touched view (touchedView)
                     */
                    for (i in 0 until childCount) {
                        child = recyclerView.getChildAt(i)
                        child.getHitRect(rect)
                        if (rect.contains(x, y)) {
                            touchedView = child
                            break
                        }
                    }

                    touchedView?.let { touched ->
                        val fgView = touched.findViewById<View>(foregroundViewId)
                        val rightBgView = touched.findViewById<View>(rightBackgroundViewId)
                        val leftBgView = touched.findViewById<View>(leftBackgroundViewId)
                        if (fgView == null || (rightBgView == null && leftBgView == null)) {
                            return false
                        }

                        touchedX = motionEvent.rawX
                        touchedY = motionEvent.rawY
                        touchedPosition = recyclerView.getChildAdapterPosition(touched)

                        if (shouldIgnoreAction(touchedPosition)) {
                            touchedPosition = ListView.INVALID_POSITION
                            return false   // <-- guard here allows for ignoring events, allowing more than one view type and preventing NPE
                        }

                        if (swipeable) {
                            foregroundView = touched.findViewById(foregroundViewId)
                            rightBackgroundView = touched.findViewById(rightBackgroundViewId)
                            leftBackgroundView = touched.findViewById(leftBackgroundViewId)
                            rightBackgroundView?.minimumHeight = foregroundView?.height ?: 0
                            leftBackgroundView?.minimumHeight = foregroundView?.height ?: 0

                            /*
                             * backgroundVisible is true when the options menu is opened
                             * This block is to register foregroundPartialViewClicked status - Partial view is the view that is still
                             * shown on the screen if the options width is < device width
                             */
                            if (backgroundVisible && foregroundView != null) {
                                x = motionEvent.rawX.toInt()
                                y = motionEvent.rawY.toInt()
                                foregroundView?.getGlobalVisibleRect(rect)
                                foregroundPartialViewClicked = rect.contains(x, y)
                            } else {
                                foregroundPartialViewClicked = false
                            }
                        }
                    }

                    /*
                     * If options menu is shown and the touched position is not the same as the row for which the
                     * options is displayed - close the options menu for the row which is displaying it
                     * (backgroundVisibleView and backgroundVisiblePosition is used for this purpose which registers which view and
                     * which position has it's options menu opened)
                     */
                    recyclerView.getHitRect(rect)
                    if (swipeable && backgroundVisible) {
                        if (touchedPosition != backgroundVisiblePosition) {
                            closeVisibleBackground(null)
                        } else {
                            touchConsumed = true
                        }
                    }
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                if (swipeable) {
                    if (touchedView != null && inSwiping) {
                        // cancel
                        animateForeground(Animation.CLOSE, DURATION_STANDARD)
                    }
                    inSwiping = false
                    rightBackgroundView = null
                    leftBackgroundView = null
                }
                touchedX = 0f
                touchedY = 0f
                touchedView = null
                touchedPosition = ListView.INVALID_POSITION
            }

            // When finger is lifted off the screen (after clicking, flinging, swiping, etc..)
            MotionEvent.ACTION_UP -> {
                if (swipeable && touchedPosition >= 0) {
                    val finalDelta = motionEvent.rawX - touchedX

                    // swipedLeft and swipedRight are true if the user swipes in the respective direction (no conditions)
                    var swipedLeft = false
                    var swipedRight = false

                    // swipedLeftProper and swipedRightProper are true if user swipes in the respective direction and if certain conditions are satisfied (given some few lines below)
                    var swipedLeftProper = false
                    var swipedRightProper = false

                    // if swiped in a direction, make that respective variable true
                    if (inSwiping) {
                        swipedLeft = finalDelta < 0
                        swipedRight = finalDelta > 0
                    }

                    /*
                     * If the user has swiped more than half of the width of the options menu, or if the
                     * velocity of swiping is between min and max fling values
                     * "proper" variable are set true
                     */
                    if (Math.abs(finalDelta) > Math.min(rightBackgroundViewWidth, leftBackgroundViewWidth) / 2 && inSwiping) {
                        swipedLeftProper = finalDelta < 0
                        swipedRightProper = finalDelta > 0
                    }

                    ///////// Manipulation of view based on the 4 variables mentioned above ///////////

                    // if swiped left properly and options menu isn't already visible, animate the foreground to the left
                    if (swipeable && !swipedRight && swipedLeftProper && touchedPosition != RecyclerView.NO_POSITION && unSwipeableRows?.contains(touchedPosition) != true && !backgroundVisible) {
                        direction = Direction.RIGHT
                        animateForeground(Animation.OPEN, DURATION_STANDARD)
                        val downPosition = touchedPosition
                        backgroundVisible = true
                        backgroundVisibleView = foregroundView
                        backgroundVisiblePosition = downPosition
                    } else if (swipeable && !swipedLeft && swipedRightProper && touchedPosition != RecyclerView.NO_POSITION && unSwipeableRows?.contains(touchedPosition) != true && !backgroundVisible) {
                        direction = Direction.LEFT
                        animateForeground(Animation.OPEN, DURATION_STANDARD)
                        val downPosition = touchedPosition
                        backgroundVisible = true
                        backgroundVisibleView = foregroundView
                        backgroundVisiblePosition = downPosition
                    } else if (swipeable && !swipedLeft && swipedRightProper && touchedPosition != RecyclerView.NO_POSITION && unSwipeableRows?.contains(touchedPosition) != true && backgroundVisible) {
                        direction = Direction.RIGHT
                        animateForeground(Animation.CLOSE, DURATION_STANDARD)
                        backgroundVisible = false
                        backgroundVisibleView = null
                        backgroundVisiblePosition = -1
                    } else if (swipeable && !swipedRight && swipedLeftProper && touchedPosition != RecyclerView.NO_POSITION && unSwipeableRows?.contains(touchedPosition) != true && backgroundVisible) {
                        direction = Direction.LEFT
                        animateForeground(Animation.CLOSE, DURATION_STANDARD)
                        backgroundVisible = false
                        backgroundVisibleView = null
                        backgroundVisiblePosition = -1
                    } else if (swipeable && swipedLeft && !backgroundVisible) {
                        // cancel
                        val tempBgView = rightBackgroundView
                        direction = Direction.RIGHT
                        animateForeground(Animation.CLOSE, DURATION_STANDARD, object : OnSwipeListener {
                            override fun onSwipeOptionsClosed() {
                                tempBgView?.visibility = View.VISIBLE
                            }

                            override fun onSwipeOptionsOpened() {}
                        })

                        backgroundVisible = false
                        backgroundVisibleView = null
                        backgroundVisiblePosition = -1
                    } else if (swipeable && swipedRight && !backgroundVisible) {
                        // cancel
                        val tempBgView = rightBackgroundView
                        direction = Direction.LEFT
                        animateForeground(Animation.CLOSE, DURATION_STANDARD, object : OnSwipeListener {
                            override fun onSwipeOptionsClosed() {
                                tempBgView?.visibility = View.VISIBLE
                            }

                            override fun onSwipeOptionsOpened() {}
                        })

                        backgroundVisible = false
                        backgroundVisibleView = null
                        backgroundVisiblePosition = -1
                    } else if (swipeable && swipedRight) {
                        // cancel
                        direction = Direction.RIGHT
                        animateForeground(Animation.CLOSE, DURATION_STANDARD)
                        backgroundVisible = false
                        backgroundVisibleView = null
                        backgroundVisiblePosition = -1
                    } else if (swipeable && swipedLeft) {
                        // cancel
                        direction = Direction.LEFT
                        animateForeground(Animation.CLOSE, DURATION_STANDARD)
                        backgroundVisible = false
                        backgroundVisibleView = null
                        backgroundVisiblePosition = -1
                    } else if (!swipedRight && !swipedLeft) {
                        // if partial foreground view is clicked (see ACTION_DOWN) bring foreground back to original position
                        // backgroundVisible is true automatically since it's already checked in ACTION_DOWN block
                        if (swipeable && foregroundPartialViewClicked) {
                            animateForeground(Animation.CLOSE, DURATION_STANDARD)
                            backgroundVisible = false
                            backgroundVisibleView = null
                            backgroundVisiblePosition = -1
                        } else if (swipeable && backgroundVisible) {
                            val optionId = getOptionViewId(motionEvent)
                            if (optionId >= 0 && touchedPosition >= 0) {
                                touchConsumed = true
                                val downPosition = touchedPosition
                                closeVisibleBackground(object : OnSwipeListener {
                                    override fun onSwipeOptionsClosed() {
                                        // TODO: return DataHolder associated with downPosition
                                        onSwipeOptionsClickListener?.onSwipeOptionClicked(optionId, downPosition)
                                    }

                                    override fun onSwipeOptionsOpened() {}
                                })
                            }
                        }
                    }
                }
                // if clicked and not swiped

                touchedX = 0f
                touchedY = 0f
                touchedView = null
                touchedPosition = ListView.INVALID_POSITION
                inSwiping = false
                rightBackgroundView = null
                leftBackgroundView = null
            }

            // when finger is moving across the screen (and not yet lifted)
            MotionEvent.ACTION_MOVE -> {
                if (!paused && swipeable) {

                    val deltaX = motionEvent.rawX - touchedX
                    val deltaY = motionEvent.rawY - touchedY

                    /*
                     * inSwiping variable which is set to true here is used to alter the swipedLeft, swipedRightProper
                     * variables in "ACTION_UP" block by checking if user is actually swiping at present or not
                     */
                    if (!inSwiping && Math.abs(deltaX) > touchSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                        inSwiping = true
                        defaultSwipingSlop = if (deltaX > 0) touchSlop else -touchSlop
                    }

                    // This block moves the foreground along with the finger when swiping
                    if (swipeable && inSwiping && unSwipeableRows?.contains(touchedPosition) != true) {
                        if (rightBackgroundView == null) {
                            rightBackgroundView = touchedView?.findViewById(rightBackgroundViewId)
                            rightBackgroundView?.visibility = View.VISIBLE
                        }
                        if (leftBackgroundView == null) {
                            leftBackgroundView = touchedView?.findViewById(leftBackgroundViewId)
                            leftBackgroundView?.visibility = View.VISIBLE
                        }

                        // if fg is being swiped left
                        if (deltaX < 0 && Math.abs(deltaX) > touchSlop && !backgroundVisible) {
                            val translateAmount = deltaX - defaultSwipingSlop

                            // swipe fg till width of bg. If swiped further, nothing happens (stalls at width of bg)
                            foregroundView?.apply {
                                translationX = if (Math.abs(translateAmount) > rightBackgroundViewWidth) -rightBackgroundViewWidth.toFloat() else translateAmount
                                if (translationX > 0) translationX = 0f
                            }
                        } else if (deltaX > 0 && Math.abs(deltaX) > touchSlop && !backgroundVisible) {
                            val translateAmount = deltaX - defaultSwipingSlop

                            // swipe fg till width of bg. If swiped further, nothing happens (stalls at width of bg)
                            foregroundView?.apply {
                                translationX = if (Math.abs(translateAmount) > leftBackgroundViewWidth) leftBackgroundViewWidth.toFloat() else translateAmount
                                if (translationX < 0) translationX = 0f
                            }
                        } else if (deltaX > 0 && direction == Direction.RIGHT && backgroundVisible) {
                            // for closing rightOptions
                            val translateAmount = deltaX - defaultSwipingSlop - rightBackgroundViewWidth

                            // swipe fg till it reaches original position. If swiped further, nothing happens (stalls at 0)
                            foregroundView?.apply {
                                translationX = if (translateAmount > 0) 0f else translateAmount
                            }
                        } else if (deltaX < 0 && direction == Direction.LEFT && backgroundVisible) {
                            // for closing rightOptions
                            val translateAmount = deltaX - defaultSwipingSlop + leftBackgroundViewWidth

                            // swipe fg till it reaches original position. If swiped further, nothing happens (stalls at 0)
                            foregroundView?.apply {
                                translationX = if (translateAmount < 0) 0f else translateAmount
                            }
                        }
                        return true
                    } else if (swipeable && inSwiping && unSwipeableRows?.contains(touchedPosition) == true) {
                        if (deltaX < touchSlop && !backgroundVisible) {
                            val translateAmount = deltaX - defaultSwipingSlop
                            if (rightBackgroundView == null) rightBackgroundView = touchedView?.findViewById(rightBackgroundViewId)
                            rightBackgroundView?.visibility = View.GONE

                            // swipe fg till width of bg. If swiped further, nothing happens (stalls at width of bg)
                            foregroundView?.apply {
                                translationX = translateAmount / 5
                                if (translationX > 0) translationX = 0f
                            }
                        }
                        return true
                    }
                }
            }
        }
        return touchConsumed
    }

    private fun shouldIgnoreAction(touchedPosition: Int): Boolean {
        return ignoredViewTypes.contains(recyclerView.adapter?.getItemViewType(touchedPosition))
    }

    private enum class Animation {
        OPEN, CLOSE
    }

    private enum class Direction {
        LEFT, RIGHT
    }

    interface OnSwipeOptionsClickListener {
        fun onSwipeOptionClicked(viewID: Int, position: Int)
    }

    interface OnSwipeListener {

        fun onSwipeOptionsClosed()

        fun onSwipeOptionsOpened()
    }

    // TEST CODE:
    //
    //    val touchListener = SwipeMenuHandler(activity!!, recyclerView)
    //    touchListener.setSwipeOptionViews(R.id.restoreImageView, R.id.restore2ImageView)
    //    .setSwipeable(R.id.foregroundLinearLayout, R.id.backgroundFrameLayout, R.id.background2FrameLayout, object : SwipeMenuHandler.OnSwipeOptionsClickListener{
    //        override fun onSwipeOptionClicked(viewID: Int, position: Int) {
    //            when (viewID) {
    //                R.id.restoreImageView -> {
    //                    toast("Restore Clicked!")
    //                }
    //                R.id.restore2ImageView -> {
    //                    toast("Restore 2 Clicked!")
    //                }
    //            }
    //        }
    //    })
    //
    //    recyclerView.addOnItemTouchListener(touchListener)
    //
    //
    // XML:
    //
    //    <FrameLayout
    //        android:id="@+id/backgroundFrameLayout"
    //        android:layout_width="wrap_content"
    //        android:layout_height="match_parent"
    //        android:layout_gravity="center_vertical|right"
    //        android:background="@color/colorAccent">
    //
    //        <android.support.v7.widget.AppCompatImageView
    //            android:id="@+id/restoreImageView"
    //            android:layout_width="wrap_content"
    //            android:layout_height="wrap_content"
    //            android:layout_gravity="center_vertical"
    //            android:padding="16dp"
    //            app:srcCompat="@drawable/ic_settings_backup_restore_black_24dp" />
    //     </FrameLayout>
    //
    //    <FrameLayout
    //        android:id="@+id/background2FrameLayout"
    //        android:layout_width="wrap_content"
    //        android:layout_height="match_parent"
    //        android:layout_gravity="center_vertical|left"
    //        android:background="@color/colorPrimary">
    //
    //        <android.support.v7.widget.AppCompatImageView
    //            android:id="@+id/restore2ImageView"
    //            android:layout_width="wrap_content"
    //            android:layout_height="wrap_content"
    //            android:layout_gravity="center_vertical"
    //            android:paddingLeft="32dp"
    //            android:paddingTop="16dp"
    //            android:paddingRight="32dp"
    //            android:paddingBottom="16dp"
    //            app:srcCompat="@drawable/ic_settings_backup_restore_black_24dp" />
    //    </FrameLayout>

    // See Also: https://github.com/FanFataL/swipe-controller-demo/blob/master/app/src/main/java/pl/fanfatal/swipecontrollerdemo/SwipeController.java
}

private const val DURATION_STANDARD = 300L
private const val DURATION_CLOSE = 150L