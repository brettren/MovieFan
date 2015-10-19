package com.brettren.moviefan;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class WantAdapter extends CursorAdapter {

    Context context;

    public WantAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) { //create a new view to hold the data pointed to by cursor
        // Choose the layout type
        int layoutId = R.layout.list_item_movie;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false); // 先获得LayoutInflater再inflate

        MovieHolder holder = new MovieHolder();
        holder.txtTitle = (TextView)view.findViewById(R.id.txtTitle);
        holder.txtReleaseDate = (TextView)view.findViewById(R.id.txtReleaseDate);
        holder.imgPoster = (ImageView)view.findViewById(R.id.imagePoster);
        holder.imgBackDrop = (ImageView)view.findViewById(R.id.imageBackDrop);

        view.setTag(holder);  // holder is stored as tag within the view

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) { // bind cursor to view, the cursor is swapped in by fragment

        MovieHolder holder = (MovieHolder) view.getTag();

        // Read date from cursor
        String title = cursor.getString(WantFragment.COL_MOVIE_TITLE);
        holder.txtTitle.setText(title);

        String release_time = cursor.getString(WantFragment.COL_RELEASE_DATE);
        holder.txtReleaseDate.setText(release_time);

        String poster_path = cursor.getString(WantFragment.COL_POSTER_PATH);
        Picasso.with(context).load(poster_path).into(holder.imgPoster);

        String backdrop_path = cursor.getString(WantFragment.COL_BACKDROP_PATH);
        Picasso.with(context).load(backdrop_path).into(holder.imgBackDrop);
    }

    // cache the call to findViewById()
    public static class MovieHolder {
        ImageView imgPoster;
        ImageView imgBackDrop;
        TextView txtTitle;
        TextView txtReleaseDate;
    }
}
