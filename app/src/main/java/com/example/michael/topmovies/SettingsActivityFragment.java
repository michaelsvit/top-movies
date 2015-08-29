package com.example.michael.topmovies;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsActivityFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public SettingsActivityFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        //Update summaries
        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), getString(R.string.pref_sort_by_key));
        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), getString(R.string.pref_order_key));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_sort_by_key))) {
            Preference sortPref = findPreference(key);
            sortPref.setSummary(sharedPreferences.getString(key, ""));
        } else if(key.equals(getString(R.string.pref_order_key))) {
            Preference orderPref = findPreference(key);
            orderPref.setSummary(sharedPreferences.getString(key, ""));
        }
    }
}
