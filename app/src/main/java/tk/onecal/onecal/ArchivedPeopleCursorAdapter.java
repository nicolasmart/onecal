package tk.onecal.onecal;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import tk.onecal.onecal.data.PeopleCustomContract;

public class ArchivedPeopleCursorAdapter extends CursorAdapter {

    private TextView mContactName, mContactPhoneNumber, mContactLabel, mNewContactId;
    private ImageView mContactImage;

    private Context mContext;

    public ArchivedPeopleCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 );
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.fragment_archived_contacts, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int nameColumnIndex = cursor.getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME);
        int contactIdColumnIndex = cursor.getColumnIndex(PeopleCustomContract.PeopleCustomEntry._ID);
        int phoneNumberColumnIndex = cursor.getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_PHONE_NUMBER);
        int labelColumnIndex = cursor.getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_GROUP);

        String contactId = cursor.getString(contactIdColumnIndex);
        String name = cursor.getString(nameColumnIndex);
        String phoneNumber = cursor.getString(phoneNumberColumnIndex);

        mContactName = (TextView) view.findViewById(R.id.contact_name);
        mContactPhoneNumber = (TextView) view.findViewById(R.id.contact_phone_number);
        mNewContactId = (TextView) view.findViewById(R.id.contact_invisible_id);

        mContext=context;
        mContactName.setText(name);
        mContactPhoneNumber.setText(phoneNumber);
        mNewContactId.setText(contactId);

    }
}
