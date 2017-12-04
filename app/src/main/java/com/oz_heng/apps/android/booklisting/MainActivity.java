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


public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<List<Book>> {

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
    /** Keywords used for search */
    private String mUserKeywords;

    /** {@link ListView} of {@link Book} */
    private ListView mListView;

    /** Text view that is displayed when the listView is empty */
    private TextView mEmptyView;
    /** Loading indicator */
    private View mProgressBar;

    private BookAdapter mBookAdapter;

    /** For saving the listView state */
    private Parcelable mState;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                Book book = mBookAdapter.getItem(i);
                if (book != null) {
                    if (!book.getUrl().isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(book.getUrl()));
                        startActivity(intent);
                    } else {
                        showToast(MainActivity.this, getString(R.string.no_link));
                    }
                }
            }
        });

        // Restore mUserQueryText from SharedPreferences.
        SharedPreferences sp = getSharedPreferences(USER_DATA, 0);
        if (sp != null) {
            mUserQueryText = sp.getString(KEY_USER_QUERY_TEXT, mUserQueryText);
        }

        if (!mUserQueryText.isEmpty()) {
            // Restore text entered by the user.
            EditText userEditText = findViewById(R.id.user_entered_text);
            userEditText.setText(mUserQueryText);
            hideSoftKeyboard();

            mUserKeywords = getKeywordsfrom(mUserQueryText);

            // If there's network connection, fetch book data.
            if (isNetworkConnected(MainActivity.this)) {
                LoaderManager loaderManager = getLoaderManager();

                loaderManager.initLoader(BOOK_LOADER_ID, null, this);
                mProgressBar.setVisibility(View.VISIBLE);
            } else {
                mProgressBar.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setText(R.string.no_internet_connection);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save mListView state, including scroll position.
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

        mBookAdapter.clear();

        // Split the entered text into keywords.
        mUserKeywords = getKeywordsfrom(mUserQueryText);

        // If there's network connection, fetch the data.
        if (isNetworkConnected(MainActivity.this)) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.restartLoader(BOOK_LOADER_ID, null,this);

        } else {
            mProgressBar.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mEmptyView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {

        final String Q = "q";
        final String MAX_RESULTS = "maxResults";

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String maxResults = sp.getString(getString(R.string.settings_max_results_key),
                getString(R.string.settings_max_results_default));

        Uri baseUri = Uri.parse(GOOGLE_BOOK_API_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter(Q, mUserKeywords);
        uriBuilder.appendQueryParameter(MAX_RESULTS, maxResults);

        // Create a new loader for the given URL
        return new BookLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {

        mProgressBar.setVisibility(View.GONE);

        // Clear the adapter of preview book data.
        mBookAdapter.clear();

        if (books != null && !books.isEmpty()) {
            mBookAdapter.addAll(books);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
            mEmptyView.setText(R.string.no_book_data);
        }

        // Restore previous mState, including scroll position.
        if (mState != null) {
            mListView.onRestoreInstanceState(mState);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
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
    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null && imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
