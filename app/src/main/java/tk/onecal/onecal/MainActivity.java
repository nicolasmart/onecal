package tk.onecal.onecal;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.biometrics.BiometricPrompt;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.speech.RecognizerIntent;
import android.text.InputType;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.wdullaer.materialdatetimepicker.Utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import tk.onecal.onecal.data.AlarmReminderContract;
import tk.onecal.onecal.data.PeopleCustomContract;

import static android.content.Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    int BIOMETRIC_ERROR_NO_BIOMETRICS = 11;

    private Toolbar mToolbar;
    private ImageView authenticationCover;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private NavigationView navigationView;
    private FloatingActionButton mAddReminderButton;
    private AppBarLayout mAppBarLayout;
    private RelativeLayout aboutView;
    int tabPosition;
    DrawerLayout drawer;

    NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this);

    private String alarmTitle = "";
    private String[] groupTabs;

    private Integer currentOpenState = 0;
    private Boolean newState = true;

    @TargetApi(Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(java.lang.Integer.toHexString(getIntent().getFlags()).equals("14000000"))
        {
            for (String key : getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
                Log.d("MainActivity", "Key: " + key + " Value: " + value);
            }
        }

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        authenticationCover = (ImageView) findViewById(R.id.authenticationHider);
        authenticationCover.bringToFront();

        mAddReminderButton = (FloatingActionButton) findViewById(R.id.fab);

        mAddReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReminderTitle();

            }
        });


        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPosition = tab.getPosition();
                if (tabPosition==0) mAddReminderButton.hide();
                else mAddReminderButton.show();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerSlide(final View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            authenticationCover.setVisibility(View.VISIBLE);
            mTabLayout.setVisibility(View.INVISIBLE);
            mAppBarLayout.setOutlineProvider(null);
            fingerprintCheck();
        }
        else {
            authenticationCover.setVisibility(View.INVISIBLE);
            mTabLayout.setVisibility(View.VISIBLE);
        }

        setupViewPager(mViewPager);
        mTabLayout.setupWithViewPager(mViewPager);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_cal);
        navigationView.setNavigationItemSelectedListener(this);

        View hView =  navigationView.getHeaderView(0);
        ImageView header = hView.findViewById(R.id.nav_header);
        if (Locale.getDefault().getLanguage().contains("en")) header.setImageResource(R.drawable.onecal_header);

        aboutView = findViewById(R.id.about_view);

        final int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().setBackgroundDrawableResource(R.drawable.dark_bg_drawable);
            navigationView.setBackgroundResource(R.drawable.dark_bg_drawable);
        }

        runBootService(BootService.class);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                finish();
            }
        }
        return super.onKeyUp(keyCode, event);
    }


    Window window3;

    void leaveAlive()
    {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("activityhandle", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("killapp", 0);
        editor.commit();
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("activityhandle", 0);
        if (prefs.getInt("killapp", 1)==0)
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("killapp", 1);
            editor.commit();
        }
        else this.finish();
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }



    private BiometricPrompt mBiometricPrompt;
    private CancellationSignal mCancellationSignal;
    private BiometricPrompt.AuthenticationCallback mAuthenticationCallback;
    private Signature mSignature;
    private String mToBeSignedMessage;
    private static final String DEFAULT_KEY_NAME = "default_key";

    @RequiresApi(Build.VERSION_CODES.P)
    public void fingerprintCheck() {
        SharedPreferences preferences = getSharedPreferences(
                SettingsActivity.SettingsFragment.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        if (!preferences.getBoolean("event_fingerprint", false)) {
            authenticationCover.setVisibility(View.INVISIBLE);
            mTabLayout.setVisibility(View.VISIBLE);
            return;
        }

        mBiometricPrompt = new BiometricPrompt.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setSubtitle(getString(R.string.fingerprint_subtitle))
                .setDescription(getString(R.string.fingerprint_desc))
                .setNegativeButton(
                        getString(R.string.fingerprint_exit),
                        this.getMainExecutor(),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
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
            authenticationCover.setVisibility(View.INVISIBLE);
            mTabLayout.setVisibility(View.VISIBLE);
            mAppBarLayout.setOutlineProvider(ViewOutlineProvider.BOUNDS);
            return;
        }

        mAuthenticationCallback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode==BIOMETRIC_ERROR_NO_BIOMETRICS) {
                    authenticationCover.setVisibility(View.INVISIBLE);
                    mTabLayout.setVisibility(View.VISIBLE);
                    mAppBarLayout.setOutlineProvider(ViewOutlineProvider.BOUNDS);
                    return;
                }
                finish();
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                authenticationCover.setVisibility(View.INVISIBLE);
                mTabLayout.setVisibility(View.VISIBLE);
                mAppBarLayout.setOutlineProvider(ViewOutlineProvider.BOUNDS);
                return;
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                finish();
            }
        };
        if (mCancellationSignal == null) {
            mCancellationSignal = new CancellationSignal();
        }

        mBiometricPrompt.authenticate(new BiometricPrompt.CryptoObject(mSignature), mCancellationSignal, getMainExecutor(), mAuthenticationCallback);


        return;
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

    public void animateStatusBar(Integer from, Integer to)
    {
        window3 = getWindow();
        window3.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        ValueAnimator colorAnimation = ValueAnimator.ofArgb(from, to);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                drawer.invalidate();

                window3.setStatusBarColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int suretabposition = tabPosition;
        setupViewPager(mViewPager);
        selectPage(suretabposition);
    }

    Bitmap imageBitmap;

    public void addReminderTitle(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.create_event_dialog));

        TextInputLayout textInputLayout = new TextInputLayout(this);

        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(getString(R.string.event_name));

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int left_margin = Utils.dpToPx(20, this.getResources());
        int top_margin = Utils.dpToPx(10, this.getResources());
        int right_margin = Utils.dpToPx(20, this.getResources());
        int bottom_margin = Utils.dpToPx(4, this.getResources());
        params.setMargins(left_margin, top_margin, right_margin, bottom_margin);

        textInputLayout.setLayoutParams(params);

        textInputLayout.addView(input);
        container.addView(textInputLayout);

        builder.setView(container);

        builder.setPositiveButton(getString(R.string.create_event_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().isEmpty()){
                    return;
                }

                alarmTitle = input.getText().toString();

                Calendar mCalendar;
                int mHour, mMinute, mYear, mMonth, mDay;
                String mDate, mTime;
                mCalendar = Calendar.getInstance();
                mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
                mMinute = mCalendar.get(Calendar.MINUTE);
                mYear = mCalendar.get(Calendar.YEAR);
                mMonth = mCalendar.get(Calendar.MONTH) + 1;
                mDay = mCalendar.get(Calendar.DATE);

                mDate = mDay + "/" + mMonth + "/" + mYear;
                mTime = mHour + ":" + mMinute;

                ContentValues values = new ContentValues();

                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE, alarmTitle);
                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_DATE, mDate);
                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TIME, mTime);
                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_LENGTH, "60");
                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_GROUP, groupTabs[tabPosition-1]);
                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_IMPORTANCE_LEVEL, getString(R.string.light_importance));
                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO, "1");
                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE, getString(R.string.repeat_day));

                Uri newUri = getApplicationContext().getContentResolver().insert(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, values);

                if (newUri == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.create_event_fail), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.create_event_success), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, AddReminderActivity.class);
                    intent.setData(newUri);

                    Bundle b = new Bundle();
                    b.putString("tabName", groupTabs[tabPosition-1]);
                    b.putString("uri", newUri.toString());
                    b.putBoolean("newReminder", true);
                    intent.putExtras(b);

                    SharedPreferences prefs = MainActivity.this.getSharedPreferences("activityhandle", 0);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("killapp", 0);
                    editor.commit();
                    startActivity(intent);
                }

            }
        });
        builder.setNegativeButton(getString(R.string.create_event_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setNeutralButton(getString(R.string.camera), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dispatchTakePictureIntent();

            }
        });

        builder.show();
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 123);
            return;
        }
        leaveAlive();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 6638) {
            try {
                    String requestText;
                    requestText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0).toLowerCase();

                    if (requestText.contains(getString(R.string.set_a_reminder_voice)) && tabPosition > 0) {
                        int i_time = requestText.indexOf(getString(R.string.for_time));
                        int i_title = requestText.indexOf(getString(R.string.with_title));

                        Calendar mCalendar;
                        mCalendar = Calendar.getInstance();

                        String timeRequest="", titleRequest="", newTimeRequest="";
                        if (i_time<0 || i_title<0)
                        {
                            if (i_title>=0) titleRequest = requestText.substring(i_title + getString(R.string.with_title).length() + 1);
                            if (i_time>=0) timeRequest = requestText.substring(i_title + getString(R.string.with_title).length() + 1);
                            else {
                                String mDate, mTime;
                                int mHour, mMinute;

                                mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
                                mMinute = mCalendar.get(Calendar.MINUTE);

                                newTimeRequest = mHour + ":" + mMinute;
                            }
                        }
                        else {
                            if (i_time < i_title) {
                                timeRequest = requestText.substring(i_time, i_title);
                                titleRequest = requestText.substring(i_title + getString(R.string.with_title).length() + 1);
                            } else if (i_title < i_time) {
                                titleRequest = requestText.substring(i_title, i_title);
                                timeRequest = requestText.substring(i_title + getString(R.string.with_title).length() + 1);
                            }
                        }

                        if (newTimeRequest=="") {
                            //Toast.makeText(this, timeRequest, Toast.LENGTH_SHORT).show();
                            timeRequest = timeRequest.replace(getString(R.string.one_number), "1");
                            timeRequest = timeRequest.replace(getString(R.string.two_number), "2");
                            timeRequest = timeRequest.replace(getString(R.string.three_number), "3");
                            timeRequest = timeRequest.replace(getString(R.string.four_number), "4");
                            timeRequest = timeRequest.replace(getString(R.string.five_number), "5");
                            timeRequest = timeRequest.replace(getString(R.string.six_number), "6");
                            timeRequest = timeRequest.replace(getString(R.string.seven_number), "7"); ///TODO: FINGERPRINT SHOULD NOT FORCE EXIT
                            timeRequest = timeRequest.replace(getString(R.string.eight_number), "8");
                            timeRequest = timeRequest.replace(getString(R.string.nine_number), "9");
                            timeRequest = timeRequest.replace(getString(R.string.ten_number), "10");
                            timeRequest = timeRequest.replace(getString(R.string.eleven_number), "11");
                            timeRequest = timeRequest.replace(getString(R.string.twelve_number), "0");
                            timeRequest = timeRequest.replace(getString(R.string.and_a_half), ":30");
                            for (int i = 0; i < timeRequest.length(); i++) {
                                if (Character.isDigit(timeRequest.charAt(i)) || timeRequest.charAt(i) == ':')
                                    newTimeRequest += timeRequest.charAt(i);
                            }
                            //Toast.makeText(this, newTimeRequest, Toast.LENGTH_SHORT).show();

                            if (newTimeRequest.length() < 2 && timeRequest.contains(getString(R.string.pm_time)))
                                newTimeRequest = String.valueOf(Integer.parseInt(String.valueOf(newTimeRequest.charAt(0))) + 12) + newTimeRequest.substring(1);
                            else if (!Character.isDigit(newTimeRequest.charAt(1)) && timeRequest.contains(getString(R.string.pm_time)))
                                newTimeRequest = String.valueOf(Integer.parseInt(String.valueOf(newTimeRequest.charAt(0))) + 12) + newTimeRequest.substring(1);
                            else if (timeRequest.contains(getString(R.string.pm_time)))
                                newTimeRequest = String.valueOf((Integer.parseInt(String.valueOf(newTimeRequest.charAt(0))) * 10) + Integer.parseInt(String.valueOf(newTimeRequest.charAt(1))) + 12) + newTimeRequest.substring(2);

                            if (newTimeRequest.length() < 3) newTimeRequest += ":00";

                        }

                        int mYear, mMonth, mDay;

                        mYear = mCalendar.get(Calendar.YEAR);
                        mMonth = mCalendar.get(Calendar.MONTH) + 1;
                        mDay = mCalendar.get(Calendar.DATE);
                        if (timeRequest.contains(getString(R.string.tomorrow))) mCalendar.add(Calendar.DAY_OF_MONTH, 1);

                        String mDate;

                        mDate = mDay + "/" + mMonth + "/" + mYear;

                        ContentValues values = new ContentValues();

                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE, titleRequest);
                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_DATE, mDate);
                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TIME, newTimeRequest);
                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_GROUP, groupTabs[tabPosition - 1]);
                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_IMPORTANCE_LEVEL, getString(R.string.light_importance));
                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO, "1");
                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE, getString(R.string.repeat_day));
                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE, "true");

                        Uri newUri = getApplicationContext().getContentResolver().insert(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, values);

                        if (newUri == null) {
                            Toast.makeText(getApplicationContext(), getString(R.string.create_event_fail), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.create_event_success), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, AddReminderActivity.class);
                            intent.setData(newUri);

                            Bundle b = new Bundle();
                            b.putString("tabName", groupTabs[tabPosition - 1]);
                            b.putString("uri", newUri.toString());
                            b.putBoolean("newReminder", true);
                            intent.putExtras(b);

                            SharedPreferences prefs = MainActivity.this.getSharedPreferences("activityhandle", 0);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("killapp", 0);
                            editor.commit();
                            startActivity(intent);
                        }
                    } else if (requestText.toLowerCase().contains(getString(R.string.open_settings))) {
                        Intent intentSettings = new Intent(this, SettingsActivity.class);
                        leaveAlive();
                        startActivity(intentSettings);
                    } else if (requestText.toLowerCase().contains(getString(R.string.open_contacts))) {
                        if (requestText.toLowerCase().contains(getString(R.string.and_call))) {
                            int i_name = requestText.indexOf(getString(R.string.and_call)) + getString(R.string.and_call).length() + 1;
                            String contactname = requestText.substring(i_name);
                            Cursor cursor_phone = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    "UPPER(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") LIKE \"%"
                                            + contactname.toUpperCase() + "%\"", null, null);

                            if (cursor_phone.moveToFirst()) {
                                int colIdx = cursor_phone
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                Intent intent_call = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", cursor_phone.getString(colIdx), null));
                                startActivity(intent_call);
                            }
                            cursor_phone.close();
                        } else if (requestText.toLowerCase().contains(getString(R.string.and_show))) {
                            int i_name = requestText.indexOf(getString(R.string.and_show)) + getString(R.string.and_show).length() + 1;
                            String contactname = requestText.substring(i_name);
                            Cursor cursor_phone = getContentResolver().query(
                                    PeopleCustomContract.PeopleCustomEntry.CONTENT_URI,
                                    null,
                                    "UPPER(" + PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME + ") LIKE \"%"
                                            + contactname.toUpperCase() + "%\"", null, null);

                            if (cursor_phone.moveToFirst()) {
                                int colIdxName = cursor_phone
                                        .getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME);
                                int colIdxId = cursor_phone
                                        .getColumnIndex(PeopleCustomContract.PeopleCustomEntry._ID);
                                int colIdxNum = cursor_phone
                                        .getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_PHONE_NUMBER);
                                int colIdxPhoto = cursor_phone
                                        .getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_PHOTO_URI);

                                String phoneNumber = cursor_phone.getString(colIdxNum);

                                Cursor cursor_phone_num = getContentResolver().query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
                                                + cursor_phone.getString(colIdxId), null, null);

                                if (cursor_phone_num.moveToFirst()) {
                                    int colIdx = cursor_phone_num
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                    phoneNumber = cursor_phone_num.getString(colIdx);
                                }
                                cursor_phone_num.close();

                                Bitmap contactPhoto = null;
                                AssetFileDescriptor afd = null;
                                try {
                                    Uri thumbUri;
                                    thumbUri = Uri.parse(cursor_phone.getString(colIdxPhoto));
                                    afd = getContentResolver().
                                            openAssetFileDescriptor(thumbUri, "r");
                                    FileDescriptor fileDescriptor = afd.getFileDescriptor();
                                    if (fileDescriptor != null) {
                                        contactPhoto = BitmapFactory.decodeFileDescriptor(
                                                fileDescriptor, null, null);
                                    }
                                } catch (FileNotFoundException e) {
                                } finally {
                                    if (afd != null) {
                                        try {
                                            afd.close();
                                        } catch (IOException e) {
                                        }
                                    }
                                }

                                byte[] photoByteArray;
                                try {
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    contactPhoto.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                    photoByteArray = stream.toByteArray();
                                } catch (Exception ex) {
                                    photoByteArray = null;
                                }

                                Bundle b = new Bundle();
                                b.putString("contact_name", cursor_phone.getString(colIdxName));
                                b.putString("contact_id", cursor_phone.getString(colIdxId));
                                b.putString("contact_phone_number", phoneNumber);
                                b.putString("current_group", "null");
                                b.putInt("current_tab", -1);
                                b.putByteArray("contact_photo", photoByteArray);

                                leaveAlive();
                                Intent contactView = new Intent(this, ContactViewActivity.class);
                                contactView.putExtras(b);
                                startActivity(contactView);
                            }
                            cursor_phone.close();

                        } else {
                            Intent intent = new Intent(getApplicationContext(), PeopleActivity.class);
                            intent.addFlags(FLAG_ACTIVITY_LAUNCH_ADJACENT |
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                            leaveAlive();
                            startActivity(intent);
                        }
                    }
                    else Toast.makeText(this, getText(R.string.invalid_command), Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                    Toast.makeText(this, getText(R.string.invalid_command), Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");

            FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);

            FirebaseVisionBarcodeDetectorOptions options =
                    new FirebaseVisionBarcodeDetectorOptions.Builder()
                            .setBarcodeFormats(
                                    FirebaseVisionBarcode.FORMAT_QR_CODE)
                            .build();
            FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                    .getVisionBarcodeDetector();

            Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(firebaseVisionImage)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                            for (FirebaseVisionBarcode barcode: barcodes) {
                                Rect bounds = barcode.getBoundingBox();
                                Point[] corners = barcode.getCornerPoints();

                                String rawValue = barcode.getRawValue();

                                int valueType = barcode.getValueType();

                                if (valueType == FirebaseVisionBarcode.TYPE_CALENDAR_EVENT) {
                                    Log.w("MainActivity", barcode.getRawValue());
                                    try {
                                        String title = barcode.getCalendarEvent().getSummary();
                                        String date = barcode.getCalendarEvent().getStart().getDay() + "/" + barcode.getCalendarEvent().getStart().getMonth() + "/" + barcode.getCalendarEvent().getStart().getYear();
                                        String time = barcode.getCalendarEvent().getStart().getHours() + ":" + barcode.getCalendarEvent().getStart().getMinutes();

                                        ContentValues values = new ContentValues();

                                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE, title);
                                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_DATE, date);
                                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TIME, time);
                                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_GROUP, groupTabs[tabPosition-1]);
                                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_IMPORTANCE_LEVEL, getString(R.string.light_importance));
                                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO, "1");
                                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE, getString(R.string.repeat_day));

                                        Uri newUri = getApplicationContext().getContentResolver().insert(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, values);

                                        if (newUri == null) {
                                            Toast.makeText(getApplicationContext(), getString(R.string.create_event_fail), Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), getString(R.string.create_event_success), Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(MainActivity.this, AddReminderActivity.class);
                                            intent.setData(newUri);

                                            Bundle b = new Bundle();
                                            b.putString("tabName", groupTabs[tabPosition-1]);
                                            b.putString("uri", newUri.toString());
                                            b.putBoolean("newReminder", true);
                                            intent.putExtras(b);

                                            SharedPreferences prefs = MainActivity.this.getSharedPreferences("activityhandle", 0);
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putInt("killapp", 0);
                                            editor.commit();
                                            startActivity(intent);
                                        }
                                        return;
                                    } catch (NullPointerException ex) {
                                        break;
                                    }
                                }
                            }
                            textRecognition(firebaseVisionImage);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });


        }
    }

    protected void textRecognition(FirebaseVisionImage firebaseVisionImage) {
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        Task<FirebaseVisionText> result2 =
                firebaseVisionTextRecognizer.processImage(firebaseVisionImage)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                alarmTitle="";
                                Calendar mCalendar;
                                mCalendar = Calendar.getInstance();
                                String mDate, mTime;
                                int mHour, mMinute;

                                mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
                                mMinute = mCalendar.get(Calendar.MINUTE);

                                mTime = mHour + ":" + mMinute;

                                int mYear, mMonth, mDay;

                                mYear = mCalendar.get(Calendar.YEAR);
                                mMonth = mCalendar.get(Calendar.MONTH) + 1;
                                mDay = mCalendar.get(Calendar.DATE);

                                mDate = mDay + "/" + mMonth + "/" + mYear;

                                for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {
                                    String blockText = block.getText();
                                    boolean not_match_loop = true;
                                    Pattern pTime = Pattern.compile("(([01]?[0-9]|2[0-3]):[0-5][0-9])");
                                    Pattern pDate = Pattern.compile("(\\d{2}[/]\\d{2}[/]\\d{4})");
                                    Matcher rTime = pTime.matcher(blockText);
                                    Matcher rDate = pDate.matcher(blockText);

                                    if (rTime.matches()) {
                                        mTime = rTime.group(1);
                                        not_match_loop = false;
                                    }
                                    if (rDate.matches()) {
                                        mDate = rDate.group(1);
                                        not_match_loop = false;
                                    }
                                    if (not_match_loop && alarmTitle.isEmpty()) alarmTitle = blockText;

                                }

                                ContentValues values = new ContentValues();

                                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE, alarmTitle);
                                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_DATE, mDate);
                                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TIME, mTime);
                                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_GROUP, groupTabs[tabPosition-1]);
                                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_IMPORTANCE_LEVEL, getString(R.string.light_importance));
                                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO, "1");
                                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE, getString(R.string.repeat_day));

                                Uri newUri = getApplicationContext().getContentResolver().insert(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, values);

                                if (newUri == null) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.create_event_fail), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.create_event_success), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, AddReminderActivity.class);
                                    intent.setData(newUri);

                                    Bundle b = new Bundle();
                                    b.putString("tabName", groupTabs[tabPosition-1]);
                                    b.putString("uri", newUri.toString());
                                    b.putBoolean("newReminder", true);
                                    intent.putExtras(b);

                                    SharedPreferences prefs = MainActivity.this.getSharedPreferences("activityhandle", 0);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putInt("killapp", 0);
                                    editor.commit();
                                    startActivity(intent);
                                }

                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, getString(R.string.problem_text_detection), Toast.LENGTH_LONG).show();
                                    }
                                });
    }

    public String[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("groupstore", 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);
        return array;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
            //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ROOT);
            leaveAlive();
            startActivityForResult(intent, 6638);

        }
        return true;
    }

    private void runBootService(Class<?> serviceClass) {
        try {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return;
                }
            }
            Intent i = new Intent(this, serviceClass);
            startService(i);
        } catch (Exception ex) {
            Log.d("MainActivity", ex.getMessage());
        }
    }

    Adapter adapter;

    public AlarmFragment newAlarmFragmentInstance(int tabPosition, String tabName) {

        AlarmFragment f = new AlarmFragment();
        Bundle args = new Bundle();
        args.putInt("tabPosition", tabPosition);
        if (tabPosition != -1) args.putString("tabName", groupTabs[tabPosition]);
        else args.putString("tabName", getString(R.string.all_tab_name));
        f.setArguments(args);
        adapter.addFragment(f, tabName);
        return f;
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new Adapter(getSupportFragmentManager());

        groupTabs = loadArray("names", getApplicationContext());

        newAlarmFragmentInstance(-1, getString(R.string.all_tab_name));
        for (int i=0; i<groupTabs.length; i++)
        {
            newAlarmFragmentInstance(i, groupTabs[i]);
        }
        viewPager.setAdapter(adapter);
    }

    void selectPage(int pageIndex){
        mTabLayout.setScrollPosition(pageIndex,0f,true);
        mViewPager.setCurrentItem(pageIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intentSettings = new Intent(this, SettingsActivity.class);
            leaveAlive();
            startActivity(intentSettings);
            return true;
        }
        if (id == R.id.action_groups) {
            Intent intentGroup = new Intent(this, GroupsEditActivity.class);
            leaveAlive();
            startActivity(intentGroup);
            return true;
        }
        if (id == R.id.action_archived) {
            Intent intentGroup = new Intent(this, ArchivedEventsActivity.class);
            leaveAlive();
            startActivity(intentGroup);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        int id = item.getItemId();

        if (id == R.id.nav_cal) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_people) {
            drawer.closeDrawer(GravityCompat.START);
            navigationView.setCheckedItem(R.id.nav_cal);
            Intent intent = new Intent(getApplicationContext(), PeopleActivity.class);
            intent.addFlags(FLAG_ACTIVITY_LAUNCH_ADJACENT |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            leaveAlive();
            startActivity(intent);
            return false;
        } else if (id == R.id.nav_feedback) {
            Intent intent = new Intent(getApplicationContext(), FeedbackActivity.class);
            leaveAlive();
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_about) {
            DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
            float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

            if (dpHeight>=800) {
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.about_menu_1959);
                aboutView.setVisibility(View.VISIBLE);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            } else if (dpHeight>=772) {
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.about_menu_189);
                aboutView.setVisibility(View.VISIBLE);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            } else if (dpHeight>=683) {
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.about_menu);
                aboutView.setVisibility(View.VISIBLE);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle(getString(R.string.about));
                alertDialog.setMessage(getString(R.string.about_text1));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.close_window),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        } else if (id == R.id.nav_about_back) {
            aboutView.setVisibility(View.INVISIBLE);
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.activity_main2_drawer);
            navigationView.setCheckedItem(R.id.nav_cal);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }



        return true;
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}