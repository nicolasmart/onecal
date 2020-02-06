package tk.onecal.onecal;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
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

public class CustomPeopleCursorAdapter extends CursorAdapter {

    private TextView mContactName, mContactPhoneNumber, mContactLabel, mNewContactId;
    private ImageView mContactImage;

    private Context mContext;

    public CustomPeopleCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 );
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.fragment_contacts, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int nameColumnIndex = cursor.getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME);
        int contactIdColumnIndex = cursor.getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_CONTACT_ID);
        int photoColumnIndex = cursor.getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_PHOTO_URI);
        int labelColumnIndex = cursor.getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_GROUP);
        int idColumnIndex = cursor.getColumnIndex(PeopleCustomContract.PeopleCustomEntry._ID);

        String contactId = cursor.getString(contactIdColumnIndex);
        String name = cursor.getString(nameColumnIndex);
        String label = cursor.getString(labelColumnIndex);
        String photoData = cursor.getString(photoColumnIndex);
        String id = cursor.getString(idColumnIndex);

        mContactName = (TextView) view.findViewById(R.id.contact_name);
        mContactPhoneNumber = (TextView) view.findViewById(R.id.contact_phone_number);
        mContactLabel = (TextView) view.findViewById(R.id.contact_label);
        mNewContactId = (TextView) view.findViewById(R.id.contact_invisible_id);
        mContactImage = (ImageView) view.findViewById(R.id.photoview);

        mContext=context;

        mContactName.setText(name);

        Cursor cursor_phone = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
                        + contactId, null, null);

        if (cursor_phone.moveToFirst()) {
            int colIdx1 = cursor_phone
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int colIdx2 = cursor_phone
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            mContactName.setText(cursor_phone.getString(colIdx1));
            mContactPhoneNumber.setText(cursor_phone.getString(colIdx2));
        }
        cursor_phone.close();

        mNewContactId.setText(contactId);

        String displaylist="";
        try {
            String grlist[] = label.split("__,__");
            for (int i=1; i<grlist.length; i++) {
                displaylist += i<grlist.length-1 ? grlist[i] + "; " : grlist[i];
            }
        } catch (Exception ex) {

        }
        mContactLabel.setText(displaylist);


        if (photoData!=null) mContactImage.setImageBitmap(loadContactPhotoThumbnail(photoData));
        else
        {
            if ((mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
                mContactImage.setImageResource(R.drawable.ic_person_white_24dp);
            else mContactImage.setImageResource(R.drawable.ic_person_black_24dp);
        }


    }

    private Bitmap loadContactPhotoThumbnail(String photoData) {
        AssetFileDescriptor afd = null;
        try {
            Uri thumbUri;
                thumbUri = Uri.parse(photoData);
            afd = mContext.getContentResolver().
                    openAssetFileDescriptor(thumbUri, "r");
            FileDescriptor fileDescriptor = afd.getFileDescriptor();
            if (fileDescriptor != null) {
                return BitmapFactory.decodeFileDescriptor(
                        fileDescriptor, null, null);
            }
        } catch (FileNotFoundException e) {
        } finally {
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {}
            }
        }
        return null;
    }
}
