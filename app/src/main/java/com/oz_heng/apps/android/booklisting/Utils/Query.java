package com.oz_heng.apps.android.booklisting.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Helper methods for getting data the Google Books API.
 */
public final class Query {
    private static final String LOG_TAG = Query.class.getSimpleName();

    private Query() {
    }

    /**
     * Query the USGS dataset and return a List of Book objects.
     *
     * @param urlString
     * @return
     */
    public static String fetchBookData(String urlString) {
//    public static List<Earthquake> fetchEarthquakeData(String urlString) {

        // Fot testing loading indicator
//        try {
//            Thread.sleep(2000 /** milliseconds */);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        // Create URL object
        URL url = createUrl(urlString);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        //  Return the list of {@link Earthquake}s extracted from the JSON response
//        return extractEarthquakesFromJson(jsonResponse);
        return jsonResponse;
    }

    /**
     * Create an URL from the the given URL string.
     *
     * @param urlString
     * @return
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
     * @param url
     * @return String
     * @throws IOException
     */
    private static String makeHttpRequest(URL url) throws IOException {
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

//    /**
//     * Return an {@link List} of {@link Earthquake} objects that has been built up from
//     * parsing a JSON response.
//     */
//    private static List<Earthquake> extractEarthquakesFromJson(String earthquakesJson) {
//        // If the JSON string is empty or null, then return early.
//        if (earthquakesJson.isEmpty()) {
//            return null;
//        }
//
//        // The items that need to be parsed out of the JSON response
//        final String FEATURES = "features";
//        final String PROPERTIES ="properties";
//        final String MAG = "mag";
//        final String PLACE = "place";
//        final String TIME = "time";
//        final String URL = "url";
//
//        // Create an empty ArrayList that we can start adding earthquakes to
//        List<Earthquake> earthquakes = new ArrayList<>();
//
//        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
//        // is formatted, a JSONException exception object will be thrown.
//        // Catch the exception so the app doesn't crash, and print the error message to the logs.
//        try {
//
//            // Crate a JSONObject from the earthquakesJso parameter
//            JSONObject baseJsonResponse = new JSONObject(earthquakesJson);
//
//            // Extract the JSONArray associated with the key called "features"
//            // which represents a list of features (or eathquakes
//            JSONArray features = baseJsonResponse.getJSONArray(FEATURES);
//
//            // For each item of the "features" array. reach the JSONObject "properties" and extract
//            // the magnitude, place and date data, and add them into the earthquake list.
//            for (int i = 0; i < features.length(); i++) {
//                JSONObject properties = features.getJSONObject(i).getJSONObject(PROPERTIES);
//
//                double magnitude = properties.getDouble(MAG);
//                String place = properties.getString(PLACE);
//                Date date = new Date(properties.getLong(TIME));
//                String url = properties.getString(URL);
//
//                earthquakes.add( new Earthquake(magnitude, place, date, url) );
//            }
//
//
//        } catch (JSONException e) {
//            // If an error is thrown when executing any of the above statements in the "try" block,
//            // catch the exception here, so the app doesn't crash. Print a log message
//            // with the message from the exception.
//            Log.e(LOG_TAG, "Problem parsing the earthquake JSON results.", e);
//        }
//
//        // Return the list of earthquakes
//        return earthquakes;
//    }

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
}
