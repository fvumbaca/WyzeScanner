package com.wyzelink.wyzescaner.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.wyzelink.wyzescaner.R;
import com.wyzelink.wyzescaner.fragments.PreferenceFragment;

/**
 * Created by Frank on 2015-10-22.
 */
public class PreferenceActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sf);
        FragmentManager fm = getFragmentManager();

        Fragment fragment = fm.findFragmentById(R.id.activity_sf_content);
        if (fragment == null) {
            fragment = new PreferenceFragment();
            fm.beginTransaction()
                    .add(R.id.activity_sf_content, fragment)
                    .commit();
        }
    }
}
