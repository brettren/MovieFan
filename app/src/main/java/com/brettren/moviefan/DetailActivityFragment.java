package com.brettren.moviefan;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DetailActivityFragment extends Fragment{

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private String movieId;

    private VideoAdapter mVideoAdapter;

    ImageView imgPoster;
    ImageView imgBackDrop;
    TextView txtTitle;
    TextView txtReleaseDate;
    TextView txtRuntime;
    TextView txtVote;
    TextView txtRevenue;
    TextView txtOverview;
    TextView txtTagline;

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

        mVideoAdapter =
                new VideoAdapter(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_video, // The ID of the textview to populate.
                        new ArrayList<Video>());

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) view.findViewById(R.id.listview_video);
        listView.setAdapter(mVideoAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Video video = mVideoAdapter.getItem(position);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(video.url));
                startActivity(intent);
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

            final String API_KEY_PARAM = "api_key";
            final String api_key = "xxx";

            final String BASE_URL =
                    "https://api.themoviedb.org/3/movie/";

            Uri builtUri = Uri.parse(BASE_URL + movieId + "?").buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, api_key)
                    .build();

            String movieJsonStr = getJson(builtUri);


            builtUri = Uri.parse(BASE_URL + movieId + "/videos?").buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, api_key)
                    .build();

            String videoJsonStr = getJson(builtUri);

            try {
                return getMovieDataFromJson(movieJsonStr, videoJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private String getJson(Uri builtUri){
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String JsonStr = null;

            try {

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // handle 404 not found
                if(urlConnection.getResponseCode() >= 400) return null;

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                JsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return JsonStr;
        }

        private Movie getMovieDataFromJson(String movieJsonStr, String videoJsonStr)
                throws JSONException {

            if(movieJsonStr == null || videoJsonStr == null) return new Movie();

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

            // parse videos

            JSONObject videoJson = new JSONObject(videoJsonStr);

            final String RESULT = "results";
            final String KEY = "key";
            final String NAME = "name";

            final String VIDEO_BASE_URL = "https://www.youtube.com/watch?v=";

            JSONArray videoArray = videoJson.getJSONArray(RESULT);

            List<Video> videos = new ArrayList<>();
            for(int i = 0; i < videoArray.length(); i++){
                JSONObject videoObject = videoArray.getJSONObject(i);
                String url = VIDEO_BASE_URL + videoObject.getString(KEY);
                String name = videoObject.getString(NAME);
                videos.add(new Video(url, name));
            }

            Movie movie = new Movie(id, title, release_date, backdrop_path, poster_path, vote_average,
                    vote_count, overview, tagline, runtime, revenue, videos);

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

            mVideoAdapter.clear();
            for(Video video : movie.videos) {
                mVideoAdapter.add(video);
            }
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