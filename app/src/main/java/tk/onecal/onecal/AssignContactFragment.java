package tk.onecal.onecal;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.HashSet;

public class AssignContactFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>    {

    int tabPos=0;
    String tabName;


    private Toolbar mToolbar;
    AssignContactCursorAdapter mCursorAdapter;
    ListView peopleListView;
    ProgressDialog prgDialog;

    private String contactTitle = "";

    private static final int VEHICLE_LOADER = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getContext().getSharedPreferences("contactgrouping", 0);
        tabName = prefs.getString("groupname", "");
        Log.d("AssignContactFragment", tabName);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_assign_contact, container, false);

        peopleListView = (ListView) rootview.findViewById(R.id.list_assign);

        View emptyView = rootview.findViewById(R.id.empty_view_assign);
        peopleListView.setEmptyView(emptyView);

        mCursorAdapter = new AssignContactCursorAdapter(getActivity(), null, tabName);
        peopleListView.setAdapter(mCursorAdapter);

        peopleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {






                if (view != null) {
                    CheckBox checkBox = (CheckBox)view.findViewById(R.id.mark_contact);
                    checkBox.setChecked(!checkBox.isChecked());

                }

            }
        });




        getLoaderManager().initLoader(VEHICLE_LOADER, null, this);

        return rootview;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
                ContactsContract.CommonDataKinds.Phone.LABEL
        };

        String[] mySelection = {"GROUP BY (data4)"};

        return new CursorLoader(getActivity(),
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " ASC, " + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        String[] projection = {
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
                ContactsContract.CommonDataKinds.Phone.LABEL
        };

        HashSet<String> normalizedNumbersAlreadyFound = new HashSet<>();
        MatrixCursor newCursor = new MatrixCursor(projection);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));

            if (normalizedNumbersAlreadyFound.add(phoneNumber))
            {
                newCursor.addRow(new Object[]{cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)});
            }

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
