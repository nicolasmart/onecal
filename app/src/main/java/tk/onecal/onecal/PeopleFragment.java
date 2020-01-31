package tk.onecal.onecal;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import android.provider.ContactsContract;

import java.util.HashSet;

import tk.onecal.onecal.data.PeopleCustomContract;
import tk.onecal.onecal.data.PeopleCustomDbHelper;

public class PeopleFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>    {

    int tabPos=0;
    String tabName;


    private Toolbar mToolbar;
    PeopleCursorAdapter mCursorAdapter;
    CustomPeopleCursorAdapter mCursorAdapter2;
    PeopleCustomDbHelper peopleCustomDbHelper = new PeopleCustomDbHelper(getActivity());
    ListView peopleListView;
    private TextView noContactsText;
    ProgressDialog prgDialog;

    private String contactTitle = "";

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
        View rootview = inflater.inflate(R.layout.fragment_people, container, false);

        peopleListView = (ListView) rootview.findViewById(R.id.list2);
        noContactsText = (TextView) rootview.findViewById(R.id.no_people_text);

        if (tabPos==-1) noContactsText.setText(getString(R.string.no_people_found));
        else noContactsText.setText(getString(R.string.no_contacts));

        View emptyView = rootview.findViewById(R.id.empty_view2);
        peopleListView.setEmptyView(emptyView);

        mCursorAdapter = new PeopleCursorAdapter(getActivity(), null);
        mCursorAdapter2 = new CustomPeopleCursorAdapter(getActivity(), null);

        if (tabPos==-1) peopleListView.setAdapter(mCursorAdapter);
        else peopleListView.setAdapter(mCursorAdapter2);

        peopleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

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
        String[] projection2 = {
                PeopleCustomContract.PeopleCustomEntry._ID,
                PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME,
                PeopleCustomContract.PeopleCustomEntry.KEY_PHONE_NUMBER,
                PeopleCustomContract.PeopleCustomEntry.KEY_PHOTO_URI,
                PeopleCustomContract.PeopleCustomEntry.KEY_GROUP
        };

        if (tabPos==-1) return new CursorLoader(getActivity(),
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,   
                projection,             
                null,                   
                null,                   
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " ASC, " + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " ASC");                  

        else return new CursorLoader(getActivity(),   
                PeopleCustomContract.PeopleCustomEntry.CONTENT_URI,   
                projection2,             
                PeopleCustomContract.PeopleCustomEntry.KEY_GROUP + "= \'" + tabName + "\'",                   
                
                null,                   
                PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME + " ASC");                  

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (tabPos==-1) {
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

            while (!cursor.isAfterLast()) {
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));

                if (normalizedNumbersAlreadyFound.add(phoneNumber)) {
                    newCursor.addRow(new Object[]{cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)});
                }

                cursor.moveToNext();
            }

            mCursorAdapter.swapCursor(newCursor);
        }
        else mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }


    public void restartLoader(){
        getLoaderManager().restartLoader(VEHICLE_LOADER, null, this);
    }
}
