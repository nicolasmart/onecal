package tk.onecal.onecal;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.CryptoObject;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Toast;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import static android.hardware.biometrics.BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS;

public class SettingsActivity extends AppCompatActivity {

    public static SwitchPreferenceCompat eventFingerprint, peopleFingerprint, calendarMode, useLightIcon, fullScreenNotifications, smartConfig, delayedStart, gradientSound;
    public static SharedPreferences sharedpreferences;
    public static SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        final int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().setBackgroundDrawableResource(R.drawable.dark_bg_drawable);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @TargetApi(28)
    public static class SettingsFragment extends PreferenceFragmentCompat {

        public final static String SETTINGS_SHARED_PREFERENCES_FILE_NAME = "ONECAL_SETTINGS.SETTINGS_SHARED_PREFERENCES_FILE_NAME";

        public static ExecutorService executorService;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            executorService = Executors.newCachedThreadPool();

            getPreferenceManager().setSharedPreferencesName(SETTINGS_SHARED_PREFERENCES_FILE_NAME);
            sharedpreferences = getActivity().getSharedPreferences(SETTINGS_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
            editor = sharedpreferences.edit();
            eventFingerprint = findPreference("event_fingerprint");
            peopleFingerprint = findPreference("people_fingerprint");
            calendarMode = findPreference("calendar_mode");
            useLightIcon = findPreference("use_light_icon");
            //fullScreenNotifications = findPreference("annoying_notifications");
            smartConfig = findPreference("smart_config");
            delayedStart = findPreference("delayed_start");
            gradientSound = findPreference("gradient_sound");

            if (sharedpreferences.getBoolean("event_fingerprint", false)) {
                eventFingerprint.setChecked(true);
            }
            if (sharedpreferences.getBoolean("people_fingerprint", false)) {
                peopleFingerprint.setChecked(true);
            }
            if (sharedpreferences.getBoolean("calendar_mode", false)) {
                calendarMode.setChecked(true);
            }
            if (sharedpreferences.getBoolean("use_light_icon", false)) {
                useLightIcon.setChecked(true);
            }
            if (sharedpreferences.getBoolean("smart_config", false)) {
                smartConfig.setChecked(true);
            }
            if (sharedpreferences.getBoolean("delayed_start", false)) {
                delayedStart.setChecked(true);
            }
            if (sharedpreferences.getBoolean("gradient_sound", true)) {
                gradientSound.setChecked(true);
            }

            eventFingerprint.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (eventFingerprint.isChecked()) {
                        fingerprintCheck("event_fingerprint", true);
                    } else {
                        fingerprintCheck("event_fingerprint", false);
                    }
                    editor.apply();
                    return true;
                }
            });
            peopleFingerprint.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (peopleFingerprint.isChecked()) {
                        fingerprintCheck("people_fingerprint", true);
                    } else {
                        fingerprintCheck("people_fingerprint", false);
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
            useLightIcon.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (useLightIcon.isChecked()) {
                        editor.putBoolean("use_light_icon", true);
                        getContext().getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(), tk.onecal.onecal.PeopleDark.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                        getContext().getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(), tk.onecal.onecal.OneCalDark.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                        getContext().getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(), tk.onecal.onecal.PeopleLight.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                        getContext().getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(), tk.onecal.onecal.OneCalLight.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    } else {
                        editor.putBoolean("use_light_icon", false);
                        getContext().getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(), tk.onecal.onecal.PeopleDark.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                        getContext().getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(), tk.onecal.onecal.OneCalDark.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                        getContext().getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(), tk.onecal.onecal.PeopleLight.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                        getContext().getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(), tk.onecal.onecal.OneCalLight.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    }
                    editor.apply();
                    return true;
                }
            });
            smartConfig.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (smartConfig.isChecked()) {
                        editor.putBoolean("smart_config", true);
                    } else {
                        editor.putBoolean("smart_config", false);
                    }
                    editor.apply();
                    return true;
                }
            });
            delayedStart.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (delayedStart.isChecked()) {
                        editor.putBoolean("delayed_start", true);
                    } else {
                        editor.putBoolean("delayed_start", false);
                    }
                    editor.apply();
                    return true;
                }
            });
            gradientSound.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (gradientSound.isChecked()) {
                        editor.putBoolean("gradient_sound", true);
                    } else {
                        editor.putBoolean("gradient_sound", false);
                    }
                    editor.apply();
                    return true;
                }
            });

        }

        BiometricPrompt mBiometricPrompt;
        CancellationSignal mCancellationSignal;
        BiometricPrompt.AuthenticationCallback mAuthenticationCallback;
        private Signature mSignature;
        private String mToBeSignedMessage;
        private static final String DEFAULT_KEY_NAME = "default_key";

        void changeViews(final String change_key, final boolean value)
        {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (change_key=="people_fingerprint") peopleFingerprint.setChecked(!value);
                    else if (change_key=="event_fingerprint") eventFingerprint.setChecked(!value);
                }
            });
        }

        void fingerprintCheck(final String change_key, final boolean value)
        {
            mCancellationSignal = new CancellationSignal();

            mBiometricPrompt = new BiometricPrompt.Builder(getContext())
                    .setTitle(getString(R.string.app_name))
                    .setSubtitle(getString(R.string.fingerprint_subtitle_settings))
                    .setDescription(getString(R.string.fingerprint_desc_settings))
                    .setNegativeButton(
                            getString(R.string.fingerprint_exit),
                            executorService,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    changeViews(change_key, value);
                                }
                            }
                    )
                    .build();

            try {
                KeyPair keyPair = generateKeyPair(DEFAULT_KEY_NAME, true);
                mToBeSignedMessage = new StringBuilder()
                        .append(Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.URL_SAFE))
                        .append(":")
                        .append(DEFAULT_KEY_NAME)
                        .append(":")
                        .append("onecal_local")
                        .toString();

                mSignature = initSignature(DEFAULT_KEY_NAME);
            } catch (Exception e) {
                return;
            }

            mAuthenticationCallback = new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    changeViews(change_key, value);
                }

                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    editor.putBoolean(change_key, value);
                    return;
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                }
            };

            mBiometricPrompt.authenticate(new BiometricPrompt.CryptoObject(mSignature), mCancellationSignal, executorService, mAuthenticationCallback);

        }

        @TargetApi(Build.VERSION_CODES.P)
        private KeyPair generateKeyPair(String keyName, boolean invalidatedByBiometricEnrollment) throws Exception {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_SIGN)
                    .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                    .setDigests(KeyProperties.DIGEST_SHA256,
                            KeyProperties.DIGEST_SHA384,
                            KeyProperties.DIGEST_SHA512)
                    .setUserAuthenticationRequired(true)
                    .setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);

            keyPairGenerator.initialize(builder.build());

            return keyPairGenerator.generateKeyPair();
        }

        @Nullable
        private KeyPair getKeyPair(String keyName) throws Exception {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (keyStore.containsAlias(keyName)) {
                PublicKey publicKey = keyStore.getCertificate(keyName).getPublicKey();
                PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyName, null);
                return new KeyPair(publicKey, privateKey);
            }
            return null;
        }

        @Nullable
        private Signature initSignature(String keyName) throws Exception {
            KeyPair keyPair = getKeyPair(keyName);

            if (keyPair != null) {
                Signature signature = Signature.getInstance("SHA256withECDSA");
                signature.initSign(keyPair.getPrivate());
                return signature;
            }
            return null;
        }
    }
}