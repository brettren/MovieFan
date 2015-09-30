package com.brettren.moviefan;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieAdapter extends ArrayAdapter<Movie> {

    Context context;
    int layoutResourceId;
    List<Movie> movies = null;

    public MovieAdapter(Context context, int layoutResourceId, List<Movie> movies) {
        super(context, layoutResourceId, movies);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.movies = movies;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        MovieHolder holder = null;

        if(view == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);

            holder = new MovieHolder();
            holder.txtTitle = (TextView)view.findViewById(R.id.txtTitle);
            holder.txtReleaseDate = (TextView)view.findViewById(R.id.txtReleaseDate);
            holder.imgPoster = (ImageView)view.findViewById(R.id.imagePoster);
            holder.imgBackDrop = (ImageView)view.findViewById(R.id.imageBackDrop);

            view.setTag(holder);
        }
        else {
            holder = (MovieHolder)view.getTag();
        }

        Movie movie = movies.get(position);
        holder.txtTitle.setText(movie.title);
        holder.txtReleaseDate.setText(movie.release_date);
        Picasso.with(context).load(movie.poster_path).into(holder.imgPoster);
        Picasso.with(context).load(movie.backdrop_path).into(holder.imgBackDrop);
        return view;
    }

    public static class MovieHolder {
        ImageView imgPoster;
        ImageView imgBackDrop;
        TextView txtTitle;
        TextView txtReleaseDate;
    }
}