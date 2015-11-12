package com.wyzelink.wyzescaner.fragments;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.wyzelink.wyzescaner.BuildConfig;
import com.wyzelink.wyzescaner.R;
import com.wyzelink.wyzescaner.ScannerApplication;

/**
 * Created by Frank on 2015-10-22.
 */
public class PreferenceFragment extends android.preference.PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        PreferenceScreen screen = getPreferenceScreen();

        screen.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        PreferenceCategory category = new PreferenceCategory(screen.getContext());
        category.setTitle("Misc");
        screen.addPreference(category);

        EditTextPreference textPreference = new EditTextPreference(screen.getContext());
        textPreference.setTitle("Version");
        textPreference.setText(BuildConfig.VERSION_NAME);
        textPreference.setEnabled(false);
        textPreference.setSummary(BuildConfig.VERSION_NAME);

        category.addPreference(textPreference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Application app = getActivity().getApplication();
        if (app != null && app instanceof ScannerApplication) {
            ((ScannerApplication) app).onSharedPreferenceChanged(sharedPreferences, s);
        }
    }
}
