package tk.onecal.onecal;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tk.onecal.onecal.data.PeopleCustomContract;

public class TaggingContactCursorAdapter extends CursorAdapter {

    private TextView mContactName, mContactPhoneNumber, mContactLabel, invisibleId;
    private ImageView mContactImage;
    private CheckBox box;
    private Context mContext;
    private int position;
    private boolean modified = false;

    List<Integer> selectedItemsPositions;
    List<String> idOfContacts;

    public TaggingContactCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 );
        selectedItemsPositions = new ArrayList<>();
        idOfContacts = new ArrayList<>();
    }
    @Override
    public View newView(final Context context, Cursor cursor, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.fragment_assign_single, parent, false);
        CheckBox box2 = (CheckBox) v.findViewById(R.id.mark_contact);
        box2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                position = (int) compoundButton.getTag();
                if (b) {
                    if (!selectedItemsPositions.contains(position)) {
                        selectedItemsPositions.add(position);
                    }
                    saveToArray(idOfContacts.get(position), "assignlist", context);
                } else {
                    try {
                        selectedItemsPositions.remove(position);
                        deleteFromArray(position, "assignlist", context);
                    } catch (Exception ex) {}
                }
            }
        });
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                box = (CheckBox) v.findViewById(R.id.mark_contact);
                box.setChecked(!box.isChecked());
                /**if (box.isChecked()) {
                    saveToArray(idOfContacts.get(position), "assignlist", context);
                }
                else {

                }*/

            }
        });
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        CheckBox box_bind = (CheckBox) view.findViewById(R.id.mark_contact);
        box_bind.setTag(cursor.getPosition());
        invisibleId = (TextView) view.findViewById(R.id.invisible_id);
        SharedPreferences prefs = context.getSharedPreferences("people_tag_cursor", 0);

        int nameColumnIndex = cursor.getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME);
        int photoColumnIndex = cursor.getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_PHOTO_URI);
        int labelColumnIndex = cursor.getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_GROUP);
        int idColumnIndex = cursor.getColumnIndex(PeopleCustomContract.PeopleCustomEntry.KEY_CONTACT_ID);

        String name = cursor.getString(nameColumnIndex);
        String label = cursor.getString(labelColumnIndex);
        String photoData = cursor.getString(photoColumnIndex);
        String id = cursor.getString(idColumnIndex);

        if (!idOfContacts.contains(id)) {
            idOfContacts.add(id);
        }

        /// TODO: Fix tagged people recovery
        if (selectedItemsPositions.contains(cursor.getPosition())/** || (invisibleId.getText()!="true" && prefs.getString("people_tagged", "").contains(id))*/) {
            box_bind.setChecked(true);
        }
        else {
            box_bind.setChecked(false);
        }


        mContactName = (TextView) view.findViewById(R.id.contact_name);
        mContactPhoneNumber = (TextView) view.findViewById(R.id.contact_phone_number);
        mContactLabel = (TextView) view.findViewById(R.id.contact_label);
        mContactImage = (ImageView) view.findViewById(R.id.photoview);

        mContext=context;

        mContactName.setText(name);

        Cursor cursor_phone = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
                        + id, null, null);

        if (cursor_phone.moveToFirst()) {
            int colIdx1 = cursor_phone
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int colIdx2 = cursor_phone
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            mContactName.setText(cursor_phone.getString(colIdx1));
            mContactPhoneNumber.setText(cursor_phone.getString(colIdx2));
        }
        cursor_phone.close();

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

    public void checkMark(View view)
    {
        if (view != null) {
            view=view.getRootView();
            CheckBox checkBox = (CheckBox)view.findViewById(R.id.mark_contact);
            checkBox.setChecked(!checkBox.isChecked());
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

    public boolean saveArray(String[] array, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("contactgrouping", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName +"_size", array.length);
        for(int i=0;i<array.length;i++)
            editor.putString(arrayName + "_" + i, array[i]);
        return editor.commit();
    }

    public String[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("contactgrouping", 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);
        return array;
    }

    public boolean deleteFromArray(int position, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("contactgrouping", 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size-1];
        int j=0;
        for(int i=0;i<size;i++) {
            if (i+1==position) continue;
            array[j] = prefs.getString(arrayName + "_" + i, null);
            j++;
        }
        return saveArray(array, arrayName, mContext);
    }

    public int saveToArray(String value, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("contactgrouping", 0);
        SharedPreferences.Editor editor = prefs.edit();
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[]=new String[size+1];
        for(int i=0;i<size;i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);
        array[size]=value;
        saveArray(array, arrayName, mContext);
        return size;
    }
}
