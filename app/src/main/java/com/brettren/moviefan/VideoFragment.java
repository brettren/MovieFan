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
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VideoFragment extends Fragment {

    private VideoAdapter mVideoAdapter;
    private List<Video> mVideos;

    private String movieId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_video, container, false);

        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            movieId = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

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
        updateVideo();
    }

    private void updateVideo() {
        FetchVideoTask task = new FetchVideoTask();
        mVideos = new ArrayList<>();
        task.execute(movieId);
    }

    public class FetchVideoTask extends AsyncTask<String, Void, List<Video> > {

        private final String LOG_TAG = FetchVideoTask.class.getSimpleName();

        @Override
        protected List<Video> doInBackground(String... params) {

            if(params.length == 0) {
                return null;
            }

            // http://api.themoviedb.org/3/movie/id/credits?api_key=xxx

            Uri builtUri = Uri.parse(Utility.BASE_URL + movieId + "/videos?").buildUpon()
                    .appendQueryParameter(Utility.API_KEY_PARAM, Utility.api_key)
                    .build();

            String videoJsonStr = Utility.getJson(builtUri);

            try {
                return getVideoDataFromJson(videoJsonStr);
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
        private List<Video> getVideoDataFromJson(String videoJsonStr)
                throws JSONException {

            JSONObject videoJson = new JSONObject(videoJsonStr);

            final String RESULT = "results";
            final String KEY = "key";
            final String NAME = "name";

            final String VIDEO_BASE_URL = "https://www.youtube.com/watch?v=";
            final String SCREENSHOT_BASE_URL = "http://img.youtube.com/vi/";

            JSONArray videoArray = videoJson.getJSONArray(RESULT);

            mVideos = new ArrayList<>();
            for(int i = 0; i < videoArray.length(); i++){
                JSONObject videoObject = videoArray.getJSONObject(i);
                String url = VIDEO_BASE_URL + videoObject.getString(KEY);
                String name = videoObject.getString(NAME);
                String screenshot_path = SCREENSHOT_BASE_URL + videoObject.getString(KEY) + "/0.jpg";
                mVideos.add(new Video(url, name, screenshot_path));
            }
            return mVideos;
        }

        @Override
        protected void onPostExecute(List<Video> result) {
            if (result != null) {
                mVideoAdapter.clear();
                for(Video video : result) {
                    mVideoAdapter.add(video);
                }
                // New data is back from the server.  Hooray!
            }
        }
    }
}