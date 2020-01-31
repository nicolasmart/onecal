package tk.onecal.onecal;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.biometrics.BiometricPrompt;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
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
import android.text.InputType;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

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

import tk.onecal.onecal.data.AlarmReminderContract;

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

        final int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if (currentNightMode==Configuration.UI_MODE_NIGHT_YES) animateStatusBar(Color.parseColor("#d73d31"), getResources().getColor(R.color.colorPrimaryDark));
                else animateStatusBar(Color.parseColor("#e94d42"), getResources().getColor(R.color.colorPrimaryDark));
            }
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            Window window3 = getWindow();

            @Override
            public void onDrawerSlide(final View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                if (slideOffset==0) {
                    currentOpenState=0;
                    newState=true;
                }
                else if (slideOffset==1) {
                    currentOpenState=1;
                    newState=true;
                }
                else if (newState==true) {
                    if (currentOpenState==0) {
                        if (currentNightMode==Configuration.UI_MODE_NIGHT_YES) animateStatusBar(getResources().getColor(R.color.colorPrimaryDark), Color.parseColor("#d73d31"));
                        else animateStatusBar(getResources().getColor(R.color.colorPrimaryDark), Color.parseColor("#e94d42"));
                    }
                    newState=false;
                }
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

        aboutView = findViewById(R.id.about_view);


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


    KeyStore keyStore;
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
                        "Exit",
                        this.getMainExecutor(),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                System.exit(1);
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
                System.exit(1);
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
                System.exit(1);
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
                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_GROUP, tabPosition-1);
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

        builder.show();
    }

    public String[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("groupstore", 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);
        return array;
    }

    Adapter adapter;

    public AlarmFragment newAlarmFragmentInstance(int tabPosition, String tabName) {

        AlarmFragment f = new AlarmFragment();
        Bundle args = new Bundle();
        args.putInt("tabPosition", tabPosition);
        if (tabPosition != -1) args.putString("tabName", groupTabs[tabPosition]);
        else args.putString("tabName", "All");
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


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
            drawer.closeDrawer(GravityCompat.START);
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