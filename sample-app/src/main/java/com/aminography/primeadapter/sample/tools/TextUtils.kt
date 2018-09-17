package com.aminography.primeadapter.sample.tools

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


/**
 * Created by aminography on 8/10/2018.
 */
class TextUtils {

    companion object {

        private const val B = 1
        private const val KB = B * 1024
        private const val MB = KB * 1024
        private const val GB = MB * 1024

        private const val DELIMITER = ','
        private val decimalFormat = DecimalFormat()
        private val symbols = DecimalFormatSymbols()

        init {
            symbols.groupingSeparator = DELIMITER
            decimalFormat.decimalFormatSymbols = symbols
            decimalFormat.groupingSize = 3
        }

        fun formatFileSize(size: Long): String {
            return when {
                size < KB -> String.format("%.1f B", size.toDouble())
                size < MB -> String.format("%.1f KB", size.toDouble() / KB)
                size < GB -> String.format("%.2f MB", size.toDouble() / MB)
                else -> String.format("%.2f GB", size.toDouble() / GB)
            }
        }

    }
}
