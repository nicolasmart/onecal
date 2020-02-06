package tk.onecal.onecal;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import tk.onecal.onecal.data.PeopleCustomContract;
import tk.onecal.onecal.data.PeopleCustomDbHelper;

public class ArchivedPeopleFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>    {

    int tabPos=0;
    String tabName;


    private Toolbar mToolbar;
    ArchivedPeopleCursorAdapter mCursorAdapter;
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

    /** TODO: Redesign the way this works; dynamically fetch actual number and e-mail - partly done
              Now we need to do the same for name too*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_people, container, false);

        peopleListView = (ListView) rootview.findViewById(R.id.list2);
        noContactsText = (TextView) rootview.findViewById(R.id.no_people_text);

        noContactsText.setText(getString(R.string.no_archived_contacts));

        View emptyView = rootview.findViewById(R.id.empty_view2);
        peopleListView.setEmptyView(emptyView);

        mCursorAdapter = new ArchivedPeopleCursorAdapter(getActivity(), null);

        peopleListView.setAdapter(mCursorAdapter);

        peopleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                TextView nameContact = view.findViewById(R.id.contact_name);
                TextView phoneNumber = view.findViewById(R.id.contact_phone_number);
                TextView contactId = view.findViewById(R.id.contact_invisible_id);

                final String name = nameContact.getText().toString();
                final String phone_number = phoneNumber.getText().toString();
                final String contact_id = contactId.getText().toString();

                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setMessage(getString(R.string.what_to_do_archived_contact));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.restore_archived_contact), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Intent.ACTION_INSERT);
                        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                        intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone_number);
                        startActivity(intent);
                        dialog.dismiss();
                    } });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getContext().getContentResolver().delete(ContentUris.withAppendedId(PeopleCustomContract.PeopleCustomEntry.CONTENT_URI, Long.parseLong(contact_id)), null, null);
                        dialog.dismiss();
                    }});
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }});

                alertDialog.show();

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
                PeopleCustomContract.PeopleCustomEntry.KEY_PHONE_NUMBER,
                PeopleCustomContract.PeopleCustomEntry.KEY_PHOTO_URI,
                PeopleCustomContract.PeopleCustomEntry.KEY_GROUP
        };

        if (tabPos==-1) return new CursorLoader(getActivity(),
                PeopleCustomContract.PeopleCustomEntry.CONTENT_URI,
                projection,
                null,
                null,
                PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME + " ASC");

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
                PeopleCustomContract.PeopleCustomEntry.KEY_CONTACT_ID,
                PeopleCustomContract.PeopleCustomEntry.KEY_PHONE_NUMBER,
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

            if (!cursor_contacts.moveToFirst()) {
                newCursor.addRow(new Object[]{cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)});
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
