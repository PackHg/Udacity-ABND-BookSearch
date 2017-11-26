package com.oz_heng.apps.android.booklisting.Utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Helper methods.
 */
public final class Helper {

    private Helper() {
    }

    /**
     * Show a {@link Toast} with the text parameter.
     * @param context The activity context.
     * @param text Text to be displayed.
     */
    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Return a String in the format "YYYY" if the string parameter is in the format
     * "YYYY-mm-dd" or "YYYY".
     * Return "" if the string parameter is empty.
     * @param text  A String in the format "YYYY-mm-dd" or "YYYY".
     * @return A string in the format "YYYY", or "".
     */
    public static String getYearfrom(String text) {
        if (text.isEmpty()) {
            return "";
        }

        return text.trim().substring(0, 4);
    }

}
