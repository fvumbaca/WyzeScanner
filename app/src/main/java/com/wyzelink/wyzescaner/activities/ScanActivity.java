package com.wyzelink.wyzescaner.activities;

import android.support.v4.app.Fragment;

import com.wyzelink.wyzescaner.fragments.ScanFragment;

/**
 * Created by Frank on 2015-10-21.
 */
public class ScanActivity extends SFActivity {

    @Override
    public Fragment getFragment() {
        return new ScanFragment();
    }

}
