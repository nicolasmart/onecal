package tk.onecal.onecal;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import tk.onecal.onecal.data.AlarmReminderContract;
import tk.onecal.onecal.data.AlarmReminderDbHelper;

public class PersonAlarmFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>    {

    String personId;


    private Toolbar mToolbar;
    AlarmCursorAdapter mCursorAdapter;
    AlarmReminderDbHelper alarmReminderDbHelper = new AlarmReminderDbHelper(getActivity());
    ListView reminderListView;
    private TextView noRemindersText;
    ProgressDialog prgDialog;

    String groupName;

    private static final int VEHICLE_LOADER = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        personId = args.getString("contact_id", null);
        groupName = args.getString("tabName", null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_alarm, container, false);

        reminderListView = (ListView) rootview.findViewById(R.id.list);
        noRemindersText = (TextView) rootview.findViewById(R.id.no_reminder_text);

        noRemindersText.setText(getString(R.string.no_events_with_this_person));

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
                b.putString("tabName", groupName);
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
                AlarmReminderContract.AlarmReminderEntry.KEY_GROUP,
                AlarmReminderContract.AlarmReminderEntry.KEY_PEOPLE_TAGGED,
                AlarmReminderContract.AlarmReminderEntry.KEY_ARCHIVED

        };

        return new CursorLoader(getActivity(),
                AlarmReminderContract.AlarmReminderEntry.CONTENT_URI,
                projection,
                AlarmReminderContract.AlarmReminderEntry.KEY_PEOPLE_TAGGED + " LIKE \"%" + personId + "%\" AND " + AlarmReminderContract.AlarmReminderEntry.KEY_ARCHIVED + " LIKE \"%false%\"",
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
