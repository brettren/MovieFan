package com.brettren.moviefan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

    private MovieAdapter mMovieAdapter;
    private SearchView mSearchView;
    private ListView mListView;
    private String mQuery = "";
    private int page = 1;
    private List<Movie> mMovies;

    public MainFragment() {
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
                if (newText == null) {
                    return false;
                }
                mQuery = newText;
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() != 0) {
                    if (!mQuery.equals(query)) {
                        mQuery = query;
                    }
                    page = 1;
                    updateMovie(page);
                    mListView.smoothScrollToPosition(0);
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
            page = 1;
            updateMovie(page);
            mListView.smoothScrollToPosition(0);
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
        mListView = (ListView) rootView.findViewById(R.id.listview_movie);

        TextView textView = new TextView(getActivity());
        String s = "More Movies>>";
        textView.setText(s);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(20);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setPadding(0, 40, 0, 40);
        textView.setBackgroundResource(R.color.colorPrimary);

        textView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View l) {
                // need one more page of result
                page++;
                new FetchMovieTask().execute(mQuery, page+"");
            }
        });

        mListView.addFooterView(textView);
        mListView.setAdapter(mMovieAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = mMovieAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), InfoActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, movie.id);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void updateMovie(int page) {
        FetchMovieTask movieTask = new FetchMovieTask();
        mMovies = new ArrayList<>();
        movieTask.execute(mQuery, page+"");
    }

    @Override
    public void onStart() {
        super.onStart();
        // only fetch page when no movies in adapter
        updateMovie(page);
    }

    public class FetchMovieTask extends AsyncTask<String, Void, List<Movie> > {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        @Override
        protected List<Movie> doInBackground(String... params) {

            if(params.length == 0) {
                return null;
            }

            final String PAGE_PARAM = "page";
            final String page = params[1];

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

                // http://api.themoviedb.org/3/discover/movie?page=1&sort_by=popularity.desc&api_key=xxx

                builtUri = Uri.parse(DISCOVER_BASE_URL).buildUpon()
                        .appendQueryParameter(PAGE_PARAM, page)
                        .appendQueryParameter(SORT_BY_PARAM, order)
                        .appendQueryParameter(Utility.API_KEY_PARAM, Utility.api_key)
                        .build();
            }
            else{
                final String SEARCH_BASE_URL =
                        "http://api.themoviedb.org/3/search/movie?";

                final String QUERY_PARAM = "query";

                final String title = params[0];

                // http://api.themoviedb.org/3/search/movie?page=1&query=title&api_key=xxx

                builtUri = Uri.parse(SEARCH_BASE_URL).buildUpon()
                        .appendQueryParameter(PAGE_PARAM, page)
                        .appendQueryParameter(QUERY_PARAM, title)
                        .appendQueryParameter(Utility.API_KEY_PARAM, Utility.api_key)
                        .build();
            }

            String movieJsonStr = Utility.getJson(builtUri);

            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private List<Movie> getMovieDataFromJson(String movieJsonStr)
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

                mMovies.add(new Movie(id, title, release_date, backdrop_path, poster_path));
            }
            return mMovies;

        }

        @Override
        protected void onPostExecute(List<Movie> result) {
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
