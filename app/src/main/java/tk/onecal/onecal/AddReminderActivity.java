package tk.onecal.onecal;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import tk.onecal.onecal.data.AlarmReminderContract;
import tk.onecal.onecal.reminder.AlarmScheduler;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.common.base.CharMatcher;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.wdullaer.materialdatetimepicker.Utils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddReminderActivity extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_VEHICLE_LOADER = 0;


    private boolean newTimeSet = false;
    private boolean newRepeatUntilSet = false;
    private boolean newDateSet = false;
    private boolean activeChanged = false;
    private Toolbar mToolbar;
    private EditText mTitleText, mLocationText;
    private View mRepeatUntilView, mLengthView, mDelayView;
    private TextView mDateText, mTimeText, mRepeatText, mRepeatNoText, mRepeatTypeText, mRepeatUntilText, mImportanceLevel, mDetailsText, mLengthText, mDelayText;
    private FloatingActionButton mFAB1;
    private FloatingActionButton mFAB2;
    private Calendar mCalendar;
    private int mYear, mMonth, mHour, mHourUntil, mMinute, mMinuteUntil, mDay;
    private long mRepeatTime;
    private Switch mRepeatSwitch;
    private String mTitle;
    private String mTime;
    private String mDate;
    private String mLocation;
    private String mLength;
    private String mDelay;
    private String mRepeat;
    private String mRepeatNo;
    private String mRepeatType;
    private String mRepeatUntil;
    private String mActive;
    private String mTabName;
    private String mPeopleSelected;

    private Uri mCurrentReminderUri;
    private boolean mVehicleHasChanged = false;
    private boolean mNewReminder;

    private static final String KEY_TITLE = "title_key";
    private static final String KEY_TIME = "time_key";
    private static final String KEY_DATE = "date_key";
    private static final String KEY_LOCATION = "location_key";
    private static final String KEY_LENGTH = "length_key";
    private static final String KEY_DELAY = "delay_key";
    private static final String KEY_REPEAT = "repeat_key";
    private static final String KEY_REPEAT_NO = "repeat_no_key";
    private static final String KEY_REPEAT_TYPE = "repeat_type_key";
    private static final String KEY_REPEAT_UNTIL = "repeat_until_key";
    private static final String KEY_ACTIVE = "active_key";
    private static final String KEY_IMPORTANCE_LEVEL = "importance_level_selected";
    private static final String KEY_PEOPLE_TAGGED = "people_selected";


    private static final long milMinute = 60000L;
    private static final long milHour = 3600000L;
    private static final long milDay = 86400000L;
    private static final long milWeek = 604800000L;
    private static final long milMonth = 2592000000L;
    private static final long milYear = 31556952000L;

    SharedPreferences appPrefs;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mVehicleHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        final int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().setBackgroundDrawableResource(R.drawable.dark_bg_drawable);
        }
        appPrefs = getApplicationContext().getSharedPreferences(SettingsActivity.SettingsFragment.SETTINGS_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);

        Bundle args = getIntent().getExtras();
        mTabName = args.getString("tabName", null);
        mNewReminder = args.getBoolean("newReminder", false);

        mCurrentReminderUri = Uri.parse(args.getString("uri", null));

        if (mCurrentReminderUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_reminder));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_reminder));
            getLoaderManager().initLoader(EXISTING_VEHICLE_LOADER, null, this);
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitleText = (EditText) findViewById(R.id.reminder_title);
        mLocationText = (EditText) findViewById(R.id.set_location);
        mDateText = (TextView) findViewById(R.id.set_date);
        mTimeText = (TextView) findViewById(R.id.set_time);
        mLengthView = findViewById(R.id.length);
        mLengthText = findViewById(R.id.set_length);
        mDelayView = findViewById(R.id.delayed_start);
        mDelayText = findViewById(R.id.set_delay);
        mRepeatText = (TextView) findViewById(R.id.set_repeat);
        mRepeatNoText = (TextView) findViewById(R.id.set_repeat_no);
        mRepeatTypeText = (TextView) findViewById(R.id.set_repeat_type);
        mRepeatUntilText = (TextView) findViewById(R.id.set_repeat_until);
        mRepeatUntilView = findViewById(R.id.repeat_until);
        mRepeatSwitch = (Switch) findViewById(R.id.repeat_switch);
        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
        mImportanceLevel = (TextView) findViewById(R.id.importance_level_selected);
        mDetailsText = (TextView) findViewById(R.id.details);

        if (mNewReminder == true) mActive = "false";
        else mActive = "true";
        mRepeat = "false";
        mRepeatNo = Integer.toString(1);
        mRepeatType = getString(R.string.repeat_day);
        mImportanceLevel.setText(getString(R.string.light_importance));
        mPeopleSelected = "";

        mCalendar = Calendar.getInstance();
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mHourUntil = mCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(Calendar.MINUTE);
        mMinuteUntil = mCalendar.get(Calendar.MINUTE);
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH) + 1;
        mDay = mCalendar.get(Calendar.DATE);

        mDate = mDay + "/" + mMonth + "/" + mYear;
        mTime = mHour + ":" + mMinute;
        mRepeatUntil = mHourUntil + " " + mMinuteUntil;

        mTitleText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTitle = s.toString().trim();
                mTitleText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        mLocationText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mLocation = s.toString().trim();
                mLocationText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mDateText.setText(mDate);
        mTimeText.setText(mTime);
        mRepeatNoText.setText(mRepeatNo);
        mRepeatTypeText.setText(mRepeatType);
        mRepeatText.setText(getString(R.string.every_repeat, mRepeatNo, mRepeatType));
        if (mRepeatType.contains(getString(R.string.repeat_minute)) || mRepeatType.contains(getString(R.string.repeat_hour))) mRepeatUntilView.setVisibility(View.VISIBLE);
        mRepeatUntilText.setText(mRepeatUntil);
        mDetailsText.setText(mTabName);
        mImportanceLevel.setText(getString(R.string.light_importance));

        if (savedInstanceState != null) {
            String savedTitle = savedInstanceState.getString(KEY_TITLE);
            mTitleText.setText(savedTitle);
            mTitle = savedTitle;

            String savedTime = savedInstanceState.getString(KEY_TIME);
            mTimeText.setText(savedTime);
            mTime = savedTime;

            String savedDate = savedInstanceState.getString(KEY_DATE);
            mDateText.setText(savedDate);
            mDate = savedDate;

            String savedLocation = savedInstanceState.getString(KEY_LOCATION);
            mLocationText.setText(savedLocation);
            mLocation = savedLocation;

            String savedLength = savedInstanceState.getString(KEY_LENGTH);
            mLengthText.setText(getNiceTime(savedLength));
            mLength = savedLength;

            String savedDelay = savedInstanceState.getString(KEY_DELAY);
            mDelayText.setText(getNiceTime(savedDelay));
            mDelay = savedDelay;

            String saveRepeat = savedInstanceState.getString(KEY_REPEAT);
            mRepeatText.setText(saveRepeat);
            mRepeat = saveRepeat;

            String savedRepeatNo = savedInstanceState.getString(KEY_REPEAT_NO);
            mRepeatNoText.setText(savedRepeatNo);
            mRepeatNo = savedRepeatNo;

            String savedRepeatType = savedInstanceState.getString(KEY_REPEAT_TYPE);
            mRepeatTypeText.setText(savedRepeatType);
            mRepeatType = savedRepeatType;

            String savedRepeatUntil = savedInstanceState.getString(KEY_REPEAT_UNTIL);
            mRepeatUntilText.setText(savedRepeatUntil);
            mRepeatUntil = savedRepeatUntil;

            String savedImportanceLevel = savedInstanceState.getString(KEY_IMPORTANCE_LEVEL);
            mImportanceLevel.setText(savedImportanceLevel);

            mDetailsText.setText(mTabName);

            mActive = savedInstanceState.getString(KEY_ACTIVE);
            mPeopleSelected = savedInstanceState.getString(KEY_PEOPLE_TAGGED);
            newTimeSet = savedInstanceState.getBoolean("time_changed");
            newRepeatUntilSet = savedInstanceState.getBoolean("repeat_until_changed");
            newDateSet = savedInstanceState.getBoolean("date_changed");
            activeChanged = savedInstanceState.getBoolean("active_changed");
        }

        if (mActive.equals("false")) {
            mFAB1.setVisibility(View.VISIBLE);
            mFAB2.setVisibility(View.GONE);

        } else if (mActive.equals("true")) {
            mFAB1.setVisibility(View.GONE);
            mFAB2.setVisibility(View.VISIBLE);
        }

        if (!appPrefs.getBoolean("smart_config", false)) {
            mLengthView.setVisibility(View.GONE);
        }
        if (!appPrefs.getBoolean("delayed_start", false)) {
            mDelayView.setVisibility(View.GONE);
        }

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(KEY_TITLE, mTitleText.getText());
        outState.putCharSequence(KEY_TIME, mTimeText.getText());
        outState.putCharSequence(KEY_DATE, mDateText.getText());
        outState.putCharSequence(KEY_LOCATION, mLocationText.getText());
        outState.putCharSequence(KEY_LENGTH, mLength);
        outState.putCharSequence(KEY_DELAY, mLength);
        outState.putCharSequence(KEY_REPEAT, mRepeatText.getText());
        outState.putCharSequence(KEY_REPEAT_NO, mRepeatNoText.getText());
        outState.putCharSequence(KEY_REPEAT_TYPE, mRepeatTypeText.getText());
        outState.putCharSequence(KEY_REPEAT_UNTIL, mRepeatUntilText.getText());
        outState.putCharSequence(KEY_ACTIVE, mActive);
        outState.putCharSequence(KEY_IMPORTANCE_LEVEL, mImportanceLevel.getText());
        outState.putCharSequence(KEY_PEOPLE_TAGGED, mPeopleSelected);
        outState.putBoolean("time_changed", newTimeSet);
        outState.putBoolean("repeat_until_changed", newTimeSet);
        outState.putBoolean("date_changed", newDateSet);
        outState.putBoolean("active_changed", activeChanged);
    }

    public void tagPeople(View v) {
        if(mCurrentReminderUri == null){
            return;
        }
        Intent intent = new Intent(this, TaggingContactActivity.class);
        Bundle b = new Bundle();
        b.putString("people_tagged", mPeopleSelected);
        b.putString("group_name", mTabName);
        intent.putExtras(b);

        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String returnString = data.getStringExtra("new_people_tagged");
                mPeopleSelected = returnString;
            }
        }
    }

    public int requestCode, get_time, get_time_duration;

    public void setTime(View v){
        requestCode = 1;
        if(mCurrentReminderUri == null){
            return;
        }
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
        );
        tpd.setThemeDark(false);
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    public void setRepeatUntil(View v){
        requestCode = 2;
        if(mCurrentReminderUri == null){
            return;
        }
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd1 = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
        );
        tpd1.setThemeDark(false);
        tpd1.show(getFragmentManager(), "Timepickerdialog");
    }

    public void setDate(View v){
        if(mCurrentReminderUri == null){
            return;
        }
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }

    public void setImportanceLevel(View v){
        if(mCurrentReminderUri == null){
            return;
        }

        final String[] listItems = {getString(R.string.light_importance), getString(R.string.medium_importance), getString(R.string.urgent_importance)};

        AlertDialog.Builder builder = new AlertDialog.Builder(AddReminderActivity.this);
        builder.setTitle(getString(R.string.choose_importance));


        int checkedItem = 0;
        if (mImportanceLevel.getText().toString()==getString(R.string.medium_importance)) checkedItem=1;
        else if (mImportanceLevel.getText().toString()==getString(R.string.urgent_importance)) checkedItem=2;

        builder.setSingleChoiceItems(listItems, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mImportanceLevel.setText(listItems[which]);
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        if (requestCode == 1)
        {
            mHour = hourOfDay;
            mMinute = minute;
            if (minute < 10) {
                mTime = hourOfDay + ":" + "0" + minute;
            } else {
                mTime = hourOfDay + ":" + minute;
            }
            mTimeText.setText(mTime);
            if (mNewReminder) {
                mActive = "true";
                mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
                mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
                mFAB1.setVisibility(View.GONE);
                mFAB2.setVisibility(View.VISIBLE);
            }
            mCalendar.set(Calendar.HOUR_OF_DAY, mHour);
            mCalendar.set(Calendar.MINUTE, mMinute);
            newTimeSet = true;
            if (!newDateSet) setDate(null);
        }
        else if (requestCode == 2)
        {
            mHourUntil = hourOfDay;
            mMinuteUntil = minute;
            if (minute < 10) {
                mRepeatUntil = hourOfDay + ":" + "0" + minute;
            } else {
                mRepeatUntil = hourOfDay + ":" + minute;
            }
            mRepeatUntilText.setText(mRepeatUntil);
            if (mNewReminder) {
                mActive = "true";
                mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
                mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
                mFAB1.setVisibility(View.GONE);
                mFAB2.setVisibility(View.VISIBLE);
            }
            newRepeatUntilSet = true;
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        monthOfYear ++;
        mDay = dayOfMonth;
        mMonth = monthOfYear;
        mYear = year;
        mDate = dayOfMonth + "/" + monthOfYear + "/" + year;
        mDateText.setText(mDate);
        mCalendar.set(Calendar.MONTH, mMonth-1);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, mDay);
        newDateSet = true;
        if (!newTimeSet) setTime(null);
    }

    public void setOrClearLocation(View v) {
        if (mLocation.isEmpty())
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, getString(R.string.location_permission_needed), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", this.getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, 789);
                }else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
                }
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    FusedLocationProviderClient fusedLocationClient;
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        double longitude = location.getLongitude();
                                        double latitude = location.getLatitude();
                                        mLocation = latitude + ", " + longitude;
                                        mLocationText.setText(mLocation);
                                    }
                                }
                            });
                }
                catch (Exception ex) {}
            }
        } else {
            mLocation = "";
            mLocationText.setText(mLocation);
        }
    }


    public void setLength(View view) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.set_length_title));

        TextInputLayout textInputLayout = new TextInputLayout(this);

        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(getString(R.string.set_length_summary));

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

                try {
                    Integer.parseInt(input.getText().toString());
                } catch (Exception ex) {
                    return;
                }

                mLength = input.getText().toString();
                mLengthText.setText(getNiceTime(mLength));
            }
        });
        builder.setNegativeButton(getString(R.string.create_event_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setNeutralButton(getString(R.string.disable_length), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mLength = "";
                mLengthText.setText(getString(R.string.length_not_set));
                Toast.makeText(getApplicationContext(), getString(R.string.will_ignore_length), Toast.LENGTH_LONG).show();
            }
        });

        builder.show();

    }

    public String getNiceTime(String minutes) {
        if (minutes == null) {
            mLength = "";
            return getString(R.string.length_not_set);
        }
        if (minutes.isEmpty()) {
            mLength = "";
            return getString(R.string.length_not_set);
        }
        int nminutes = Integer.parseInt(minutes);
        if (nminutes < 60) return minutes + " " + getString(R.string.minutes);
        if (nminutes % 60 == 0) return nminutes/60 + " " + getString(R.string.hours);
        return nminutes/60 + " " + getString(R.string.hours) + getString(R.string.separator_length) + nminutes%60 + " " + getString(R.string.minutes);
    }

    String titleOfPrevious = "";

    public boolean isSmartConfiggedAndBusy() {

        if (appPrefs.getBoolean("smart_config", false)) {
            try {
                Cursor cursor_active = getContentResolver().query(
                        AlarmReminderContract.AlarmReminderEntry.CONTENT_URI,
                        null,
                        AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE + " LIKE \""
                                + "true" + "\"", null, null);

                Calendar mCalendar0 = mCalendar;
                long selectedTimeStamp0b = mCalendar.getTimeInMillis();
                mCalendar0.add(Calendar.MINUTE, Integer.parseInt(mLength));
                long selectedTimeStamp0 = mCalendar.getTimeInMillis();

                cursor_active.moveToFirst();
                while (cursor_active.moveToNext()) {
                    if (!cursor_active.getString(cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_ARCHIVED)).contains("false")) continue;


                    Uri mCurrentReminderUri = ContentUris
                            .withAppendedId(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI,
                                    cursor_active.getInt(cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry._ID)));

                    int titleidx = cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE);
                    int timeidx = cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TIME);
                    int dateidx = cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_DATE);
                    int lengthidx = cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_LENGTH);
                    int repeatidx = cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT);
                    int repeatnoidx = cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO);
                    int repeattypeidx = cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE);
                    String mTitle1 = cursor_active.getString(titleidx);
                    String mTime1 = cursor_active.getString(timeidx);
                    String mDate1 = cursor_active.getString(dateidx);
                    String mLength1 = cursor_active.getString(lengthidx);
                    String mRepeatNo1 = cursor_active.getString(repeatnoidx);
                    String mRepeatType1 = cursor_active.getString(repeattypeidx);
                    String mRepeat1 = cursor_active.getString(repeatidx);
                    long mRepeatTime1;

                    String mNewTime1, mNewDate1;
                    if (mDate1.charAt(4) == '/' && mDate1.charAt(1) == '/')
                        mNewDate1 = "0" + CharMatcher.anyOf("/").removeFrom(mDate1);
                    else if (mDate1.charAt(4) == '/' && mDate1.charAt(2) == '/')
                        mNewDate1 = mDate1.substring(0, 2) + "0" + CharMatcher.anyOf("/").removeFrom(mDate1.substring(3));
                    else if (mDate1.charAt(3) == '/' && mDate1.charAt(1) == '/')
                        mNewDate1 = "0" + mDate1.charAt(0) + "0" + CharMatcher.anyOf("/").removeFrom(mDate1.substring(2));
                    else mNewDate1 = CharMatcher.anyOf("/").removeFrom(mDate1);
                    mNewDate1 = mNewDate1.substring(4, 8) + mNewDate1.substring(2, 4) + mNewDate1.substring(0, 2);
                    if (mTime1.charAt(1) == ':')
                        mNewTime1 = CharMatcher.anyOf(":").removeFrom("0" + mTime1);
                    else if (mTime1.length() == 4)
                        mNewTime1 = CharMatcher.anyOf(":").removeFrom(mTime1.charAt(0) + mTime1.charAt(1) + "0" + mTime1.charAt(2));
                    else mNewTime1 = CharMatcher.anyOf(":").removeFrom(mTime1);

                    if (mRepeatType1.equals(getString(R.string.repeat_minute))) {
                        mRepeatTime1 = Integer.parseInt(mRepeatNo1) * milMinute;
                        mRepeatType1 = "MINUTELY";
                    } else if (mRepeatType1.equals(getString(R.string.repeat_hour))) {
                        mRepeatTime1 = Integer.parseInt(mRepeatNo1) * milHour;
                        mRepeatType1 = "HOURLY";
                    } else if (mRepeatType1.equals(getString(R.string.repeat_day))) {
                        mRepeatTime1 = Integer.parseInt(mRepeatNo1) * milDay;
                        mRepeatType1 = "DAILY";
                    } else if (mRepeatType1.equals(getString(R.string.repeat_week))) {
                        mRepeatTime1 = Integer.parseInt(mRepeatNo1) * milWeek;
                        mRepeatType1 = "WEEKLY";
                    } else if (mRepeatType1.equals(getString(R.string.repeat_month))) {
                        mRepeatTime1 = Integer.parseInt(mRepeatNo1) * milMonth;
                        mRepeatType1 = "MONTHLY";
                    } else if (mRepeatType1.equals(getString(R.string.repeat_year))) {
                        mRepeatTime1 = Integer.parseInt(mRepeatNo1) * milYear;
                        mRepeatType1 = "YEARLY";
                    }

                    Calendar mCalendar1 = Calendar.getInstance();


                    Integer mMonth1 = Integer.parseInt(mNewDate1.substring(4, 6));
                    mCalendar1.set(Calendar.MONTH, mMonth1-1);
                    mCalendar1.set(Calendar.YEAR, Integer.parseInt(mNewDate1.substring(0, 4)));
                    mCalendar1.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mNewDate1.substring(6, 8)));
                    mCalendar1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(mNewTime1.substring(0, 2)));
                    mCalendar1.set(Calendar.MINUTE, Integer.parseInt(mNewTime1.substring(2, 4)));
                    mCalendar1.set(Calendar.SECOND, 0);

                    long selectedTimeStamp1b = mCalendar1.getTimeInMillis();
                    try {
                        mCalendar1.add(Calendar.MINUTE, Integer.parseInt(mLength1));
                    } catch (Exception ex1) {}
                    long selectedTimeStamp1 = mCalendar1.getTimeInMillis();

                    if (mRepeat1.equals("true")) {

                    } else {
                        if (selectedTimeStamp1 >= selectedTimeStamp0b && selectedTimeStamp1b <= selectedTimeStamp0) {
                            titleOfPrevious = mTitle1;
                            return true;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            return false;
        }
        return false;
    }

    public void setDelay(View view) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delayed_start_title))
               .setMessage(getString(R.string.set_delay_message));

        TextInputLayout textInputLayout = new TextInputLayout(this);

        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(getString(R.string.set_delay_summary));

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

                try {
                    Integer.parseInt(input.getText().toString());
                } catch (Exception ex) {
                    return;
                }

                mDelay = input.getText().toString();
                mDelayText.setText(getNiceTime(mDelay));
            }
        });
        builder.setNegativeButton(getString(R.string.create_event_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setNeutralButton(getString(R.string.disable_length), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDelay = "";
                mDelayText.setText(getString(R.string.length_not_set));
            }
        });

        builder.show();
    }

    public void selectFab1(View v) {
        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB1.setVisibility(View.GONE);
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
        mFAB2.setVisibility(View.VISIBLE);
        mActive = "true";
        activeChanged = true;
    }

    public void selectFab2(View v) {
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
        mFAB2.setVisibility(View.GONE);
        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB1.setVisibility(View.VISIBLE);
        mActive = "false";
        activeChanged = true;
    }

    public void onSwitchRepeat(View view) {
        boolean on = ((Switch) view).isChecked();
        if (on) {
            mRepeat = "true";
            mRepeatText.setText(getString(R.string.every_repeat, mRepeatNo, mRepeatType));
        } else {
            mRepeat = "false";
            mRepeatText.setText(R.string.repeat_off);
        }
    }

    public void aroundSwitchRepeat(View view) {
        boolean on = mRepeatSwitch.isChecked();
        if (!on) {
            mRepeat = "true";
            mRepeatText.setText(getString(R.string.every_repeat, mRepeatNo, mRepeatType));
            mRepeatSwitch.setChecked(true);
        } else {
            mRepeat = "false";
            mRepeatText.setText(R.string.repeat_off);
            mRepeatSwitch.setChecked(false);
        }
    }

    public void selectRepeatType(View v){
        final String[] items = new String[6];

        items[0] = getString(R.string.repeat_minute);
        items[1] = getString(R.string.repeat_hour);
        items[2] = getString(R.string.repeat_day);
        items[3] = getString(R.string.repeat_week);
        items[4] = getString(R.string.repeat_month);
        items[5] = getString(R.string.repeat_year);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_type));
        builder.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {

                mRepeatType = items[item];
                mRepeatTypeText.setText(mRepeatType);
                mRepeatText.setText(getString(R.string.every_repeat, mRepeatNo, mRepeatType));
                if (item < 2) mRepeatUntilView.setVisibility(View.VISIBLE);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void setRepeatNo(View v){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.repetitions_num));

        TextInputLayout textInputLayout = new TextInputLayout(this);

        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(getString(R.string.enter_num));

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

        alert.setView(container);
        alert.setPositiveButton(getString(R.string.create_event_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        if (input.getText().toString().length() == 0) {
                            mRepeatNo = Integer.toString(1);
                            mRepeatNoText.setText(mRepeatNo);
                            mRepeatText.setText(getString(R.string.every_repeat, mRepeatNo, mRepeatType));
                        }
                        else {
                            mRepeatNo = input.getText().toString().trim();
                            mRepeatNoText.setText(mRepeatNo);
                            mRepeatText.setText(getString(R.string.every_repeat, mRepeatNo, mRepeatType));
                        }
                    }
                });
        alert.setNegativeButton(getString(R.string.create_event_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_reminder, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentReminderUri == null) {
            MenuItem menuItem = menu.findItem(R.id.discard_reminder);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.save_reminder:
                if (mTitleText.getText().toString().length() == 0){
                    mTitleText.setError(getString(R.string.reminder_title_blank));
                }
                else if (isSmartConfiggedAndBusy()) {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.engaged_already))
                            .setMessage(getString(R.string.the_selected_time_sc) + titleOfPrevious)
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    saveReminder();
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }

                else {
                    saveReminder();
                    finish();
                }
                return true;
            case R.id.generate_qr:
                try {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
                        return false;
                    }

                    String mNewTime, mNewDate;
                    if (mDate.charAt(4) == '/' && mDate.charAt(1) == '/') mNewDate = "0" + CharMatcher.anyOf("/").removeFrom(mDate);
                    else if (mDate.charAt(4) == '/' && mDate.charAt(2) == '/') mNewDate = mDate.substring(0, 2) + "0" + CharMatcher.anyOf("/").removeFrom(mDate.substring(3));
                    else if (mDate.charAt(3) == '/' && mDate.charAt(1) == '/') mNewDate = "0" + mDate.charAt(0) + "0" + CharMatcher.anyOf("/").removeFrom(mDate.substring(2));
                    else mNewDate = CharMatcher.anyOf("/").removeFrom(mDate);
                    mNewDate = mNewDate.substring(4, 8) + mNewDate.substring(2, 4) + mNewDate.substring(0, 2);
                    if (mTime.charAt(1) == ':') mNewTime = CharMatcher.anyOf(":").removeFrom("0" + mTime);
                    else if (mTime.length() == 4) mNewTime = CharMatcher.anyOf(":").removeFrom(mTime.charAt(0) + mTime.charAt(1) + "0" + mTime.charAt(2));
                    else mNewTime = CharMatcher.anyOf(":").removeFrom(mTime);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap("BEGIN:VEVENT\n" +
                            "SUMMARY:" + mTitle + "\n" +
                            "DTSTART:" + mNewDate + "T" + mNewTime + "00Z\n" +
                            "END:VEVENT", BarcodeFormat.QR_CODE, 400, 400);

                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/OneCal/" + System.currentTimeMillis()/1000 + ".png";
                    FileOutputStream out = new FileOutputStream(filePath);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                    ContentValues values = new ContentValues();

                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.MediaColumns.DATA, filePath);

                    getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(filePath), "image/*");
                    startActivity(intent);
                    //ImageView imageViewQrCode = (ImageView) findViewById(R.id.qrCode);
                    //imageViewQrCode.setImageBitmap(bitmap);
                } catch(Exception e) {
                    Log.w("MainActivity", e.getMessage());
                }
                return true;
            case R.id.discard_reminder:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mVehicleHasChanged) {
                    onBackPressed();
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                onBackPressed();
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteReminder();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteReminder() {
        if (mCurrentReminderUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentReminderUri, null, null);

            new AlarmScheduler().cancelAlarm(getApplicationContext(), mCurrentReminderUri);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_reminder_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_reminder_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    public void saveReminder(){

        ContentValues values = new ContentValues();

        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE, mTitle);
        if (newDateSet) values.put(AlarmReminderContract.AlarmReminderEntry.KEY_DATE, mDate);
        if (newTimeSet) values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TIME, mTime);
        if (newRepeatUntilSet) values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_UNTIL, mRepeatUntil);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_LOCATION, mLocation);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_LENGTH, mLength);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_STARTS_AFTER, mDelay);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT, mRepeat);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO, mRepeatNo);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE, mRepeatType);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE, mActive);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_ARCHIVED, "false");
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_PEOPLE_TAGGED, mPeopleSelected);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_IMPORTANCE_LEVEL, mImportanceLevel.getText().toString());


        mCalendar.set(Calendar.MONTH, mMonth-1);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, mDay);
        mCalendar.set(Calendar.HOUR_OF_DAY, mHour);
        mCalendar.set(Calendar.MINUTE, mMinute);
        mCalendar.set(Calendar.SECOND, 0);

        if (!TextUtils.isEmpty(mDelay)) mCalendar.add(Calendar.MINUTE, -Integer.parseInt(mDelay));

        long selectedTimestamp =  mCalendar.getTimeInMillis();

        if (mRepeatType.equals(getString(R.string.repeat_minute))) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milMinute;
            mRepeatType = "MINUTELY";
        } else if (mRepeatType.equals(getString(R.string.repeat_hour))) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milHour;
            mRepeatType = "HOURLY";
        } else if (mRepeatType.equals(getString(R.string.repeat_day))) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milDay;
            mRepeatType = "DAILY";
        } else if (mRepeatType.equals(getString(R.string.repeat_week))) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milWeek;
            mRepeatType = "WEEKLY";
        } else if (mRepeatType.equals(getString(R.string.repeat_month))) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milMonth;
            mRepeatType = "MONTHLY";
        } else if (mRepeatType.equals(getString(R.string.repeat_year))) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milYear;
            mRepeatType = "YEARLY";
        }

        if (mCurrentReminderUri == null) {
            Uri newUri = getContentResolver().insert(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_reminder_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_reminder_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentReminderUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_reminder_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_reminder_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        if (mActive.equals("true")) {
            if (mRepeat.equals("true")) {
                if ((newTimeSet && newDateSet) || (mActive=="true" && activeChanged)) {
                    new AlarmScheduler().cancelAlarm(getApplicationContext(), mCurrentReminderUri);
                    new AlarmScheduler().setRepeatAlarm(getApplicationContext(), selectedTimestamp, mCurrentReminderUri, mRepeatTime);
                }
            } else if (mRepeat.equals("false")) {
                if ((newTimeSet && newDateSet) || (mActive=="true" && activeChanged)) {
                    new AlarmScheduler().cancelAlarm(getApplicationContext(), mCurrentReminderUri);
                    new AlarmScheduler().setAlarm(getApplicationContext(), selectedTimestamp, mCurrentReminderUri);
                    //Log.d("BootService", String.valueOf(selectedTimestamp) + " " + String.valueOf(mCurrentReminderUri) + " that's addreminder");
                }
            }
        }

        Toast.makeText(getApplicationContext(), getString(R.string.saved_to_onecal),
                Toast.LENGTH_SHORT).show();

        SharedPreferences preferences = this.getSharedPreferences(
                SettingsActivity.SettingsFragment.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        if (preferences.getBoolean("calendar_mode", false)) {
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setType("vnd.android.cursor.item/event");
            intent.putExtra(CalendarContract.Events.TITLE, mTitle);
            if (!mLocation.isEmpty()) intent.putExtra(CalendarContract.Events.EVENT_LOCATION, mLocation);
            if (mRepeat.equals("true")) intent.putExtra(CalendarContract.Events.RRULE, "FREQ="+mRepeatType+";INTERVAL="+mRepeatNo);
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                    selectedTimestamp);

            startActivity(intent);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }




    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                AlarmReminderContract.AlarmReminderEntry._ID,
                AlarmReminderContract.AlarmReminderEntry.KEY_TITLE,
                AlarmReminderContract.AlarmReminderEntry.KEY_DATE,
                AlarmReminderContract.AlarmReminderEntry.KEY_TIME,
                AlarmReminderContract.AlarmReminderEntry.KEY_LOCATION,
                AlarmReminderContract.AlarmReminderEntry.KEY_LENGTH,
                AlarmReminderContract.AlarmReminderEntry.KEY_STARTS_AFTER,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_UNTIL,
                AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE,
                AlarmReminderContract.AlarmReminderEntry.KEY_IMPORTANCE_LEVEL,
                AlarmReminderContract.AlarmReminderEntry.KEY_GROUP,
                AlarmReminderContract.AlarmReminderEntry.KEY_PEOPLE_TAGGED
        };

        return new CursorLoader(this,
                mCurrentReminderUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int titleColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE);
            int dateColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_DATE);
            int timeColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TIME);
            int locationColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_LOCATION);
            int delayColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_STARTS_AFTER);
            int lengthColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_LENGTH);
            int repeatColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT);
            int repeatNoColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO);
            int repeatTypeColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE);
            int repeatUntilColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_UNTIL);
            int importanceLevelColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_IMPORTANCE_LEVEL);
            int groupColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_GROUP);
            int peopleTaggedColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_PEOPLE_TAGGED);

            String title = cursor.getString(titleColumnIndex);
            String date = cursor.getString(dateColumnIndex);
            String time = cursor.getString(timeColumnIndex);
            String location = cursor.getString(locationColumnIndex);
            String delay = cursor.getString(delayColumnIndex);
            String length = cursor.getString(lengthColumnIndex);
            String repeat = cursor.getString(repeatColumnIndex);
            String repeatNo = cursor.getString(repeatNoColumnIndex);
            String repeatType = cursor.getString(repeatTypeColumnIndex);
            String repeatUntil = cursor.getString(repeatUntilColumnIndex);
            String importanceLevel = cursor.getString(importanceLevelColumnIndex);
            String group = cursor.getString(groupColumnIndex);
            String peopleTagged = cursor.getString(peopleTaggedColumnIndex);

            mDate = date;
            mTime = time;
            mRepeatUntil = repeatUntil;
            mPeopleSelected = peopleTagged;

            mTitleText.setText(title);
            mDateText.setText(date);
            mTimeText.setText(time);
            mLocationText.setText(location);
            mLength = length;
            mLengthText.setText(getNiceTime(length));
            mDelay = delay;
            mDelayText.setText(getNiceTime(delay));
            mRepeatNoText.setText(repeatNo);
            mRepeatTypeText.setText(repeatType);
            mRepeatUntilText.setText(repeatUntil);
            mRepeatText.setText(getString(R.string.every_repeat, mRepeatNo, mRepeatType));
            if (repeat == null){
                mRepeatSwitch.setChecked(false);
                mRepeatText.setText(R.string.repeat_off);
            }
            else if (repeat.equals("false")) {
                mRepeatSwitch.setChecked(false);
                mRepeatText.setText(R.string.repeat_off);

            } else if (repeat.equals("true")) {
                mRepeatSwitch.setChecked(true);
            }

            mDetailsText.setText(group);
            mTabName = group;

            mImportanceLevel.setText(importanceLevel);

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

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
