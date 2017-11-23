package com.oz_heng.apps.android.booklisting.Utils;

import android.content.Context;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
     * Return a String in the format "YYYY" from a string parameter that is in the format
     * "YYYY/mm/dd".
     * @param text  A String in the format "YYYY/mm/dd".
     * @return String in the format "YYYY".
     */
    public static String getYearfrom(String text) {
        SimpleDateFormat input = new SimpleDateFormat("YYYY/mm/dd", Locale.getDefault());
        SimpleDateFormat output = new SimpleDateFormat("YYYY", Locale.getDefault());

        String year = "";
        try {
            Date date = input.parse(text);
            year = output.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return year;
    }

}
