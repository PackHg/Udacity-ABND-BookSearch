package com.oz_heng.apps.android.booklisting;

import android.graphics.Bitmap;

/**
 * Book data.
 */
public class Book {
    private String mTitle;
    private String mAuthors;
    private String mPublishedDate;
    private String mUrl;
    private Bitmap mThumbnailImage;

    public Book(String title, String authors, String date, String url, Bitmap thumbnailImage) {
        mTitle = title;
        mAuthors = authors;
        mPublishedDate = date;
        mUrl = url;
        mThumbnailImage = thumbnailImage;
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

    public String getUrl() {
        return mUrl;
    }

    public Bitmap getThumbnailImage() {
        return mThumbnailImage;
    }

    @Override
    public String toString() {
        final String COMMA = ", ";

        return getTitle() + COMMA + getAuthors() + COMMA + getPublishedDate()
                + COMMA + getUrl();
    }
}
