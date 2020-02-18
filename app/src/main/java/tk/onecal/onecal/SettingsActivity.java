package tk.onecal.onecal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsActivity extends AppCompatActivity {

    public static SwitchPreferenceCompat eventFingerprint, peopleFingerprint, calendarMode;
    public static SharedPreferences sharedpreferences;
    public static SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        public final static String SETTINGS_SHARED_PREFERENCES_FILE_NAME = "ONECAL_SETTINGS.SETTINGS_SHARED_PREFERENCES_FILE_NAME";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            getPreferenceManager().setSharedPreferencesName(SETTINGS_SHARED_PREFERENCES_FILE_NAME);
            sharedpreferences = getActivity().getSharedPreferences(SETTINGS_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
            editor = sharedpreferences.edit();
            eventFingerprint = findPreference("event_fingerprint");
            peopleFingerprint = findPreference("people_fingerprint");
            calendarMode = findPreference("calendar_mode");

            if (sharedpreferences.getBoolean("event_fingerprint", false)) {
                eventFingerprint.setChecked(true);
            }
            if (sharedpreferences.getBoolean("people_fingerprint", false)) {
                peopleFingerprint.setChecked(true);
            }
            if (sharedpreferences.getBoolean("calendar_mode", false)) {
                calendarMode.setChecked(true);
            }

            eventFingerprint.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (eventFingerprint.isChecked()) {
                        editor.putBoolean("event_fingerprint", true);
                    } else {
                        editor.putBoolean("event_fingerprint", false);
                    }
                    editor.apply();
                    return true;
                }
            });
            peopleFingerprint.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (peopleFingerprint.isChecked()) {
                        editor.putBoolean("people_fingerprint", true);
                    } else {
                        editor.putBoolean("people_fingerprint", false);
                    }
                    editor.apply();
                    return true;
                }
            });
            calendarMode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (calendarMode.isChecked()) {
                        editor.putBoolean("calendar_mode", true);
                    } else {
                        editor.putBoolean("calendar_mode", false);
                    }
                    editor.apply();
                    return true;
                }
            });

        }
    }
}