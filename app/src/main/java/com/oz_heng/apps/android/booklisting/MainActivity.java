package com.oz_heng.apps.android.booklisting;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    // Text entered by the user.
    private String userText;
    // Array of keywords entered by the user.
    private String[] userKeywords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideSoftKeyboard();

        final EditText editText = findViewById(R.id.user_entered_text);

        // Set the search button to trigger the appropriate action.
        ImageButton searchButton = (ImageButton) findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userText = editText.getText().toString().trim();

                // If the entered text is empty, alert the user with a Toast message and do nothing.
                if (userText.isEmpty()) {
                    showToast(MainActivity.this, getString(R.string.please_enter));
                    return;
                }

                // Spit the text into an array of keywords.
                userKeywords =  userText.split("\\s+");
                for (int i=0; i < userKeywords.length; i++) {
                    Log.v(LOG_TAG, "userKeywords[" + i + "]: " + userKeywords[i]);
                }
            }
        });

    }

//    private String[] getUserKeywords() {
//
//    }

    /**
     * Show a {@link Toast} with the text parameter.
     * @param context The activity context.
     * @param text Text to be displayed.
     */
    void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Hide the Android soft keyboard.
     */
    void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null && imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
