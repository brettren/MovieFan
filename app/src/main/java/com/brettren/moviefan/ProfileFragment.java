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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;


public class ProfileFragment extends Fragment {
    private static final String LOG_TAG = ProfileFragment.class.getSimpleName();

    private String personId;

    ImageView imgProfile;
    TextView txtName;
    TextView txtBirthday;
    TextView txtPlaceofbirth;
    TextView txtBiography;


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

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }

    private void updateMovie() {
        FetchProfileTask task = new FetchProfileTask();
        task.execute();
    }

    public class FetchProfileTask extends AsyncTask<String, Void, Person> {

        private final String LOG_TAG = FetchProfileTask.class.getSimpleName();

        @Override
        protected Person doInBackground(String... params) {

            // https://api.themoviedb.org/3/person/id?api_key=xxx
            String PERSON_BASE_URL = "http://api.themoviedb.org/3/person/";
            Uri builtUri = Uri.parse(PERSON_BASE_URL + personId + "?").buildUpon()
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
}
