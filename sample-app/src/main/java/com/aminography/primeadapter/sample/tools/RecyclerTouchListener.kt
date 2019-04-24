package com.aminography.primeadapter.sample.tools

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.ListView
import java.util.*
import kotlin.collections.ArrayList

class RecyclerTouchListener(private val act: Activity, // Fixed properties
                            private val mRecyclerView: RecyclerView?) : RecyclerView.OnItemTouchListener {
    private var unSwipeableRows: ArrayList<Int>? = null
    /*
     * independentViews are views on the foreground layer which when clicked, act "independent" from the foreground
     * ie, they are treated separately from the "row click" action
     */
    private var optionViews: ArrayList<Int>? = null
    private val ignoredViewTypes: Set<Int>
    // Cached ViewConfiguration and system-wide constant values
    private val touchSlop: Int
    private val minFlingVel: Int
    private val maxFlingVel: Int
    private val ANIMATION_STANDARD: Long = 300
    private val ANIMATION_CLOSE: Long = 150
    // private SwipeListener mSwipeListener;
    private var bgWidth = 1 // 1 and not 0 to prevent dividing by zero
    private var bgWidth2 = 1 // 1 and not 0 to prevent dividing by zero
    // Transient properties
    // private List<PendingDismissData> mPendingDismisses = new ArrayList<>();
    private var touchedX: Float = 0.toFloat()
    private var touchedY: Float = 0.toFloat()
    private var isFgSwiping: Boolean = false
    private var mSwipingSlop: Int = 0
    private var mVelocityTracker: VelocityTracker? = null
    private var touchedPosition: Int = 0
    private var touchedView: View? = null
    private var mPaused: Boolean = false
    private var bgVisible: Boolean = false
    private var fgPartialViewClicked: Boolean = false
    private var bgVisiblePosition: Int = 0
    private var bgVisibleView: View? = null
    private var heightOutsideRView: Int = 0
    private var screenHeight: Int = 0
    // Foreground view (to be swiped), Background view (to show)
    private var fgView: View? = null
    private var bgView: View? = null
    private var bgView2: View? = null
    //view ID
    private var fgViewID: Int = 0
    private var bgViewID: Int = 0
    private var bgViewID2: Int = 0
    private var mBgClickListener: OnSwipeOptionsClickListener? = null
    // user choices
    private var swipeable = false

    private var isLeft = false

    init {
        val vc = ViewConfiguration.get(mRecyclerView?.context)
        touchSlop = vc.scaledTouchSlop
        minFlingVel = vc.scaledMinimumFlingVelocity * 16
        maxFlingVel = vc.scaledMaximumFlingVelocity
        bgVisible = false
        bgVisiblePosition = -1
        bgVisibleView = null
        fgPartialViewClicked = false
        unSwipeableRows = ArrayList()
        ignoredViewTypes = HashSet()
        optionViews = ArrayList()

        mRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                /**
                 * This will ensure that this RecyclerTouchListener is paused during recycler view scrolling.
                 * If a scroll listener is already assigned, the caller should still pass scroll changes through
                 * to this listener.
                 */
                setEnabled(newState != RecyclerView.SCROLL_STATE_DRAGGING)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {}
        })
    }

    /**
     * Enables or disables (pauses or resumes) watching for swipe-to-dismiss gestures.
     *
     * @param enabled Whether or not to watch for gestures.
     */
    fun setEnabled(enabled: Boolean) {
        mPaused = !enabled
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, motionEvent: MotionEvent): Boolean {
        return handleTouchEvent(motionEvent)
    }

    override fun onTouchEvent(rv: RecyclerView, motionEvent: MotionEvent) {
        handleTouchEvent(motionEvent)
    }

    //////////////// Swipeable ////////////////////

    fun setSwipeable(foregroundID: Int, backgroundID: Int, backgroundID2: Int, listener: OnSwipeOptionsClickListener): RecyclerTouchListener {
        this.swipeable = true
        if (fgViewID != 0 && foregroundID != fgViewID) {
            throw IllegalArgumentException("foregroundID does not match previously set ID")
        }
        fgViewID = foregroundID
        bgViewID = backgroundID
        bgViewID2 = backgroundID2
        this.mBgClickListener = listener

        val displayMetrics = DisplayMetrics()
        act.windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenHeight = displayMetrics.heightPixels

        return this
    }

    fun setSwipeOptionViews(vararg viewIds: Int): RecyclerTouchListener {
        this.optionViews = ArrayList()
        viewIds.forEach { this.optionViews?.add(it) }
        return this
    }

    fun setUnSwipeableRows(vararg rows: Int): RecyclerTouchListener {
        this.unSwipeableRows = ArrayList()
        rows.forEach { this.unSwipeableRows?.add(it) }
        return this
    }

    //-------------- Checkers for preventing ---------------//

    private fun getOptionViewID(motionEvent: MotionEvent): Int {
        for (i in optionViews!!.indices) {
            if (touchedView != null) {
                val rect = Rect()
                val x = motionEvent.rawX.toInt()
                val y = motionEvent.rawY.toInt()
                touchedView!!.findViewById<View>(optionViews!![i]).getGlobalVisibleRect(rect)
                if (rect.contains(x, y)) {
                    return optionViews!![i]
                }
            }
        }
        return -1
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    @Deprecated("")
    fun closeVisibleBG() {
        if (bgVisibleView == null) {
            Log.e(TAG, "No rows found for which background options are visible")
            return
        }
        bgVisibleView!!.animate()
                .translationX(0f)
                .setDuration(ANIMATION_CLOSE)
                .setListener(null)

        bgVisible = false
        bgVisibleView = null
        bgVisiblePosition = -1
    }

    private fun closeVisibleBG(mSwipeCloseListener: OnSwipeListener?) {
        if (bgVisibleView == null) {
            Log.e(TAG, "No rows found for which background options are visible")
            return
        }
        val translateAnimator = ObjectAnimator.ofFloat(bgVisibleView, View.TRANSLATION_X, 0f)
        translateAnimator.duration = ANIMATION_CLOSE
        translateAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                mSwipeCloseListener?.onSwipeOptionsClosed()
                translateAnimator.removeAllListeners()
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
        translateAnimator.start()

        bgVisible = false
        bgVisibleView = null
        bgVisiblePosition = -1
    }

    private fun animateFG(downView: View?, animateType: Animation, duration: Long) {
        //        boolean isLeft = false;
        if (animateType == Animation.OPEN) {
            val translateAnimator = ObjectAnimator.ofFloat<View>(fgView, View.TRANSLATION_X, (if (isLeft) bgWidth2 else -bgWidth).toFloat())
            translateAnimator.duration = duration
            translateAnimator.interpolator = DecelerateInterpolator(1.5f)
            translateAnimator.start()
        } else if (animateType == Animation.CLOSE) {
            val translateAnimator = ObjectAnimator.ofFloat(fgView, View.TRANSLATION_X, 0f)
            translateAnimator.duration = duration
            translateAnimator.interpolator = DecelerateInterpolator(1.5f)
            translateAnimator.start()
        }
    }

    private fun animateFG(downView: View?, animateType: Animation, duration: Long, mSwipeCloseListener: OnSwipeListener?) {
        //        boolean isLeft = false;
        val translateAnimator: ObjectAnimator
        if (animateType == Animation.OPEN) {
            translateAnimator = ObjectAnimator.ofFloat(fgView, View.TRANSLATION_X, (if (isLeft) bgWidth2 else -bgWidth).toFloat())
            translateAnimator.duration = duration
            translateAnimator.interpolator = DecelerateInterpolator(1.5f)
            translateAnimator.start()
        } else
        /*if (animateType == Animation.CLOSE)*/ {
            translateAnimator = ObjectAnimator.ofFloat(fgView, View.TRANSLATION_X, 0f)
            translateAnimator.duration = duration
            translateAnimator.interpolator = DecelerateInterpolator(1.5f)
            translateAnimator.start()
        }

        translateAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                if (mSwipeCloseListener != null) {
                    if (animateType == Animation.OPEN) {
                        mSwipeCloseListener.onSwipeOptionsOpened()
                    } else if (animateType == Animation.CLOSE) {
                        mSwipeCloseListener.onSwipeOptionsClosed()
                    }
                }
                translateAnimator.removeAllListeners()
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun handleTouchEvent(motionEvent: MotionEvent): Boolean {
        var consumed = false
        if (swipeable) {
            if (bgWidth < 2) {
                if (act.findViewById<View>(bgViewID) != null) {
                    bgWidth = act.findViewById<View>(bgViewID).width
                }
            }
            if (bgWidth2 < 2) {
                if (act.findViewById<View>(bgViewID2) != null) {
                    bgWidth2 = act.findViewById<View>(bgViewID2).width
                }
            }
            heightOutsideRView = screenHeight - mRecyclerView!!.height
        }

        when (motionEvent.actionMasked) {

            // When finger touches screen
            MotionEvent.ACTION_DOWN -> {
                if (!mPaused) {

                    // Find the child view that was touched (perform a hit test)
                    val rect = Rect()
                    val childCount = mRecyclerView!!.childCount
                    val listViewCoords = IntArray(2)
                    mRecyclerView.getLocationOnScreen(listViewCoords)
                    // x and y values respective to the recycler view
                    var x = motionEvent.rawX.toInt() - listViewCoords[0]
                    var y = motionEvent.rawY.toInt() - listViewCoords[1]
                    var child: View

                    /*
                     * check for every child (row) in the recycler view whether the touched co-ordinates belong to that
                     * respective child and if it does, register that child as the touched view (touchedView)
                     */
                    for (i in 0 until childCount) {
                        child = mRecyclerView.getChildAt(i)
                        child.getHitRect(rect)
                        if (rect.contains(x, y)) {
                            touchedView = child
                            break
                        }
                    }

                    if (touchedView != null) {
                        val fgView1 = touchedView!!.findViewById<View>(fgViewID)
                        val bgView1 = touchedView!!.findViewById<View>(bgViewID)
                        val bgView21 = touchedView!!.findViewById<View>(bgViewID2)
                        if (fgView1 == null || bgView1 == null && bgView21 == null) {
                            return false
                        }

                        touchedX = motionEvent.rawX
                        touchedY = motionEvent.rawY
                        touchedPosition = mRecyclerView.getChildAdapterPosition(touchedView!!)

                        if (shouldIgnoreAction(touchedPosition)) {
                            touchedPosition = ListView.INVALID_POSITION
                            return false   // <-- guard here allows for ignoring events, allowing more than one view type and preventing NPE
                        }

                        if (swipeable) {
                            mVelocityTracker = VelocityTracker.obtain()
                            mVelocityTracker!!.addMovement(motionEvent)
                            fgView = touchedView!!.findViewById(fgViewID)
                            bgView = touchedView!!.findViewById(bgViewID)
                            bgView2 = touchedView!!.findViewById(bgViewID2)
                            bgView!!.minimumHeight = fgView!!.height
                            bgView2!!.minimumHeight = fgView!!.height

                            /*
                             * bgVisible is true when the options menu is opened
                             * This block is to register fgPartialViewClicked status - Partial view is the view that is still
                             * shown on the screen if the options width is < device width
                             */
                            if (bgVisible && fgView != null) {
                                x = motionEvent.rawX.toInt()
                                y = motionEvent.rawY.toInt()
                                fgView!!.getGlobalVisibleRect(rect)
                                fgPartialViewClicked = rect.contains(x, y)
                            } else {
                                fgPartialViewClicked = false
                            }
                        }
                    }

                    /*
                     * If options menu is shown and the touched position is not the same as the row for which the
                     * options is displayed - close the options menu for the row which is displaying it
                     * (bgVisibleView and bgVisiblePosition is used for this purpose which registers which view and
                     * which position has it's options menu opened)
                     */
                    x = motionEvent.rawX.toInt()
                    y = motionEvent.rawY.toInt()
                    mRecyclerView.getHitRect(rect)
                    if (swipeable && bgVisible) {
                        if (touchedPosition != bgVisiblePosition) {
                            closeVisibleBG(null)
                        } else {
                            consumed = true
                        }
                    }
                }
            }

            MotionEvent.ACTION_CANCEL -> {

                if (mVelocityTracker != null) {
                    if (swipeable) {
                        if (touchedView != null && isFgSwiping) {
                            // cancel
                            animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD)
                        }
                        mVelocityTracker!!.recycle()
                        mVelocityTracker = null
                        isFgSwiping = false
                        bgView = null
                        bgView2 = null
                    }
                    touchedX = 0f
                    touchedY = 0f
                    touchedView = null
                    touchedPosition = ListView.INVALID_POSITION
                }
            }

            // When finger is lifted off the screen (after clicking, flinging, swiping, etc..)
            MotionEvent.ACTION_UP -> {
                run {

                    if (mVelocityTracker != null && swipeable && touchedPosition >= 0) {

                        // swipedLeft and swipedRight are true if the user swipes in the respective direction (no conditions)
                        var swipedLeft = false
                        var swipedRight = false
                        /*
                     * swipedLeftProper and swipedRightProper are true if user swipes in the respective direction
                     * and if certain conditions are satisfied (given some few lines below)
                     */
                        var swipedLeftProper = false
                        var swipedRightProper = false

                        val finalDelta = motionEvent.rawX - touchedX

                        // if swiped in a direction, make that respective variable true
                        if (isFgSwiping) {
                            swipedLeft = finalDelta < 0
                            swipedRight = finalDelta > 0
                        }

                        /*
                     * If the user has swiped more than half of the width of the options menu, or if the
                     * velocity of swiping is between min and max fling values
                     * "proper" variable are set true
                     */
                        if (Math.abs(finalDelta) > Math.min(bgWidth, bgWidth2) / 2 && isFgSwiping) {
                            swipedLeftProper = finalDelta < 0
                            swipedRightProper = finalDelta > 0
                            //                } else if (swipeable) {
                            //                    mVelocityTracker.addMovement(motionEvent);
                            //                    mVelocityTracker.computeCurrentVelocity(1000);
                            //                    float velocityX = mVelocityTracker.getXVelocity();
                            //                    float absVelocityX = Math.abs(velocityX);
                            //                    float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
                            //                    if (minFlingVel <= absVelocityX && absVelocityX <= maxFlingVel && absVelocityY < absVelocityX && isFgSwiping) {
                            //                        // dismiss only if flinging in the same direction as dragging
                            //                        swipedLeftProper = (velocityX < 0) == (finalDelta < 0);
                            //                        swipedRightProper = (velocityX > 0) == (finalDelta > 0);
                            //                    }
                        }

                        ///////// Manipulation of view based on the 4 variables mentioned above ///////////

                        // if swiped left properly and options menu isn't already visible, animate the foreground to the left
                        if (swipeable && !swipedRight && swipedLeftProper && touchedPosition != RecyclerView.NO_POSITION && !unSwipeableRows!!.contains(touchedPosition) && !bgVisible) {

                            val downPosition = touchedPosition
                            //TODO - speed
                            isLeft = false
                            animateFG(touchedView, Animation.OPEN, ANIMATION_STANDARD)
                            bgVisible = true
                            bgVisibleView = fgView
                            bgVisiblePosition = downPosition
                        } else if (swipeable && !swipedLeft && swipedRightProper && touchedPosition != RecyclerView.NO_POSITION && !unSwipeableRows!!.contains(touchedPosition) && !bgVisible) {

                            val downPosition = touchedPosition
                            //TODO - speed
                            isLeft = true
                            animateFG(touchedView, Animation.OPEN, ANIMATION_STANDARD)
                            bgVisible = true
                            bgVisibleView = fgView
                            bgVisiblePosition = downPosition
                        } else if (swipeable && !swipedLeft && swipedRightProper && touchedPosition != RecyclerView.NO_POSITION && !unSwipeableRows!!.contains(touchedPosition) && bgVisible) {

                            //TODO - speed
                            isLeft = false
                            animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD)
                            bgVisible = false
                            bgVisibleView = null
                            bgVisiblePosition = -1
                        } else if (swipeable && !swipedRight && swipedLeftProper && touchedPosition != RecyclerView.NO_POSITION && !unSwipeableRows!!.contains(touchedPosition) && bgVisible) {

                            //TODO - speed
                            isLeft = true
                            animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD)
                            bgVisible = false
                            bgVisibleView = null
                            bgVisiblePosition = -1
                        } else if (swipeable && swipedLeft && !bgVisible) {
                            // cancel
                            val tempBgView = bgView
                            isLeft = false
                            animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD, object : OnSwipeListener {
                                override fun onSwipeOptionsClosed() {
                                    if (tempBgView != null) tempBgView.visibility = View.VISIBLE
                                }

                                override fun onSwipeOptionsOpened() {}
                            })

                            bgVisible = false
                            bgVisibleView = null
                            bgVisiblePosition = -1
                        } else if (swipeable && swipedRight && !bgVisible) {
                            // cancel
                            val tempBgView = bgView
                            isLeft = true
                            animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD, object : OnSwipeListener {
                                override fun onSwipeOptionsClosed() {
                                    if (tempBgView != null) tempBgView.visibility = View.VISIBLE
                                }

                                override fun onSwipeOptionsOpened() {}
                            })

                            bgVisible = false
                            bgVisibleView = null
                            bgVisiblePosition = -1
                        } else if (swipeable && swipedRight) {
                            // cancel
                            isLeft = false
                            animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD)
                            bgVisible = false
                            bgVisibleView = null
                            bgVisiblePosition = -1
                        } else if (swipeable && swipedLeft) {
                            // cancel
                            isLeft = true
                            animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD)
                            bgVisible = false
                            bgVisibleView = null
                            bgVisiblePosition = -1
                        } else if (!swipedRight && !swipedLeft) {
                            // if partial foreground view is clicked (see ACTION_DOWN) bring foreground back to original position
                            // bgVisible is true automatically since it's already checked in ACTION_DOWN block
                            if (swipeable && fgPartialViewClicked) {
                                animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD)
                                bgVisible = false
                                bgVisibleView = null
                                bgVisiblePosition = -1
                            } else if (swipeable && bgVisible) {
                                val optionID = getOptionViewID(motionEvent)
                                if (optionID >= 0 && touchedPosition >= 0) {
                                    consumed = true
                                    val downPosition = touchedPosition
                                    closeVisibleBG(object : OnSwipeListener {
                                        override fun onSwipeOptionsClosed() {
                                            mBgClickListener!!.onSwipeOptionClicked(optionID, downPosition)
                                        }

                                        override fun onSwipeOptionsOpened() {}
                                    })
                                }
                            }
                        }
                    }
                    // if clicked and not swiped

                    if (swipeable) {
                        mVelocityTracker!!.recycle()
                        mVelocityTracker = null
                    }
                    touchedX = 0f
                    touchedY = 0f
                    touchedView = null
                    touchedPosition = ListView.INVALID_POSITION
                    isFgSwiping = false
                    bgView = null
                    bgView2 = null
                }
            }

            // when finger is moving across the screen (and not yet lifted)
            MotionEvent.ACTION_MOVE -> {
                if (mVelocityTracker != null && !mPaused && swipeable) {

                    mVelocityTracker!!.addMovement(motionEvent)
                    val deltaX = motionEvent.rawX - touchedX
                    val deltaY = motionEvent.rawY - touchedY

                    /*
                     * isFgSwiping variable which is set to true here is used to alter the swipedLeft, swipedRightProper
                     * variables in "ACTION_UP" block by checking if user is actually swiping at present or not
                     */
                    if (!isFgSwiping && Math.abs(deltaX) > touchSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                        isFgSwiping = true
                        mSwipingSlop = if (deltaX > 0) touchSlop else -touchSlop
                    }

                    // This block moves the foreground along with the finger when swiping
                    if (swipeable && isFgSwiping && !unSwipeableRows!!.contains(touchedPosition)) {
                        if (bgView == null) {
                            bgView = touchedView!!.findViewById(bgViewID)
                            bgView!!.visibility = View.VISIBLE
                        }
                        if (bgView2 == null) {
                            bgView2 = touchedView!!.findViewById(bgViewID2)
                            bgView2!!.visibility = View.VISIBLE
                        }

                        // if fg is being swiped left
                        if (deltaX < 0 && Math.abs(deltaX) > touchSlop && !bgVisible) {
                            val translateAmount = deltaX - mSwipingSlop
                            //                        if ((Math.abs(translateAmount) > bgWidth ? -bgWidth : translateAmount) <= 0) {

                            // swipe fg till width of bg. If swiped further, nothing happens (stalls at width of bg)
                            fgView!!.translationX = if (Math.abs(translateAmount) > bgWidth) -bgWidth.toFloat() else translateAmount
                            if (fgView!!.translationX > 0) fgView!!.translationX = 0f
                            //                        }
                        } else if (deltaX > 0 && Math.abs(deltaX) > touchSlop && !bgVisible) {
                            val translateAmount = deltaX - mSwipingSlop
                            //                        if ((Math.abs(translateAmount) > bgWidth ? -bgWidth : translateAmount) <= 0) {

                            // swipe fg till width of bg. If swiped further, nothing happens (stalls at width of bg)
                            fgView!!.translationX = if (Math.abs(translateAmount) > bgWidth2) bgWidth2.toFloat() else translateAmount
                            if (fgView!!.translationX < 0) fgView!!.translationX = 0f
                            //                        }
                        } else if (deltaX > 0 && !isLeft && bgVisible) {
                            // for closing rightOptions
                            val translateAmount = deltaX - mSwipingSlop - bgWidth

                            // swipe fg till it reaches original position. If swiped further, nothing happens (stalls at 0)
                            fgView!!.translationX = if (translateAmount > 0) 0f else translateAmount
                        } else if (deltaX < 0 && isLeft && bgVisible) {
                            // for closing rightOptions
                            val translateAmount = deltaX - mSwipingSlop + bgWidth2

                            // swipe fg till it reaches original position. If swiped further, nothing happens (stalls at 0)
                            fgView!!.translationX = if (translateAmount < 0) 0f else translateAmount
                        }// if fg is being swiped left - Amin
                        // if fg is being swiped right
                        // if fg is being swiped right - Amin
                        return true
                    } else if (swipeable && isFgSwiping && unSwipeableRows!!.contains(touchedPosition)) {
                        if (deltaX < touchSlop && !bgVisible) {
                            val translateAmount = deltaX - mSwipingSlop
                            if (bgView == null)
                                bgView = touchedView!!.findViewById(bgViewID)

                            if (bgView != null)
                                bgView!!.visibility = View.GONE

                            // swipe fg till width of bg. If swiped further, nothing happens (stalls at width of bg)
                            fgView!!.translationX = translateAmount / 5
                            if (fgView!!.translationX > 0) fgView!!.translationX = 0f

                        }
                        return true
                    }// moves the fg slightly to give the illusion of an "unswipeable" row
                }
            }
        }
        return consumed
    }

    private fun shouldIgnoreAction(touchedPosition: Int): Boolean {
        return mRecyclerView == null || ignoredViewTypes.contains(mRecyclerView.adapter!!.getItemViewType(touchedPosition))
    }

    private enum class Animation {
        OPEN, CLOSE
    }

    interface OnSwipeOptionsClickListener {
        fun onSwipeOptionClicked(viewID: Int, position: Int)
    }

    interface OnSwipeListener {

        fun onSwipeOptionsClosed()

        fun onSwipeOptionsOpened()
    }

    companion object {
        private val TAG = "RecyclerTouchListener"
    }

    // TEST CODE:
    //
    //    val touchListener = RecyclerTouchListener(activity!!, recyclerView)
    //    touchListener.setSwipeOptionViews(R.id.restoreImageView, R.id.restore2ImageView)
    //    .setSwipeable(R.id.foregroundLinearLayout, R.id.backgroundFrameLayout, R.id.background2FrameLayout, object : RecyclerTouchListener.OnSwipeOptionsClickListener{
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