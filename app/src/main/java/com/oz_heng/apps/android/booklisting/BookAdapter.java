package com.oz_heng.apps.android.booklisting;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.oz_heng.apps.android.booklisting.Utils.Helper.getYearfrom;


/**
 * {@link BookAdapter} provides the layout for each list item based on a data
 * source, which is an {@link ArrayList<Book>}.
 */
public class BookAdapter extends ArrayAdapter<Book> {
    public BookAdapter(@NonNull Context context, @NonNull ArrayList<Book> books) {
        super(context, 0, books);
    }

    static class ViewHolder {
        ImageView thumbnail;
        TextView title;
        TextView authors;
        TextView publishedIn;
        TextView description;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder;

        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_book_item, parent,
                    false);
            holder = new ViewHolder();

            holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.authors = (TextView) convertView.findViewById(R.id.authors);
            holder.publishedIn = (TextView) convertView.findViewById(R.id.published_in);
            holder.description = (TextView) convertView.findViewById(R.id.description);
            convertView.setTag(holder);
        }

        Book book = getItem(position);

        if (book != null) {
            if (book.getThumbnail() != null) {
                holder.thumbnail.setImageBitmap(book.getThumbnail());
            }
            holder.title.setText(book.getTitle());
            if (!book.getAuthors().isEmpty()) {
                holder.authors.setText(book.getAuthors());
            } else {
                holder.authors.setText(R.string.author_unknown);
            }
            if (!book.getAuthors().isEmpty()) {
                holder.publishedIn.setText(getYearfrom(book.getPublishedDate()));
            } else {
                holder.publishedIn.setText(R.string.published_date_unknown);
            }
            holder.description.setText(book.getDescription());
        }

        return convertView;
    }
}
