package com.oz_heng.apps.android.booklisting;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.oz_heng.apps.android.booklisting.Utils.Helper.getKeywordsfrom;
import static com.oz_heng.apps.android.booklisting.Utils.Helper.showToast;
import static com.oz_heng.apps.android.booklisting.Utils.Query.isNetworkConnected;

// Done: handle case "JSONException: No value for authors".
// Done: No progress bar upon search following the 1st search.
// Normal: After returning from the cliecked book's webpage, the search is restarting again. To fix?
// TODO: Increase timeout with http connection?

public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<List<Book>> {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /** Base URL for querying Google Book API */
    private static final String GOOGLE_BOOK_API_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?";

    /** Constant value for the book loader ID */
    private static final int BOOK_LOADER_ID = 1;

    /** Tag used to save user data in SharedPreferences */
    private static final String USER_DATA = "com.oz_heng.apps.android.booklisting.user_data";
    /** Key for saving mUserQueryText String variable */
    private static final String KEY_USER_QUERY_TEXT = "user query text";

    /** Query text entered by the user */
    private String mUserQueryText = "";
    /** Keywords entered by the user */
    private String mUserKeywords;
//    /** Is it the first search? */
//    private boolean mIsFirstSearch = true;

    /** ListView of books */
    ListView mListView;

    /** Text view that is displayed when the listView is empty */
    private TextView mEmptyView;
    /** Loading indicator */
    private View mProgressBar;

    private BookAdapter mBookAdapter;

    /** For saving the listView state */
    Parcelable mState;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v(LOG_TAG, "PH: onCreate()");

        // Set the search button to trigger the appropriate action.
        ImageButton searchButton = findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });

        // Setup listView with a BookAdapter.
        mListView = findViewById(R.id.list_view);
        mBookAdapter = new BookAdapter(this, new ArrayList<Book>());
        mListView.setAdapter(mBookAdapter);

        // Set listView with an empty view.
        mEmptyView = findViewById(R.id.empty_view);
        mListView.setEmptyView(mEmptyView);
        mEmptyView.setVisibility(View.GONE);

        mProgressBar = findViewById(R.id.loading_spinner);
        mProgressBar.setVisibility(View.GONE);

        // Set a click listener to open the webpage url to see additional details on the
        // book the user clicks on.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Log.v(LOG_TAG, "PH: ListView, onItemClick().");

                Book book = mBookAdapter.getItem(i);
                if (book != null) {
                    Intent intent  = new Intent(Intent.ACTION_VIEW, Uri.parse(book.getUrl()));
                    startActivity(intent);
                }
            }
        });

        // Restore mUserQueryText from SharedPreferences.
        SharedPreferences sp = getSharedPreferences(USER_DATA, 0);
        if (sp != null) {
            mUserQueryText = sp.getString(KEY_USER_QUERY_TEXT, mUserQueryText);
        }

//        mIsFirstSearch = true;

        if (!mUserQueryText.isEmpty()) {
            // Restore text entered by the user.
            EditText userEditText = findViewById(R.id.user_entered_text);
            userEditText.setText(mUserQueryText);
            hideSoftKeyboard();

            mUserKeywords = getKeywordsfrom(mUserQueryText);

            // If there's network connection, fetch the data.
            if (isNetworkConnected(MainActivity.this)) {
                LoaderManager loaderManager = getLoaderManager();

                loaderManager.initLoader(BOOK_LOADER_ID, null, this);
                mProgressBar.setVisibility(View.VISIBLE);
                Log.v(LOG_TAG, "PH: loaderManager.initLoader() has been called");
            } else {
                mProgressBar.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setText(R.string.no_internet_connection);
            }
        }

        // Restore previous mState, including scroll position.
        if (mState != null) {
            Log.v(LOG_TAG, "PH: Restoring mListView state.");
            mListView.onRestoreInstanceState(mState);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save mListView state, including scroll position.
        Log.v(LOG_TAG, "PH: onPause(), saving mListView state.");
        mState = mListView.onSaveInstanceState();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Save mUserQueryText in SharedPreferences.
        SharedPreferences sp = getSharedPreferences(USER_DATA, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_USER_QUERY_TEXT, mUserQueryText);
        editor.apply();
    }

    /**
     * Search based on the text entered by the user.
     */
    private void search() {
        Log.v(LOG_TAG, "PH: search().");

        mEmptyView.setText("");
        mEmptyView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        EditText editText = findViewById(R.id.user_entered_text);

        mUserQueryText = editText.getText().toString().trim();

        // If the entered text is empty, alert the user with a Toast message and do nothing.
        if (mUserQueryText.isEmpty()) {
            showToast(MainActivity.this, getString(R.string.please_enter));
            return;
        }

        hideSoftKeyboard();

        // Split the entered text into keywords.
        mUserKeywords = getKeywordsfrom(mUserQueryText);
        Log.v(LOG_TAG, "mUserKeywords: " + mUserKeywords);

        // If there's network connection, fetch the data.
        if (isNetworkConnected(MainActivity.this)) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.restartLoader(BOOK_LOADER_ID, null,this);

            Log.v(LOG_TAG, "PH: loaderManager.restartLoader() has been called.");
        } else {
            mProgressBar.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mEmptyView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String maxResults = sp.getString(getString(R.string.settings_max_results_key),
                getString(R.string.settings_max_results_default));

        Uri baseUri = Uri.parse(GOOGLE_BOOK_API_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("q", mUserKeywords);
        uriBuilder.appendQueryParameter("maxResults", maxResults);

        Log.v(LOG_TAG, "PH: onCreateLoader - maxResults: " + maxResults);
        Log.v(LOG_TAG, "PH: onCreateLoader - uriBuilder.toString(): " + uriBuilder.toString());

         // Create a new loader for the given URL
        return new BookLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {

        Log.v(LOG_TAG, "PH: onLoadFinished().");

        mProgressBar.setVisibility(View.GONE);

        // Clear the adapter of preview book data.
        mBookAdapter.clear();

        if (books != null && !books.isEmpty()) {
            for (int i = 0; i < books.size(); i++) {
                Log.v(LOG_TAG, "PH: onLoadFinished - Books(" + i + "): " +
                        books.get(i).toString());
            }
            mBookAdapter.addAll(books);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
            mEmptyView.setText(R.string.no_book_data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        Log.v(LOG_TAG, "PH: onLoaderReset()");
        mBookAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
