package tk.onecal.onecal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class ArchivedPeopleActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    int tabPosition;
    private String alarmTitle = "";
    private String[] groupTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.archived_people));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
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

        setupViewPager(mViewPager);
        mTabLayout.setupWithViewPager(mViewPager);
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

    @Override
    protected void onResume() {
        super.onResume();
        int suretabposition = tabPosition;
        setupViewPager(mViewPager);
        selectPage(suretabposition);
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

    public ArchivedPeopleFragment newPeopleFragmentInstance(int tabPosition, String tabName) {

        ArchivedPeopleFragment f = new ArchivedPeopleFragment();
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

        newPeopleFragmentInstance(-1, getString(R.string.all_tab_name));
        for (int i=0; i<groupTabs.length; i++)
        {
            newPeopleFragmentInstance(i, groupTabs[i]);
        }
        viewPager.setAdapter(adapter);
    }

    void selectPage(int pageIndex){
        mTabLayout.setScrollPosition(pageIndex,0f,true);
        mViewPager.setCurrentItem(pageIndex);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
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
