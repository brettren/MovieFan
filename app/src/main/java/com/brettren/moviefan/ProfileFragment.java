package com.brettren.moviefan;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lucasr.twowayview.TwoWayView;

import java.util.ArrayList;
import java.util.List;


public class ProfileFragment extends Fragment {
    private static final String LOG_TAG = ProfileFragment.class.getSimpleName();

    private String personId;

    ImageView imgProfile;
    TextView txtName;
    TextView txtBirthday;
    TextView txtPlaceofbirth;
    TextView txtBiography;

    // for production param
    private ProductionAdapter mProductionAdapter;
    private TwoWayView mListView;
    private List<Movie> mMovies;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            personId = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
        txtName = (TextView)view.findViewById(R.id.txtName);
        txtBirthday = (TextView)view.findViewById(R.id.txtBirthday);
        txtPlaceofbirth = (TextView)view.findViewById(R.id.txtPlaceofbirth);
        txtBiography = (TextView)view.findViewById(R.id.txtBiography);
        imgProfile = (ImageView)view.findViewById(R.id.imageProfile);



        mProductionAdapter =
                new ProductionAdapter(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_production, // The ID of the textview to populate.
                        new ArrayList<Movie>());
        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (TwoWayView) view.findViewById(R.id.listview_production);
        mListView.setAdapter(mProductionAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = mProductionAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), InfoActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, movie.id);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateProfile();
    }

    private void updateProfile() {

        FetchProductionTask task1 = new FetchProductionTask();
        mMovies = new ArrayList<>();
        task1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        FetchProfileTask task = new FetchProfileTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public class FetchProfileTask extends AsyncTask<String, Void, Person> {

        private final String LOG_TAG = FetchProfileTask.class.getSimpleName();

        @Override
        protected Person doInBackground(String... params) {

            // https://api.themoviedb.org/3/person/id?api_key=xxx

            Uri builtUri = Uri.parse(Utility.PERSON_BASE_URL + personId + "?").buildUpon()
                    .appendQueryParameter(Utility.API_KEY_PARAM, Utility.api_key)
                    .build();

            String personJsonStr = Utility.getJson(builtUri);

            try {
                return getPersonDataFromJson(personJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private Person getPersonDataFromJson(String personJsonStr)
                throws JSONException {

            if(personJsonStr == null) return new Person();

            // These are the names of the JSON objects that need to be extracted.

            final String ID = "id";
            final String BIOGRAPHY = "biography";
            final String BIRTHDAY = "birthday";
            final String NAME = "name";
            final String PLACEOFBIRTH = "place_of_birth";
            final String PROFILE_PATH = "profile_path";

            JSONObject personObject = new JSONObject(personJsonStr);

            String id;
            String biography;
            String birthday;
            String name;
            String place_of_birth;
            String profile_path;


            final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p";
            final String imgSize = "/w500";

            id = personObject.getString(ID);
            profile_path = IMAGE_BASE_URL + imgSize+ personObject.getString(PROFILE_PATH);
            biography = personObject.getString(BIOGRAPHY);
            birthday = "Birthday: " + personObject.getString(BIRTHDAY);
            name = personObject.getString(NAME);
            place_of_birth = "Place of birth: " + personObject.getString(PLACEOFBIRTH);

            Person person = new Person(id, name, profile_path, biography, birthday, place_of_birth);

            return person;

        }


        @Override
        protected void onPostExecute(Person person) {
            if (person == null) return;
            txtName.setText(person.name);
            txtBirthday.setText(person.birthday);
            txtPlaceofbirth.setText(person.place_of_birth);
            txtBiography.setText(person.biography);

            Picasso.with(getActivity()).load(person.profile_path).into(imgProfile);

        }

    }


    public class FetchProductionTask extends AsyncTask<String, Void, List<Movie> > {

        private final String LOG_TAG = FetchProductionTask.class.getSimpleName();

        @Override
        protected List<Movie> doInBackground(String... params) {


            // https://api.themoviedb.org/3/person/id/movie_credits?api_key=xxx
            Uri builtUri = Uri.parse(Utility.PERSON_BASE_URL + personId + "/movie_credits?").buildUpon()
                    .appendQueryParameter(Utility.API_KEY_PARAM, Utility.api_key)
                    .build();

            String productionJsonStr = Utility.getJson(builtUri);

            try {
                return getProductionDataFromJson(productionJsonStr);
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
        private List<Movie> getProductionDataFromJson(String productionJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String CAST = "cast";

            final String ID = "id";
            final String RELEASE_DATE = "release_date";
            final String TITLE = "title";
            final String POSTER_PATH = "poster_path";


            JSONObject productionJson = new JSONObject(productionJsonStr);
            JSONArray productionArray = productionJson.getJSONArray(CAST);


            for(int i = 0; i < productionArray.length(); i++) {

                String id;
                String poster_path;
                String title;
                String release_date;

                // Get the JSON object representing the movie
                JSONObject movieObject = productionArray.getJSONObject(i);

                final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p";
                final String imgSize = "/w500";
                id = movieObject.getString(ID);
                poster_path = IMAGE_BASE_URL + imgSize+ movieObject.getString(POSTER_PATH);

                title = movieObject.getString(TITLE);
                release_date = movieObject.getString(RELEASE_DATE);

                mMovies.add(new Movie(id, title, release_date, poster_path));
            }
            return mMovies;
        }

        @Override
        protected void onPostExecute(List<Movie> result) {
            if (result != null) {
                mProductionAdapter.clear();
                for(Movie movie : result) {
                    mProductionAdapter.add(movie);
                }
                // New data is back from the server.  Hooray!
            }
        }
    }
}
