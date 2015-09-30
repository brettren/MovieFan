package com.brettren.moviefan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private MovieAdapter mMovieAdapter;
    private SearchView mSearchView;
    private String mQuery = "";

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mainfragment, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // Configure the search info and add event listeners
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null || newText.length() == 0) {
                    return false;
                }
                mQuery = newText;
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() != 0) {
                    if (!mQuery.equals(query)) {
                        mQuery = query;
                    }
                    updateMovie();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovie();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mMovieAdapter =
                new MovieAdapter(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_movie, // The ID of the textview to populate.
                        new ArrayList<Movie>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_movie);
        listView.setAdapter(mMovieAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = mMovieAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, movie.id);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void updateMovie() {
        FetchMovieTask movieTask = new FetchMovieTask();
        movieTask.execute(mQuery);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }

    public class FetchMovieTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        @Override
        protected Movie[] doInBackground(String... params) {

            if(params.length == 0) {
                return null;
            }

            Log.e(LOG_TAG, params[0]);

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            final String API_KEY_PARAM = "api_key";
            final String api_key = "xxx";

            Uri builtUri;

            if (params[0].length() == 0) {
                // get the sort_by value through preference
                SharedPreferences sharedPrefs =
                        PreferenceManager.getDefaultSharedPreferences(getActivity());
                String sortBy = sharedPrefs.getString(
                        getString(R.string.pref_discover_key),
                        getString(R.string.pref_popular_value));


                final String SORT_BY_PARAM = "sort_by";

                final String order;
                if(sortBy.equals(getString(R.string.pref_popular_value))){
                    order = "popularity.desc";
                }
                else{
                    order = "revenue.desc";
                }

                final String DISCOVER_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";

                // http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=xxx

                builtUri = Uri.parse(DISCOVER_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, order)
                        .appendQueryParameter(API_KEY_PARAM, api_key)
                        .build();
            }
            else{
                final String SEARCH_BASE_URL =
                        "http://api.themoviedb.org/3/search/movie?";

                final String QUERY_PARAM = "query";

                final String title = params[0];

                // http://api.themoviedb.org/3/search/movie?query=title&api_key=xxx

                builtUri = Uri.parse(SEARCH_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, title)
                        .appendQueryParameter(API_KEY_PARAM, api_key)
                        .build();
            }

            String movieJsonStr = getJson(builtUri);

            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
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

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private Movie[] getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.

            final String RESULT = "results";
            final String ID = "id";
            final String BACKDROP_PATH = "backdrop_path";
            final String POSTER_PATH = "poster_path";
            final String TITLE = "title";
            final String RELEASE_DATE = "release_date";
            final String VOTE_AVERAGE = "vote_average";
            final String VOTE_COUNT = "vote_count";


            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(RESULT);

            Movie[] movies = new Movie[movieArray.length()];


            for(int i = 0; i < movieArray.length(); i++) {

                String id;
                String backdrop_path;
                String poster_path;
                String title;
                String release_date;
                String vote_average;
                String vote_count;

                // Get the JSON object representing the movie
                JSONObject movieObject = movieArray.getJSONObject(i);

                final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p";
                final String imgSize = "/w500";
                id = movieObject.getString(ID);
                backdrop_path = IMAGE_BASE_URL + imgSize+ movieObject.getString(BACKDROP_PATH);
                poster_path = IMAGE_BASE_URL + imgSize + movieObject.getString(POSTER_PATH);
                title = movieObject.getString(TITLE);
                release_date = movieObject.getString(RELEASE_DATE);
                vote_average = movieObject.getString(VOTE_AVERAGE);
                vote_count = movieObject.getString(VOTE_COUNT);

                movies[i] = new Movie(id, title, release_date, backdrop_path, poster_path);
            }
            return movies;

        }

        @Override
        protected void onPostExecute(Movie[] result) {
            if (result != null) {
                mMovieAdapter.clear();
                for(Movie movie : result) {
                    mMovieAdapter.add(movie);
                }
                // New data is back from the server.  Hooray!
            }
        }
    }

}
