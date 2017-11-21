package com.oz_heng.apps.android.booklisting;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.List;

import static com.oz_heng.apps.android.booklisting.Utils.Helper.showToast;
import static com.oz_heng.apps.android.booklisting.Utils.Query.isNetworkConnected;

public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<List<Book>> {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    // Base URL for querying Google Book API
    private static final String GOOGLE_BOOK_API_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?";

    // Constant value for the book loader ID
    private static final int BOOK_LOADER_ID = 1;


    // Text entered by the user.
    private String mUserText;
    // Keywords entered by the user.
    private String mUserKeywords;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText editText = findViewById(R.id.user_entered_text);

        // Set the search button to trigger the appropriate action.
        ImageButton searchButton = (ImageButton) findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUserText = editText.getText().toString().trim();

                // If the entered text is empty, alert the user with a Toast message and do nothing.
                if (mUserText.isEmpty()) {
                    showToast(MainActivity.this, getString(R.string.please_enter));
                    return;
                }

                hideSoftKeyboard();

                // Split the entered text into keywords.
                String[] keywords  =  mUserText.split("\\s+");
//                StringBuilder stringBuilder = new StringBuilder();
//                stringBuilder.append(keywords[0]);
                mUserKeywords = keywords[0];
                for (int i=1; i < keywords.length; i++) {
//                    stringBuilder.append("+");
//                    stringBuilder.append(keywords[i]);
                    mUserKeywords = mUserKeywords.concat("+" + keywords[i]);
                }
//                mUserKeywords = stringBuilder.toString();
                Log.v(LOG_TAG, "mUserKeywords: " + mUserKeywords);

                // If there's network connection, fetch the data.
                if (isNetworkConnected(MainActivity.this)) {
                    LoaderManager loaderManager = getLoaderManager();
                    loaderManager.initLoader(BOOK_LOADER_ID, null, MainActivity.this);
                } else {
                    showToast(MainActivity.this, getString(R.string.no_internet));
                }
            }
        });

    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {

         Uri baseUri = Uri.parse(GOOGLE_BOOK_API_REQUEST_URL);
         Uri.Builder uriBuilder = baseUri.buildUpon();
         uriBuilder.appendQueryParameter("q", mUserKeywords);

         Log.v(LOG_TAG, "onCreateLoader - uriBuilder.toString(): " + uriBuilder.toString());

         // Create a new loader for the given URL
        return new BookLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {
        if (books != null && !books.isEmpty()) {
            showToast(this, "onLoadFinished() - Json response has been fetched. See Logcat.");
            for (int i = 0; i < books.size(); i++) {
                Log.v(LOG_TAG, "onLoadFinished - Books(" + i + "): " +
                        books.get(i).toString());
            }
        } else {
            showToast(this, "No book data found");
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {

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
