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

}
