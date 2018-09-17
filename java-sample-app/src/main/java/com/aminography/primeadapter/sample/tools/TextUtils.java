package com.aminography.primeadapter.sample.tools;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by aminography on 8/10/2018.
 */
public class TextUtils {

    private static final long B = 1;
    private static final long KB = B * 1024;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;

    private static final char DELIMITER = ',';
    private static final DecimalFormat decimalFormat = new DecimalFormat();
    private static DecimalFormatSymbols symbols = new DecimalFormatSymbols();

    static {
        symbols.setGroupingSeparator(DELIMITER);
        decimalFormat.setDecimalFormatSymbols(symbols);
        decimalFormat.setGroupingSize(3);
    }

    public static String formatFileSize(long size) {
        if (size < KB) {
            return String.format(Locale.getDefault(), "%.1f B", (double) size);
        } else if (size < MB) {
            return String.format(Locale.getDefault(), "%.1f KB", (double) size / KB);
        } else if (size < GB) {
            return String.format(Locale.getDefault(), "%.2f MB", (double) size / MB);
        } else {
            return String.format(Locale.getDefault(), "%.2f GB", (double) size / GB);
        }
    }

}
