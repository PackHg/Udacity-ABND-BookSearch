package com.oz_heng.apps.android.booklisting.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.oz_heng.apps.android.booklisting.Book;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods for getting data the Google Books API.
 */
public final class Query {
    private static final String LOG_TAG = Query.class.getSimpleName();

    private Query() {
    }

    /**
     * Query Google Book API and return a List of Book objects.
     *
     * @param urlString String containing the URL.
     * @return List of Book data.
     */
    public static List<Book> fetchBookData(String urlString) {

        Log.v(LOG_TAG, "PH: fetchBookData(), urlString: " + urlString);

        // Create URL object
        URL url = createUrl(urlString);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
            Log.v(LOG_TAG, "jsonResponse :" + jsonResponse);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        //  Return a list of Books from the JSON response
        return extractBooksfromJsonString(jsonResponse);
    }

    /**
     * Create an URL from the the given URL string.
     *
     * @param urlString String containing the URL.
     * @return an URL.
     */
    private static URL createUrl(String urlString) {
        URL url = null;

        try {
            url = new URL(urlString);
        }
        catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating url", e);
        }

        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     *
     * @param url an URL.
     * @return String containing the JSON response.
     * @throws IOException when making a Http connection.
     */
    private static String makeHttpRequest(URL url) throws IOException {

        Log.v(LOG_TAG, "PH: makeHttpRequest().");

        String jsonResponse = "";

        // if the url argument is null then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;

        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(2000 /* milliseconds */);      // Instead of 1000 milliseconds
            httpURLConnection.setConnectTimeout(3000 /* milliseconds */);   // Instead of 1500 milliseconds
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            // If the request is successful (response code HttpURLConnection.HTTP_OK)
            // then read the input stream and parse the response
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
                jsonResponse = readFromInputStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + httpURLConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem in retrieving the JSON result from the url.", e);
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }

        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a {@link List} of {@link Book} objects by parsing a JSON String.
     * @param jsonString String to be parsed.
     */
    private static ArrayList<Book> extractBooksfromJsonString(String jsonString) {

        Log.v(LOG_TAG, "PH: extractBooksfromJsonString().");

        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }

        // Keys for parsing
        final String KIND = "kind";
        final String ITEMS = "items";
        final String VOLUME_INFO = "volumeInfo";
        final String TITLE = "title";
        final String AUTHORS = "authors";
        final String PUBLISHED_DATE = "publishedDate";
        final String DESCRIPTION = "description";
        final String IMAGE_LINKS = "imageLinks";
        final String THUMBNAIL = "thumbnail";
        final String CANONICAL_VOULUME_LINK = "canonicalVolumeLink";

        ArrayList<Book> bookArrayList = new ArrayList<Book>();

        try {
            JSONObject base = new JSONObject(jsonString);

            String title = "";
            String authors = "";
            String publishedDate = "";
            String bookUrl = "";
            Bitmap thumbnailImage = null;
            String description = "";

            JSONArray items = base.getJSONArray(ITEMS);
            for (int i = 0; i < items.length(); i++) {
                try {
                    JSONObject volumeInfo = items.getJSONObject(i).getJSONObject(VOLUME_INFO);

                    // Parse title and authors.
                    title = volumeInfo.getString(TITLE);
                    JSONArray authorArray = volumeInfo.getJSONArray(AUTHORS);
                    authors = "";
                    if (authorArray.length() > 0) {
                        authors = authors.concat(authorArray.getString(0));
                        Log.v(LOG_TAG, "authorArray.getString(0): " +
                                authorArray.getString(0));
                        for (int j = 1; j < authorArray.length(); j++) {
                            Log.v(LOG_TAG, "authorArray.getString(" + j + "): "
                                    + authorArray.getString(j));
                            authors = authors.concat(", " + authorArray.getString(j));
                        }
                        Log.v(LOG_TAG, "authors: " + authors);
                    }

                    publishedDate = volumeInfo.getString(PUBLISHED_DATE);

                    // Get the thumbnail image.
                    JSONObject imageLinks = volumeInfo.getJSONObject(IMAGE_LINKS);
                    String thumbnailUrl = imageLinks.getString(THUMBNAIL);
                    thumbnailImage = null;
                    if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                        thumbnailImage = getBitmapFromURL(thumbnailUrl);
                    }

                    bookUrl = volumeInfo.getString(CANONICAL_VOULUME_LINK);
                    description = volumeInfo.getString(DESCRIPTION);

                }
                catch (JSONException e) {
                    if (e.getMessage().contains("No value for description")) {
                        description = "";
                    } else {
                        Log.e(LOG_TAG, "Problem in parsing the JSON string", e);
                    }
                }

                if (title.isEmpty()) {
                    // If title is empty go to next loop interation.
                    continue;
                }

                bookArrayList.add(new Book(title, authors, publishedDate, description, bookUrl,
                        thumbnailImage));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem in parsing the JSON string", e);
        }

        return bookArrayList;
    }

    /**
     * Check the network connectivity.
     *
     * @param context the context of the activity
     * @return true if there's a network connection
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Load an image from a given URL and turn it into a Bitmap.
     * Return null if there's issue in getting the {@link Bitmap} or the src is empty.
     *
     * @param srcUrl
     * @return
     */
    public static Bitmap getBitmapFromURL(String srcUrl) {

        if (srcUrl.isEmpty()) {
            return null;
        }

        HttpURLConnection connection = null;
        InputStream input = null;

        try {
            URL url = new URL(srcUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();
            // Use Android’s BitmapFactory class to decode the input stream
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem in getting a Bitmap from the given URL string.", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Problem in closing the inputStream.", e);
                }
            }
        }
    }

}
