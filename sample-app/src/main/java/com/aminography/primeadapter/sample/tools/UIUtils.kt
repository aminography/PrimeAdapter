package com.aminography.primeadapter.sample.tools

import android.content.Context
import android.content.Intent


/**
 * Created by aminography on 8/10/2018.
 */
class UIUtils {

    companion object {

        fun dp2px(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }

}
