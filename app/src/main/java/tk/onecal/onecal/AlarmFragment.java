package tk.onecal.onecal;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import tk.onecal.onecal.data.AlarmReminderContract;
import tk.onecal.onecal.data.AlarmReminderDbHelper;

public class AlarmFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>    {

    int tabPos=0;
    String tabName;


    private Toolbar mToolbar;
    AlarmCursorAdapter mCursorAdapter;
    AlarmReminderDbHelper alarmReminderDbHelper = new AlarmReminderDbHelper(getActivity());
    ListView reminderListView;
    private TextView noRemindersText;
    ProgressDialog prgDialog;

    private String alarmTitle = "";

    private static final int VEHICLE_LOADER = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        tabPos = args.getInt("tabPosition", 0);
        tabName = args.getString("tabName", null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_alarm, container, false);

        reminderListView = (ListView) rootview.findViewById(R.id.list);
        noRemindersText = (TextView) rootview.findViewById(R.id.no_reminder_text);

        if (tabPos==-1) noRemindersText.setText(getString(R.string.all_empty));

        View emptyView = rootview.findViewById(R.id.empty_view);
        reminderListView.setEmptyView(emptyView);

        mCursorAdapter = new AlarmCursorAdapter(getActivity(), null);
        reminderListView.setAdapter(mCursorAdapter);

        reminderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), AddReminderActivity.class);

                Uri currentVehicleUri = ContentUris.withAppendedId(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, id);

                intent.setData(currentVehicleUri);

                Bundle b = new Bundle();
                b.putString("tabName", tabName);
                b.putString("uri", ContentUris.withAppendedId(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, id).toString());
                intent.putExtras(b);

                SharedPreferences prefs = getActivity().getSharedPreferences("activityhandle", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("killapp", 0);
                editor.commit();
                startActivity(intent);

            }
        });




        getLoaderManager().initLoader(VEHICLE_LOADER, null, this);

        return rootview;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                AlarmReminderContract.AlarmReminderEntry._ID,
                AlarmReminderContract.AlarmReminderEntry.KEY_TITLE,
                AlarmReminderContract.AlarmReminderEntry.KEY_DATE,
                AlarmReminderContract.AlarmReminderEntry.KEY_TIME,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE,
                AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE,
                AlarmReminderContract.AlarmReminderEntry.KEY_IMPORTANCE_LEVEL,
                AlarmReminderContract.AlarmReminderEntry.KEY_GROUP

        };

        if (tabPos==-1) return new CursorLoader(getActivity(),
                AlarmReminderContract.AlarmReminderEntry.CONTENT_URI,
                projection,
                null,
                null,
                AlarmReminderContract.AlarmReminderEntry.KEY_DATE + " ASC, " + AlarmReminderContract.AlarmReminderEntry.KEY_TIME + " ASC");

        return new CursorLoader(getActivity(),
                AlarmReminderContract.AlarmReminderEntry.CONTENT_URI,
                projection,
                AlarmReminderContract.AlarmReminderEntry.KEY_GROUP + "=" + String.valueOf(tabPos),
                null,
                AlarmReminderContract.AlarmReminderEntry.KEY_DATE + " ASC, " + AlarmReminderContract.AlarmReminderEntry.KEY_TIME + " ASC");

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }


    public void restartLoader(){
        getLoaderManager().restartLoader(VEHICLE_LOADER, null, this);
    }
}
