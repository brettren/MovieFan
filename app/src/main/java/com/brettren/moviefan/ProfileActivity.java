package com.brettren.moviefan;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;


public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.profile_container, new ProfileFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }
}
