package tk.onecal.onecal;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.wdullaer.materialdatetimepicker.Utils;

import java.util.Calendar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import tk.onecal.onecal.data.AlarmReminderContract;

public class TaggedPeopleViewActivity extends AppCompatActivity {

    String eventName, peopleId, eventUri;
    int tabPosition;
    FloatingActionButton fab;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tagged_view);

        final int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().setBackgroundDrawableResource(R.drawable.dark_bg_drawable);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle b = getIntent().getExtras();
        peopleId=b.getString("people_id", null);
        eventName=b.getString("event_name", null);
        eventUri=b.getString("event_uri", null);

        getSupportActionBar().setTitle(eventName);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TaggedPeopleViewActivity.this, AddReminderActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                intent.setData(Uri.parse(eventUri));

                Bundle b = new Bundle();
                b.putString("tabName", getString(R.string.all_tab_name));
                b.putString("uri", eventUri);
                intent.putExtras(b);

                startActivity(intent);
                finish();
            }
        });

        Fragment taggedFragment = newTaggedFragmentInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.tagged_fragment_frame, taggedFragment).commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tagged_menu, menu);
        return true;
    }

    @Override
    protected void onPause(){
        deleteArray("taggednumbers", getApplicationContext());
        deleteArray("taggedemails", getApplicationContext());
        super.onPause();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sms) {
            String[] selectedContacts = loadArray("taggednumbers", getApplicationContext());
            deleteArray("taggednumbers", getApplicationContext());
            StringBuilder uri = new StringBuilder("sms:");
            for (int i=0; i<selectedContacts.length; i++) {
                uri.append(selectedContacts[i]);
                uri.append(";");
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString()));
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_email) {
            String[] selectedContacts = loadArray("taggedemails", getApplicationContext());
            deleteArray("taggedemails", getApplicationContext());
            final Intent emailLauncher = new Intent(Intent.ACTION_SEND_MULTIPLE);
            emailLauncher.setType("message/rfc822");
            emailLauncher.putExtra(Intent.EXTRA_EMAIL, selectedContacts);
            emailLauncher.putExtra(Intent.EXTRA_SUBJECT, eventName);
            try {
                startActivity(emailLauncher);
            } catch(Exception ex) {

            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public TaggedPeopleViewFragment newTaggedFragmentInstance() {

        TaggedPeopleViewFragment f = new TaggedPeopleViewFragment();
        Bundle args = new Bundle();
        args.putString("people_id", peopleId);
        f.setArguments(args);
        return f;
    }

    public String[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("taggedview", 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);
        return array;
    }

    public boolean deleteArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("taggedview", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName +"_size", 0);
        return editor.commit();
    }


}
