package com.oz_heng.apps.android.booklisting;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.oz_heng.apps.android.booklisting.Utils.Helper.showToast;
import static com.oz_heng.apps.android.booklisting.Utils.Query.isNetworkConnected;

// Done: handle case "JSONException: No value for authors".
// Done: No progress bar upon search following the 1st search.
// TODO: After returning from the cliecked book's webpage, the search is restarting again. To fix?

public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<List<Book>> {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /** Base URL for querying Google Book API */
    private static final String GOOGLE_BOOK_API_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?";

    /** Constant value for the book loader ID */
    private static final int BOOK_LOADER_ID = 1;

    /** Key for saving mUserQueryText String variable with the instance state */
    private static final String KEY_USER_QUERY_TEXT = "user query text";
    /** Key for saving mIsFirstSearch boolean variable with the instance state */
    private static final String KEY_IS_FIST_SEARCH = "is it first search";

    /** Query text entered by the user */
    private String mUserQueryText = "";
    /** Keywords entered by the user */
    private String mUserKeywords;
    /** Is it the first search? */
    private boolean mIsFirstSearch = true;


    // Text view that is displayed when the listView is empty.
    private TextView mEmptyView;
    // Loading idicator.
    private View mProgressBar;

    private BookAdapter mBookAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v(LOG_TAG, "PH: onCreate()");

        if (savedInstanceState != null) {
            mUserQueryText = savedInstanceState.getString(KEY_USER_QUERY_TEXT, mUserQueryText);
//            mIsFirstSearch = savedInstanceState.getBoolean(KEY_IS_FIST_SEARCH, mIsFirstSearch);
        }

        mIsFirstSearch = true;

        // Set the search button to trigger the appropriate action.
        ImageButton searchButton = findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });

        // Setup listView with a BookAdapter.
        ListView listView = (ListView) findViewById(R.id.list_view);
        mBookAdapter = new BookAdapter(this, new ArrayList<Book>());
        listView.setAdapter(mBookAdapter);

        // Set listView with an empty view.
        mEmptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(mEmptyView);
        mEmptyView.setVisibility(View.GONE);

        mProgressBar = findViewById(R.id.loading_spinner);
        mProgressBar.setVisibility(View.GONE);

        // Set a click listener to open the webpage url to see additional details on the
        // book the user clicks on.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        if (!mUserQueryText.isEmpty()) {
            // If there's network connection, fetch the data.
            if (isNetworkConnected(MainActivity.this)) {
                LoaderManager loaderManager = getLoaderManager();

                loaderManager.initLoader(BOOK_LOADER_ID, null, this);
                Log.v(LOG_TAG, "PH: loaderManager.initLoader() has been called");
            } else {
                mProgressBar.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setText(R.string.no_internet_connection);
            }
        }

        hideSoftKeyboard();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_USER_QUERY_TEXT, mUserQueryText);
//        outState.putBoolean(KEY_IS_FIST_SEARCH, mIsFirstSearch);
        super.onSaveInstanceState(outState);
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
        String[] keywords  =  mUserQueryText.split("\\s+");
        mUserKeywords = keywords[0];
        for (int i=1; i < keywords.length; i++) {
            mUserKeywords = mUserKeywords.concat("+" + keywords[i]);
        }
        Log.v(LOG_TAG, "mUserKeywords: " + mUserKeywords);

//        // Clear previous book data in ListView.
//        mBookAdapter.clear();

        // If there's network connection, fetch the data.
        if (isNetworkConnected(MainActivity.this)) {
            LoaderManager loaderManager = getLoaderManager();

            if (mIsFirstSearch) {
                loaderManager.initLoader(BOOK_LOADER_ID, null, this);
                Log.v(LOG_TAG, "PH: loaderManager.initLoader() has been called");
                mIsFirstSearch = false;
            } else {
                // Restart the loader.
                loaderManager.restartLoader(BOOK_LOADER_ID, null,this);
                Log.v(LOG_TAG, "PH: loaderManager.restartLoader() has been called.");
            }
        } else {
            mProgressBar.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mEmptyView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {

         Uri baseUri = Uri.parse(GOOGLE_BOOK_API_REQUEST_URL);
         Uri.Builder uriBuilder = baseUri.buildUpon();
         uriBuilder.appendQueryParameter("q", mUserKeywords);

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
