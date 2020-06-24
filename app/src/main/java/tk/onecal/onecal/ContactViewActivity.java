package tk.onecal.onecal;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.wdullaer.materialdatetimepicker.Utils;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.viewpager.widget.ViewPager;
import tk.onecal.onecal.data.AlarmReminderContract;

import android.provider.ContactsContract;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ContactViewActivity extends AppCompatActivity {

    TextView contactName, contactPhone, contactEmail;
    String contactID, groupName;
    int tabPosition;
    Fragment eventFragment;
    String phone_number, email;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_view);

        final int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().setBackgroundDrawableResource(R.drawable.dark_bg_drawable);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle b = getIntent().getExtras();
        String name = b.getString("contact_name", "");
        groupName = b.getString("current_group", "");
        if (groupName.contains("null")) getSupportActionBar().setTitle("");
        else getSupportActionBar().setTitle(groupName);
        tabPosition = b.getInt("current_tab", 0);
        phone_number = b.getString("contact_phone_number", "");
        contactID = b.getString("contact_id", "");
        byte[] photo = b.getByteArray("contact_photo");
        if (photo != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(photo, 0, photo.length);
            ImageView image = (ImageView) findViewById(R.id.contact_photo_view);
            image.setImageBitmap(bmp);
        }

        contactName = findViewById(R.id.contact_name_view);
        contactName.setText(name);
        contactPhone = findViewById(R.id.contact_phone_number_view);
        contactPhone.setText(phone_number);

        email="";
        Cursor cursor_email = getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = "
                        + contactID, null, null);

        if (cursor_email.moveToFirst()) {
            int colIdx = cursor_email
                    .getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
            email = cursor_email.getString(colIdx);
        }
        cursor_email.close();
        if (email == null) email = "";

        contactEmail = findViewById(R.id.contact_email_view);
        contactEmail.setText(email);

        FloatingActionButton fab = findViewById(R.id.fab);
        if (groupName.contains("null")) fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addReminderTitleContact();
            }
        });

        Fragment eventFragment = newAlarmFragmentInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.event_fragment_frame, eventFragment).commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact_menu, menu);
        return true;
    }

    public void callContact(View v) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone_number, null));
        startActivity(intent);
    }

    public void smsContact(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phone_number, null));
        startActivity(intent);
    }

    public void mailContact(View v) {
        if (email.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_email_found), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null));
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactID);
            intent.setData(uri);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    String alarmTitle = "";

    public void addReminderTitleContact(){

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
                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_GROUP, groupName);
                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_IMPORTANCE_LEVEL, getString(R.string.light_importance));
                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO, "1");
                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE, getString(R.string.repeat_day));
                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_PEOPLE_TAGGED, contactID+" ");

                Uri newUri = getApplicationContext().getContentResolver().insert(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, values);

                if (newUri == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.create_event_fail), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.create_event_success), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ContactViewActivity.this, AddReminderActivity.class);
                    intent.setData(newUri);

                    Bundle b = new Bundle();
                    b.putString("tabName", groupName);
                    b.putString("uri", newUri.toString());
                    b.putBoolean("newReminder", true);
                    intent.putExtras(b);

                    SharedPreferences prefs = ContactViewActivity.this.getSharedPreferences("activityhandle", 0);
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

    public PersonAlarmFragment newAlarmFragmentInstance() {

        PersonAlarmFragment f = new PersonAlarmFragment();
        Bundle args = new Bundle();
        args.putString("tabName", groupName);
        args.putString("contact_id", contactID);
        f.setArguments(args);
        return f;
    }


}
