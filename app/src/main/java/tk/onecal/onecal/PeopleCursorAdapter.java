package tk.onecal.onecal;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import android.provider.ContactsContract;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import tk.onecal.onecal.data.PeopleCustomContract;

public class PeopleCursorAdapter extends CursorAdapter {

    private TextView mContactName, mContactPhoneNumber, mContactLabel, mInvisibleId;
    private ImageView mContactImage;

    private Context mContext;

    public PeopleCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 );
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.fragment_contacts, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);
        int phoneNumberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        int photoColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI);
        int idColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);

        String phoneNumber = cursor.getString(phoneNumberColumnIndex);
        String name = cursor.getString(nameColumnIndex);
        String photoData = cursor.getString(photoColumnIndex);
        String id = cursor.getString(idColumnIndex);

        mContactName = (TextView) view.findViewById(R.id.contact_name);
        mContactPhoneNumber = (TextView) view.findViewById(R.id.contact_phone_number);
        mContactImage = (ImageView) view.findViewById(R.id.photoview);
        mContactLabel = (TextView) view.findViewById(R.id.contact_label);
        mInvisibleId = (TextView) view.findViewById(R.id.contact_invisible_id);
        mInvisibleId.setText(id);

        mContext=context;

        mContactName.setText(name);
        mContactPhoneNumber.setText(phoneNumber);

        String label=""; ///TODO: Do the same for AssignContact
        Cursor cursor_group = context.getContentResolver().query(
                PeopleCustomContract.PeopleCustomEntry.CONTENT_URI,
                null,
                PeopleCustomContract.PeopleCustomEntry.KEY_CONTACT_ID + " LIKE \""
                        + id + "\"", null, null);

        if (cursor_group.moveToFirst()) {
            int colIdx = cursor_group
                    .getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_GROUP);
            label = cursor_group.getString(colIdx);
        }
        cursor_group.close();

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
