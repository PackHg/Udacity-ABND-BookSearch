package com.oz_heng.apps.android.booklisting;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

import static com.oz_heng.apps.android.booklisting.Utils.Query.fetchBookData;


/**
 * For loading a list of books by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class BookLoader extends AsyncTaskLoader<List<Book>> {
    private static final String LOG_TAG = BookLoader.class.getSimpleName();

  // Query URL
    private String mUrl;

    /**
     * Constructor.
     * @param context context of the activity
     * @param url to load the data from
     */
    public BookLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    /**
     * Is called in a backaground thread to fetch book data from Google Books API.
     * @return
     */
    @Override
    public List<Book> loadInBackground() {
        if (mUrl == null || mUrl.isEmpty()) {
            return null;
        }
        Log.v(LOG_TAG, "PH: loadInBackground(), mUrl: " + mUrl);

        return fetchBookData(mUrl);
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {

        Log.v(LOG_TAG, "PH: onStartLoading().");

        forceLoad();
    }
}
