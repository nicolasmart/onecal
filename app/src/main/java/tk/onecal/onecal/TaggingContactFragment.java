package tk.onecal.onecal;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import tk.onecal.onecal.data.PeopleCustomContract;

public class TaggingContactFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>    {

    private Toolbar mToolbar;
    TaggingContactCursorAdapter mCursorAdapter;
    ListView peopleListView;
    ProgressDialog prgDialog;
    String groupName;

    private String contactTitle = "";

    private static final int VEHICLE_LOADER = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        groupName = args.getString("groupName", null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_assign_contact, container, false);

        peopleListView = (ListView) rootview.findViewById(R.id.list_assign);

        View emptyView = rootview.findViewById(R.id.empty_view_assign);
        peopleListView.setEmptyView(emptyView);

        mCursorAdapter = new TaggingContactCursorAdapter(getActivity(), null);
        peopleListView.setAdapter(mCursorAdapter);

        peopleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                if (view != null) {
                    CheckBox checkBox = (CheckBox)view.findViewById(R.id.mark_contact);
                    checkBox.setChecked(!checkBox.isChecked());

                    TextView modifiedMark = view.findViewById(R.id.invisible_id);
                    modifiedMark.setText("true");

                }

            }
        });




        getLoaderManager().initLoader(VEHICLE_LOADER, null, this);

        return rootview;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                PeopleCustomContract.PeopleCustomEntry._ID,
                PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME,
                PeopleCustomContract.PeopleCustomEntry.KEY_CONTACT_ID,
                PeopleCustomContract.PeopleCustomEntry.KEY_PHOTO_URI,
                PeopleCustomContract.PeopleCustomEntry.KEY_GROUP
        };

        return new CursorLoader(getActivity(),
                PeopleCustomContract.PeopleCustomEntry.CONTENT_URI,
                projection,
                PeopleCustomContract.PeopleCustomEntry.KEY_GROUP + " LIKE \'%__,__" + groupName + "__,__%\'",
                null,
                PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME + " ASC");

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        String[] projection = {
                PeopleCustomContract.PeopleCustomEntry._ID,
                PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME,
                PeopleCustomContract.PeopleCustomEntry.KEY_CONTACT_ID,
                PeopleCustomContract.PeopleCustomEntry.KEY_PHOTO_URI,
                PeopleCustomContract.PeopleCustomEntry.KEY_GROUP
        };

        MatrixCursor newCursor = new MatrixCursor(projection);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            String contactId = cursor.getString(cursor.getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_CONTACT_ID));

            Cursor cursor_contacts = getContext().getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " LIKE \""
                            + contactId + "\"", null, null);

            if (cursor_contacts.moveToFirst()) {
                newCursor.addRow(new Object[]{cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)});
            }
            cursor_contacts.close();
            cursor.moveToNext();
        }

        mCursorAdapter.swapCursor(newCursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }


    public void restartLoader(){
        getLoaderManager().restartLoader(VEHICLE_LOADER, null, this);
    }
}
