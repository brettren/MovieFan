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
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class CastFragment extends Fragment  {

    private CastAdapter mCastAdapter;
    private List<Person> mCast;

    private String movieId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cast, container, false);

        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            movieId = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        mCastAdapter =
                new CastAdapter(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_cast, // The ID of the textview to populate.
                        new ArrayList<Person>());

        // Get a reference to the GridView, and attach this adapter to it.
        GridView gridView = (GridView) view.findViewById(R.id.gridview_cast);
        gridView.setAdapter(mCastAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Person person = mCastAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), ProfileActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, person.id);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateCast();
    }

    private void updateCast() {
        FetchCastTask task = new FetchCastTask();
        mCast = new ArrayList<>();
        task.execute(movieId);
    }

    public class FetchCastTask extends AsyncTask<String, Void, List<Person> > {

        private final String LOG_TAG = FetchCastTask.class.getSimpleName();

        @Override
        protected List<Person> doInBackground(String... params) {

            if(params.length == 0) {
                return null;
            }

            // http://api.themoviedb.org/3/movie/id/credits?api_key=xxx

            Uri builtUri = Uri.parse(Utility.BASE_URL + movieId + "/credits?").buildUpon()
                    .appendQueryParameter(Utility.API_KEY_PARAM, Utility.api_key)
                    .build();

            String castJsonStr = Utility.getJson(builtUri);

            try {
                return getCastDataFromJson(castJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }



        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private List<Person> getCastDataFromJson(String castJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String CAST = "cast";

            final String ID = "id";
            final String CHRACTER = "character";
            final String NAME = "name";
            final String PROFILE_PATH = "profile_path";


            JSONObject castJson = new JSONObject(castJsonStr);
            JSONArray castArray = castJson.getJSONArray(CAST);


            for(int i = 0; i < castArray.length(); i++) {

                String id;
                String character;
                String name;
                String profile_path;

                // Get the JSON object representing the movie
                JSONObject movieObject = castArray.getJSONObject(i);

                final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p";
                final String imgSize = "/w500";
                id = movieObject.getString(ID);
                profile_path = IMAGE_BASE_URL + imgSize+ movieObject.getString(PROFILE_PATH);

                character = movieObject.getString(CHRACTER);
                name = movieObject.getString(NAME);

                mCast.add(new Person(id, character, name, profile_path));
            }
            return mCast;
        }

        @Override
        protected void onPostExecute(List<Person> result) {
            if (result != null) {
                mCastAdapter.clear();
                for(Person person : result) {
                    mCastAdapter.add(person);
                }
                // New data is back from the server.  Hooray!
            }
        }
    }
}
