package com.oz_heng.apps.android.booklisting;

import android.graphics.Bitmap;

/**
 * Book data.
 */
public class Book {
    private String mTitle;
    private String mAuthors;
    private String mPublishedDate;
    private String mDescription;
    private String mUrl;
    private Bitmap mThumbnail;

    public Book(String title, String authors, String date, String description,
                String url, Bitmap thumbnailImage) {
        mTitle = title;
        mAuthors = authors;
        mPublishedDate = date;
        mDescription = description;
        mUrl = url;
        mThumbnail = thumbnailImage;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthors() {
        return mAuthors;
    }

    public String getPublishedDate() {
        return mPublishedDate;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getUrl() {
        return mUrl;
    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }

    @Override
    public String toString() {
        final String NL = "\n";

        return getTitle() + NL + getAuthors() + NL + getPublishedDate()
                + NL + getDescription() + NL + getUrl();
    }
}
