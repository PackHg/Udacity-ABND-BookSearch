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

    /**
     * Return a String of keywords in the format "keyword1+keyword2+...". If the String parameter
     * is empty, return an empty String.
     * @param s String parameter.
     * @return a String of keywords.
     */
    public static String getKeywordsfrom(String s) {
        if (s.isEmpty()) {
            return "";
        }

        String[] keywordArray  =  s.split("\\s+");
        String  keywords = keywordArray[0];
        for (int i=1; i < keywordArray.length; i++) {
            keywords = keywords.concat("+" + keywordArray[i]);
        }
        return keywords;
    }

}
