package tk.onecal.onecal;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AssignContactCursorAdapter extends CursorAdapter {

    private TextView mContactName, mContactPhoneNumber, mContactLabel, invisibleId;
    private ImageView mContactImage;
    private CheckBox box;
    private Context mContext;
    private int position;
    private String groupName;

    List<Integer> selectedItemsPositions;
    List<String> idOfContacts;
    List<String> namesOfContacts;
    List<String> numbersOfContacts;
    List<String> photosOfContacts;
    List<String> labelOfContacts;
    List<String> deleteContact;

    public AssignContactCursorAdapter(Context context, Cursor c, String grpnm) {
        super(context, c, 0 );
        groupName=grpnm;
        selectedItemsPositions = new ArrayList<>();
        idOfContacts = new ArrayList<>();
        namesOfContacts = new ArrayList<>();
        numbersOfContacts = new ArrayList<>();
        photosOfContacts = new ArrayList<>();
        labelOfContacts = new ArrayList<>();
        deleteContact = new ArrayList<>();
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
                } else {
                    selectedItemsPositions.remove((Object) position);
                }
            }
        });
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                box = (CheckBox) v.findViewById(R.id.mark_contact);
                box.setChecked(!box.isChecked());
                if (box.isChecked()) {
                    TextView tv = v.findViewById(R.id.invisible_id);
                    if (deleteContact.get(position)!="-1") {
                        deleteContact.set(position, "0");
                        deleteFromArray(position, "assignlist", context);
                        deleteFromArray(position, "assignnames", context);
                        deleteFromArray(position, "assignnumbers", context);
                        deleteFromArray(position, "assignphotos", context);
                        deleteFromArray(position, "assigngroups", context);
                        deleteFromArray(position, "assigndelete", context);
                    } else {
                        saveToArray(idOfContacts.get(position), "assignlist", context);
                        saveToArray(namesOfContacts.get(position), "assignnames", context);
                        saveToArray(numbersOfContacts.get(position), "assignnumbers", context);
                        saveToArray(photosOfContacts.get(position), "assignphotos", context);
                        saveToArray(labelOfContacts.get(position), "assigngroups", context);
                        saveToArray(deleteContact.get(position), "assigndelete", context);
                    }
                }
                else {
                    if (deleteContact.get(position)!="-1") {
                        deleteContact.set(position, "1");
                        saveToArray(idOfContacts.get(position), "assignlist", context);
                        saveToArray(namesOfContacts.get(position), "assignnames", context);
                        saveToArray(numbersOfContacts.get(position), "assignnumbers", context);
                        saveToArray(photosOfContacts.get(position), "assignphotos", context);
                        saveToArray(labelOfContacts.get(position), "assigngroups", context);
                        saveToArray(deleteContact.get(position), "assigndelete", context);
                    } else {
                        deleteFromArray(position, "assignlist", context);
                        deleteFromArray(position, "assignnames", context);
                        deleteFromArray(position, "assignnumbers", context);
                        deleteFromArray(position, "assignphotos", context);
                        deleteFromArray(position, "assigngroups", context);
                        deleteFromArray(position, "assigndelete", context);
                    }
                }

            }
        });
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        CheckBox box_bind = (CheckBox) view.findViewById(R.id.mark_contact);
        box_bind.setTag(cursor.getPosition());

        if (selectedItemsPositions.contains(cursor.getPosition()))
            box_bind.setChecked(true);
        else
            box_bind.setChecked(false);

        int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);
        int phoneNumberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
        int photoColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI);
        int labelColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);
        int idColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID);

        String phoneNumber = cursor.getString(phoneNumberColumnIndex);
        String name = cursor.getString(nameColumnIndex);
        String label = cursor.getString(labelColumnIndex);
        String photoData = cursor.getString(photoColumnIndex);
        String id = cursor.getString(idColumnIndex);

        if (!idOfContacts.contains(id)) {
            idOfContacts.add(id);
            namesOfContacts.add(name);
            numbersOfContacts.add(phoneNumber);
            photosOfContacts.add(photoData);
            labelOfContacts.add(label);
            if (label != null) {
                if (label.contains("__,__" + groupName + "__,__")) {
                    box_bind.setChecked(true);
                    deleteContact.add("0");
                } else deleteContact.add("-1");
            } else deleteContact.add("-1");
        }

        mContactName = (TextView) view.findViewById(R.id.contact_name);
        mContactPhoneNumber = (TextView) view.findViewById(R.id.contact_phone_number);
        mContactLabel = (TextView) view.findViewById(R.id.contact_label);
        invisibleId = (TextView) view.findViewById(R.id.invisible_id);
        mContactImage = (ImageView) view.findViewById(R.id.photoview);

        mContext=context;

        mContactName.setText(name);
        mContactPhoneNumber.setText(phoneNumber);

        String displaylist="";
        try {
            String grlist[] = label.split("__,__");
            for (int i=1; i<grlist.length; i++) {
                displaylist += i<grlist.length-1 ? grlist[i] + "; " : grlist[i];
            }
        } catch (Exception ex) {

        }
        mContactLabel.setText(displaylist);

        invisibleId.setText(id);


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
            if (i==position) continue;
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
