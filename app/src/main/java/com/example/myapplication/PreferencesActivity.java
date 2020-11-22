package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceActivity;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceManager;


public class PreferencesActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

    }

    @Override
    protected void onPause() {
        super.onPause();

    }
}
