package tk.onecal.onecal;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.viewpager.widget.ViewPager;
import tk.onecal.onecal.data.PeopleCustomContract;
import tk.onecal.onecal.data.PeopleCustomDbHelper;

public class TaggingContactActivity extends AppCompatActivity {
    String peopleSelected, groupName;
    Boolean contained=false;
    Adapter adapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_contact);

        final int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().setBackgroundDrawableResource(R.drawable.dark_bg_drawable);
        }

        Bundle extras = getIntent().getExtras();
        peopleSelected = extras.getString("people_tagged", null);
        groupName = extras.getString("group_name", null);

        SharedPreferences prefs = getSharedPreferences("people_tag_cursor", 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("people_tagged", peopleSelected);
        edit.commit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.tag_person));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_done);

        mViewPager = findViewById(R.id.viewpager);
        setupViewPager(mViewPager);
    }

    @Override
    public boolean onSupportNavigateUp() {
        String newPeopleSelection = "";
        String[] selectedContacts = loadArray("assignlist", getApplicationContext());
        deleteArray("assignlist", getApplicationContext());

        for (int i=0; i<selectedContacts.length; i++) {
            newPeopleSelection += selectedContacts[i] + " ";
        }

        ///Toast.makeText(this, newPeopleSelection, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent();
        intent.putExtra("new_people_tagged", newPeopleSelection);
        setResult(RESULT_OK, intent);
        finish();

        return true;
    }


    public String[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("contactgrouping", 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);
        return array;
    }

    public boolean deleteArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("contactgrouping", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName +"_size", 0);
        return editor.commit();
    }


    @Override
    public void onBackPressed() {
        deleteArray("assignlist", getApplicationContext());
        Toast.makeText(getApplicationContext(), getString(R.string.no_changes_assign_contact), Toast.LENGTH_LONG).show();
        super.onBackPressed();
    }

    public TaggingContactFragment newTaggingContactFragmentInstance() {

        TaggingContactFragment f = new TaggingContactFragment();
        Bundle args = new Bundle();
        args.putString("groupName", groupName);
        f.setArguments(args);
        adapter.addFragment(f, groupName);
        return f;
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new Adapter(getSupportFragmentManager());
        newTaggingContactFragmentInstance();
        viewPager.setAdapter(adapter);
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
