package com.aminography.primeadapter.sample.tools;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.aminography.primeadapter.sample.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

/**
 * Created by aminography on 8/10/2018.
 */
public class UIUtils {

    public static void loadImage(ImageView imageView, Drawable imageDrawable) {
        Glide.with(imageView.getContext())
                .load(imageDrawable)
                .apply(RequestOptions.placeholderOf(R.drawable.default_placeholder))
                .into(imageView);
    }

    public static float dp2px(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}
