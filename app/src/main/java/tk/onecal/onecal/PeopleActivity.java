package tk.onecal.onecal;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.biometrics.BiometricPrompt;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.provider.ContactsContract;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT;
import static android.hardware.biometrics.BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS;

public class PeopleActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private ImageView authenticationCover;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private NavigationView navigationView;
    private FloatingActionButton mEditContacts;
    private AppBarLayout mAppBarLayout;
    private RelativeLayout aboutView;
    int tabPosition;
    DrawerLayout drawer;

    private String alarmTitle = "";
    private String[] groupTabs;

    private Integer currentOpenState = 0;
    private Boolean newState = true;

    private Window window3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(this, getString(R.string.contact_permission_needed), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", this.getPackageName(), null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, 789);
            }else {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 123);
            }
        } else {
            setContentView(R.layout.activity_people);

            mToolbar = (Toolbar) findViewById(R.id.toolbar2);
            mViewPager = (ViewPager) findViewById(R.id.viewpager2);
            authenticationCover = (ImageView) findViewById(R.id.authenticationHider);
            authenticationCover.bringToFront();

            mEditContacts = (FloatingActionButton) findViewById(R.id.fab2);

            mEditContacts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    leaveAlive();
                    switchToMainContactsApp();
                }
            });

            mTabLayout = (TabLayout) findViewById(R.id.tabs2);
            mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    tabPosition = tab.getPosition();
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
                    if (currentNightMode==Configuration.UI_MODE_NIGHT_YES) animateStatusBar(Color.parseColor("#D84136"), getResources().getColor(R.color.colorPrimaryDark));
                    else animateStatusBar(Color.parseColor("#EC554C"), getResources().getColor(R.color.colorPrimaryDark));
                }

                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                }

                @Override
                public void onDrawerSlide(final View drawerView, float slideOffset) {
                    super.onDrawerSlide(drawerView, slideOffset);

                    if (slideOffset == 0) {
                        currentOpenState = 0;
                        newState = true;
                    } else if (slideOffset == 1) {
                        currentOpenState = 1;
                        newState = true;
                    } else if (newState == true) {
                        if (currentOpenState == 0) {
                            if (currentNightMode==Configuration.UI_MODE_NIGHT_YES) animateStatusBar(getResources().getColor(R.color.colorPrimaryDark), Color.parseColor("#D84136"));
                            else animateStatusBar(getResources().getColor(R.color.colorPrimaryDark), Color.parseColor("#EC554C"));
                        }
                        newState = false;
                    }
                }
            };
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar2);

            setupViewPager(mViewPager);
            mTabLayout.setupWithViewPager(mViewPager);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                authenticationCover.setVisibility(View.VISIBLE);
                mTabLayout.setVisibility(View.INVISIBLE);
                mAppBarLayout.setOutlineProvider(null);
                fingerprintCheck();
            } else {
                authenticationCover.setVisibility(View.INVISIBLE);
                mTabLayout.setVisibility(View.VISIBLE);
            }

            navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_people);
            navigationView.setNavigationItemSelectedListener(this);

            View hView =  navigationView.getHeaderView(0);
            ImageView header = hView.findViewById(R.id.nav_header);
            if (Locale.getDefault().getLanguage().contains("en")) header.setImageResource(R.drawable.onecal_header);

            aboutView = findViewById(R.id.about_view);
        }
    }

    void leaveAlive()
    {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("activityhandle", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("killapp", 0);
        editor.commit();
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

    void switchToMainContactsApp()
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);
        startActivity(intent);
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
        try {
            int suretabposition = tabPosition;
            setupViewPager(mViewPager);
            selectPage(suretabposition);
        } catch (Exception ex) {

        }
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

    public PeopleFragment newPeopleFragmentInstance(int tabPosition, String tabName) {

        PeopleFragment f = new PeopleFragment();
        Bundle args = new Bundle();
        args.putInt("tabPosition", tabPosition);
        if (tabPosition != -1) args.putString("tabName", groupTabs[tabPosition]);
        else args.putString("tabName", "All");
        f.setArguments(args);
        adapter.addFragment(f, tabName);
        return f;
    }

    public CustomPeopleFragment newCustomPeopleFragmentInstance(int tabPosition, String tabName) {

        CustomPeopleFragment f = new CustomPeopleFragment();
        Bundle args = new Bundle();
        args.putInt("tabPosition", tabPosition);
        if (tabPosition != -1) args.putString("tabName", groupTabs[tabPosition]);
        else args.putString("tabName", "All");
        f.setArguments(args);
        adapter.addFragment(f, tabName);
        return f;
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new PeopleActivity.Adapter(getSupportFragmentManager());

        groupTabs = loadArray("names", getApplicationContext());

        newPeopleFragmentInstance(-1, getString(R.string.all_tab_name));
        for (int i=0; i<groupTabs.length; i++)
        {
            newCustomPeopleFragmentInstance(i, groupTabs[i]);
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
        getMenuInflater().inflate(R.menu.main2_people, menu);
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
            startActivity(intentGroup);
            leaveAlive();
            return true;
        }
        if (id == R.id.action_archived_contacts) {
            Intent intentGroup = new Intent(this, ArchivedPeopleActivity.class);
            startActivity(intentGroup);
            leaveAlive();
            return true;
        }
        if (id == R.id.action_assign_contact) {
            Intent intentGroup = new Intent(this, AssignContactActivity.class);
            if (tabPosition==0) {
                Toast.makeText(this, getString(R.string.select_group_first), Toast.LENGTH_LONG).show();
                return false;
            }
            SharedPreferences prefs = getApplicationContext().getSharedPreferences("contactgrouping", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("groupname", groupTabs[tabPosition-1]);
            editor.commit();
            intentGroup.putExtra("groupname", groupTabs[tabPosition-1]);
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
            navigationView.setCheckedItem(R.id.nav_people);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(FLAG_ACTIVITY_LAUNCH_ADJACENT |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            leaveAlive();
            startActivity(intent);
            return false;
        } else if (id == R.id.nav_people) {
            drawer.closeDrawer(GravityCompat.START);
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
                AlertDialog alertDialog = new AlertDialog.Builder(PeopleActivity.this).create();
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
            navigationView.setCheckedItem(R.id.nav_people);
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

    KeyStore keyStore;
    private BiometricPrompt mBiometricPrompt;
    private CancellationSignal mCancellationSignal;
    private BiometricPrompt.AuthenticationCallback mAuthenticationCallback;
    private Signature mSignature;
    private String mToBeSignedMessage;
    private static final String DEFAULT_KEY_NAME = "default_key";

    @RequiresApi(Build.VERSION_CODES.P)
    public void fingerprintCheck() {
        SharedPreferences preferences = this.getSharedPreferences(
                SettingsActivity.SettingsFragment.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        if (!preferences.getBoolean("people_fingerprint", false)) {
            authenticationCover.setVisibility(View.INVISIBLE);
            mTabLayout.setVisibility(View.VISIBLE);
            return;
        }

        mBiometricPrompt = new BiometricPrompt.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setSubtitle(getString(R.string.people_fingerprint_subtitle))
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

}
