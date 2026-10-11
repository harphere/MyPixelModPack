package dev.chet.chromepiestatusmatch.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import dev.chet.mypixelmodpack.R;

public class PiePreferenceFragment extends PreferenceFragment {

    @SuppressLint("WorldReadableFiles")
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("chromepie_v1");
        getPreferenceManager().setSharedPreferencesMode(dev.chet.chromepiestatusmatch.PreferenceAccess.mode(getActivity()));
        addPreferencesFromResource(R.xml.main_preferences);
        SharedPreferences prefs = getActivity().getSharedPreferences(
                getPreferenceManager().getSharedPreferencesName(), dev.chet.chromepiestatusmatch.PreferenceAccess.mode(getActivity()));

        setHasOptionsMenu(true);

        // If SharedPreferences does not contain this preference,
        // we can presume this is a new install
        if (!prefs.contains("screen_slice_1")) {
            PreferenceManager.setDefaultValues(getActivity(), getPreferenceManager().getSharedPreferencesName(),
                    dev.chet.chromepiestatusmatch.PreferenceAccess.mode(getActivity()), R.xml.aosp_preferences, false);
        }

        final Preference killChrome = findPreference("kill_chrome");
        killChrome.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((PieSettings) getActivity()).killProcesses(true);
                return true;
            }
        });

        Preference hideIcon = findPreference("hide_launcher_icon");
        if (hideIcon != null) getPreferenceScreen().removePreference(hideIcon);

        final Preference editMenu = findPreference("edit_pie_menu");
        editMenu.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((PreferenceActivity) getActivity()).startWithFragment(
                        MenuPreferenceFragment.class.getName(), null, null, Activity.RESULT_CANCELED);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionbar_kill:
                ((PieSettings) getActivity()).killProcesses(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.removeItem(R.id.menu_load_defaults);
    }

}
