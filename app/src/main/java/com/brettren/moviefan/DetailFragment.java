package com.brettren.moviefan;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brettren.moviefan.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailFragment extends Fragment{

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private String movieId;
    private Movie mMovie;

    ImageView imgPoster;
    ImageView imgBackDrop;
    TextView txtTitle;
    TextView txtReleaseDate;
    TextView txtRuntime;
    TextView txtVote;
    TextView txtRevenue;
    TextView txtOverview;
    TextView txtTagline;
    Button btnWantToSee;
    Button btnDelete;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            movieId = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
        txtTitle = (TextView)view.findViewById(R.id.txtTitle);
        txtReleaseDate = (TextView)view.findViewById(R.id.txtReleaseDate);
        imgPoster = (ImageView)view.findViewById(R.id.imagePoster);
        imgBackDrop = (ImageView)view.findViewById(R.id.imageBackDrop);

        txtRuntime = (TextView)view.findViewById(R.id.txtRuntime);
        txtVote = (TextView)view.findViewById(R.id.txtVote);
        txtRevenue = (TextView)view.findViewById(R.id.txtRevenue);
        txtOverview = (TextView)view.findViewById(R.id.txtOverview);
        txtTagline = (TextView)view.findViewById(R.id.txtTagline);
        btnWantToSee = (Button)view.findViewById(R.id.btnWantToSee);
        btnDelete = (Button)view.findViewById(R.id.btnDelete);

        btnWantToSee.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Cursor movieCursor = getActivity().getContentResolver().query(
                        MovieContract.MovieEntry.CONTENT_URI,
                        new String[]{MovieContract.MovieEntry._ID},
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{movieId},
                        null);

                // if the movie already exists in database, nothing to do
                if (movieCursor != null && movieCursor.moveToFirst()) {
                    Toast.makeText(getActivity(), mMovie.title + " already exists in list", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    ContentValues movieValue = new ContentValues();

                    movieValue.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mMovie.id);
                    movieValue.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, mMovie.title);
                    movieValue.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, mMovie.release_date);
                    movieValue.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, mMovie.poster_path);
                    movieValue.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, mMovie.backdrop_path);

                    // Finally, insert movie data into the database.
                    getActivity().getContentResolver().insert(
                            MovieContract.MovieEntry.CONTENT_URI,
                            movieValue
                    );
                }

                Toast.makeText(getActivity(), mMovie.title + " has been added to list", Toast.LENGTH_SHORT).show();

                movieCursor.close();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Cursor movieCursor = getActivity().getContentResolver().query(
                        MovieContract.MovieEntry.CONTENT_URI,
                        new String[]{MovieContract.MovieEntry._ID},
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{movieId},
                        null);

                // if the movie not exists in database, nothing to do
                if (movieCursor == null || !movieCursor.moveToFirst()) {
                    Toast.makeText(getActivity(), mMovie.title + " not exists in list", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    // Finally, delete movie data from the database.
                    getActivity().getContentResolver().delete(
                            MovieContract.MovieEntry.CONTENT_URI,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{movieId}
                    );
                }

                Toast.makeText(getActivity(), mMovie.title + " has been deleted from list", Toast.LENGTH_SHORT).show();

                movieCursor.close();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }

    private void updateMovie() {
        FetchDetailTask movieTask = new FetchDetailTask();
        movieTask.execute(movieId);
    }

    public class FetchDetailTask extends AsyncTask<String, Void, Movie> {

        private final String LOG_TAG = FetchDetailTask.class.getSimpleName();

        @Override
        protected Movie doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=xxx

            Uri builtUri = Uri.parse(Utility.BASE_URL + movieId + "?").buildUpon()
                    .appendQueryParameter(Utility.API_KEY_PARAM, Utility.api_key)
                    .build();

            String movieJsonStr = Utility.getJson(builtUri);

            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private Movie getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            if(movieJsonStr == null) return new Movie();

            // These are the names of the JSON objects that need to be extracted.

            final String ID = "id";
            final String BACKDROP_PATH = "backdrop_path";
            final String POSTER_PATH = "poster_path";
            final String TITLE = "title";
            final String OVERVIEW = "overview";
            final String RELEASE_DATE = "release_date";
            final String VOTE_AVERAGE = "vote_average";
            final String VOTE_COUNT = "vote_count";
            final String TAG_LINE = "tagline";
            final String RUNTIME = "runtime";
            final String REVENUE = "revenue";

            JSONObject movieObject = new JSONObject(movieJsonStr);

            String id;
            String backdrop_path;
            String poster_path;
            String overview;
            String title;
            String release_date;
            String vote_average;
            String vote_count;

            String tagline;
            String runtime;
            String revenue;


            final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p";
            final String imgSize = "/w500";
            id = movieObject.getString(ID);
            backdrop_path = IMAGE_BASE_URL + imgSize+ movieObject.getString(BACKDROP_PATH);
            poster_path = IMAGE_BASE_URL + imgSize + movieObject.getString(POSTER_PATH);
            title = movieObject.getString(TITLE);
            release_date = movieObject.getString(RELEASE_DATE);
            vote_average = movieObject.getString(VOTE_AVERAGE);
            vote_count = movieObject.getString(VOTE_COUNT);

            overview = movieObject.getString(OVERVIEW);
            tagline = movieObject.getString(TAG_LINE);
            runtime = movieObject.getString(RUNTIME);
            revenue = movieObject.getString(REVENUE);


            Movie movie = new Movie(id, title, release_date, backdrop_path, poster_path, vote_average,
                    vote_count, overview, tagline, runtime, revenue);

            mMovie = movie;

            return movie;

        }


        @Override
        protected void onPostExecute(Movie movie) {
            if (movie == null) return;
            txtTitle.setText(movie.title);
            txtReleaseDate.setText(movie.release_date);
            String runtime = movie.runtime + "min";
            txtRuntime.setText(runtime);
            String vote = movie.vote_average + "/" + "10  (" + movie.vote_count + ")";
            txtVote.setText(vote);
            txtRevenue.setText(getRevenue(movie.revenue));
            txtOverview.setText(movie.overview);
            String tagline = "\"" + movie.tagline + "\"";
            txtTagline.setText(tagline);

            Picasso.with(getActivity()).load(movie.poster_path).into(imgPoster);
            Picasso.with(getActivity()).load(movie.backdrop_path).into(imgBackDrop);

        }

        private String getRevenue(String revenue){
            if(revenue == null) return null;
            StringBuffer sb = new StringBuffer(revenue);
            for(int i = revenue.length()-4; i >= 0; i-=3){
                sb.insert(i+1, ",");
            }
            return sb.toString() + " $";
        }
    }

}