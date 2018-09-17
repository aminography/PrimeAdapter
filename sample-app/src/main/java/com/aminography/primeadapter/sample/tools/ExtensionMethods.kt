package com.aminography.primeadapter.sample.tools

import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.widget.ImageView
import com.aminography.primeadapter.sample.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

/**
 * Created by aminography on 6/6/2018.
 */

fun ImageView.loadImage(imageDrawable: Drawable?, circleCrop: Boolean = false, @DrawableRes placeholderResId: Int = R.drawable.default_placeholder) {
    val requestBuilder = Glide.with(context)
            .load(imageDrawable)
            .apply(RequestOptions.placeholderOf(placeholderResId))
    if (circleCrop) requestBuilder.apply(RequestOptions.circleCropTransform())
    requestBuilder.into(this)
}