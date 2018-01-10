package com.example.bootcampsns.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.bootcampsns.R;

public class ServerSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new ServerSettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        String server_host = preference.getString("server_host", "");
        String server_port = preference.getString("server_port", "");
        if(server_host.equals("") || server_port.equals("")) {
            return false;
        }

        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class ServerSettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        public ServerSettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }

        @Override
        public void onResume() {
            super.onResume();
            SharedPreferences preference = getPreferenceScreen().getSharedPreferences();
            preference.registerOnSharedPreferenceChangeListener(this);
            setAllSummary(preference);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        private void setAllSummary(final SharedPreferences preference) {
            String[] items = {"server_host", "server_port", "proxy_host", "proxy_port"};
            for(int i=0;i<items.length;++i) {
                findPreference(items[i]).setSummary(preference.getString(items[i], "None"));
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            setAllSummary(sharedPreferences);
        }
    }
}
