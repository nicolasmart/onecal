package tk.onecal.onecal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import tk.onecal.onecal.data.PeopleCustomContract;
import tk.onecal.onecal.data.PeopleCustomDbHelper;

public class TaggedPeopleViewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>    {

    int tabPos=0;
    String tabName;


    private Toolbar mToolbar;
    TaggedPeopleViewCursorAdapter mCursorAdapter;
    PeopleCustomDbHelper peopleCustomDbHelper = new PeopleCustomDbHelper(getActivity());
    ListView peopleListView;
    private TextView noContactsText;
    ProgressDialog prgDialog;

    private String peopleId = "";

    private static final int VEHICLE_LOADER = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        peopleId = args.getString("people_id", null);
    }

    /** TODO: Redesign the way this works; dynamically fetch actual number and e-mail - partly done
              Now we need to do the same for name too*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_people, container, false);

        peopleListView = (ListView) rootview.findViewById(R.id.list2);
        noContactsText = (TextView) rootview.findViewById(R.id.no_people_text);

        noContactsText.setText("");

        View emptyView = rootview.findViewById(R.id.empty_view2);
        peopleListView.setEmptyView(emptyView);

        mCursorAdapter = new TaggedPeopleViewCursorAdapter(getActivity(), null);

        peopleListView.setAdapter(mCursorAdapter);

        peopleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                byte[] photoByteArray;
                try {
                    ImageView photo = view.findViewById(R.id.photoview);
                    Bitmap bmp = ((BitmapDrawable) photo.getDrawable()).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    photoByteArray = stream.toByteArray();
                }
                catch (Exception ex) {
                    photoByteArray = null;
                }

                TextView name = view.findViewById(R.id.contact_name);
                TextView contact_id = view.findViewById(R.id.contact_invisible_id);
                TextView phone_number = view.findViewById(R.id.contact_phone_number);

                final String number = phone_number.getText().toString();

                Intent contactView = new Intent(getActivity(), ContactViewActivity.class);
                Bundle b = new Bundle();
                b.putString("contact_name", name.getText().toString());
                b.putString("contact_id", contact_id.getText().toString());
                b.putString("contact_phone_number", number);
                b.putString("current_group", "null");
                b.putInt("current_tab", 0);
                b.putByteArray("contact_photo", photoByteArray);
                contactView.putExtras(b);
                startActivity(contactView);
                return;
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
                null,
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
        mCursorAdapter.swapCursor(cursor);

        MatrixCursor newCursor = new MatrixCursor(projection);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            String contactId = cursor.getString(cursor.getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_CONTACT_ID));

            if (peopleId.contains(contactId)) {
                newCursor.addRow(new Object[]{cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)});
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
