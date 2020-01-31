package tk.onecal.onecal;

import android.app.ProgressDialog;
import android.database.Cursor;
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

import tk.onecal.onecal.data.PeopleCustomContract;
import tk.onecal.onecal.data.PeopleCustomDbHelper;

public class CustomPeopleFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>    {

    int tabPos=0;
    String tabName;


    private Toolbar mToolbar;
    CustomPeopleCursorAdapter mCursorAdapter;
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

        noContactsText.setText(getString(R.string.no_contacts));

        View emptyView = rootview.findViewById(R.id.empty_view2);
        peopleListView.setEmptyView(emptyView);

        mCursorAdapter = new CustomPeopleCursorAdapter(getActivity(), null);

        peopleListView.setAdapter(mCursorAdapter);

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
                PeopleCustomContract.PeopleCustomEntry._ID,
                PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME,
                PeopleCustomContract.PeopleCustomEntry.KEY_PHONE_NUMBER,
                PeopleCustomContract.PeopleCustomEntry.KEY_PHOTO_URI,
                PeopleCustomContract.PeopleCustomEntry.KEY_GROUP
        };

        return new CursorLoader(getActivity(),
                PeopleCustomContract.PeopleCustomEntry.CONTENT_URI,
                projection,
                PeopleCustomContract.PeopleCustomEntry.KEY_GROUP + " LIKE \'%__,__" + tabName + "__,__%\'",
                null,
                PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME + " ASC");

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        String[] projection = {
                PeopleCustomContract.PeopleCustomEntry._ID,
                PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME,
                PeopleCustomContract.PeopleCustomEntry.KEY_PHONE_NUMBER,
                PeopleCustomContract.PeopleCustomEntry.KEY_PHOTO_URI,
                PeopleCustomContract.PeopleCustomEntry.KEY_GROUP
        };


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
