package tk.onecal.onecal;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import tk.onecal.onecal.data.PeopleCustomContract;
import tk.onecal.onecal.data.PeopleCustomDbHelper;

public class AssignContactActivity extends AppCompatActivity {
    String groupName;
    Boolean contained=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_contact);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                groupName = null;
            } else {
                groupName = extras.getString("groupname");
            }
        } else {
            groupName = (String) savedInstanceState.getSerializable("groupname");
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.assign_contact_text));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_done);
    }

    public String[] mergeArray(String[] first, String[] second)
    {
        String[] both = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, both, first.length, second.length);
        return both;
    }

    public String removeGroup(String input, String group)
    {
        contained=true;
        if (input=="__,__"+group+"__,__") return null;
        contained=false;
        return convertArrayToString(removeItemFromArray(convertStringToArray(input), group));
    }


    public String addGroup(String input, String group)
    {
        if (input==null) return "__,__"+group+"__,__";
        String[] groupslist=convertStringToArray(input);
        String[] newlist = new String[groupslist.length+1];
        for (int i=0; i<groupslist.length; i++) {
            newlist[i]=groupslist[i];
        }
        newlist[groupslist.length]=group;
        return convertArrayToString(newlist);
    }

    @Override
    public boolean onSupportNavigateUp() {
        String[] grstr = new String[1];
        grstr[0]=groupName;
        String groupString;
        String[] selectedContacts = loadArray("assignlist", getApplicationContext());
        String[] selectedNames = loadArray("assignnames", getApplicationContext());
        String[] selectedNumbers = loadArray("assignnumbers", getApplicationContext());
        String[] selectedPhotos = loadArray("assignphotos", getApplicationContext());
        String[] selectedGroups = loadArray("assigngroups", getApplicationContext());
        String[] selectedAction = loadArray("assigndelete", getApplicationContext()); ///TODO: See what's this about
        deleteArray("assignlist", getApplicationContext());
        deleteArray("assignnames", getApplicationContext());
        deleteArray("assignnumbers", getApplicationContext());
        deleteArray("assignphotos", getApplicationContext());
        deleteArray("assigngroups", getApplicationContext());
        deleteArray("assigndelete", getApplicationContext());

        PeopleCustomDbHelper mydb = new PeopleCustomDbHelper(this);

        final ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        String where = ContactsContract.Data._ID + " = ? AND "
                + ContactsContract.Data.MIMETYPE + " = ?";
        for (int i=0; i<selectedContacts.length; i++) {
            String[] labelParams = new String[]{selectedContacts[i],
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};

            groupString=removeGroup(selectedGroups[i], groupName);
            if (contained == false) groupString=addGroup(selectedGroups[i], groupName);

            if (groupString.contains("null")) groupString="";

            ContentValues values = new ContentValues();

            values.put(PeopleCustomContract.PeopleCustomEntry.KEY_CONTACT_ID, selectedContacts[i]);
            values.put(PeopleCustomContract.PeopleCustomEntry.KEY_GROUP, groupString);
            values.put(PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME, selectedNames[i]);
            values.put(PeopleCustomContract.PeopleCustomEntry.KEY_PHONE_NUMBER, selectedNumbers[i]);
            values.put(PeopleCustomContract.PeopleCustomEntry.KEY_PHOTO_URI, selectedPhotos[i]);

            Uri currUri = null;
            try {
                currUri = Uri.parse(ContentUris.withAppendedId(PeopleCustomContract.PeopleCustomEntry.CONTENT_URI, Long.parseLong(mydb.getContactId(selectedContacts[i]))).toString());
            } catch (Exception ex) {

            }
            if (currUri == null) getContentResolver().insert(PeopleCustomContract.PeopleCustomEntry.CONTENT_URI, values);
            else getContentResolver().update(currUri, values, null, null);
        }
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        super.onBackPressed();
        return true;
    }


    public String[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("contactgrouping", 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);
        return array;
    }

    public boolean deleteArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("contactgrouping", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName +"_size", 0);
        return editor.commit();
    }


    @Override
    public void onBackPressed() {
        deleteArray("assignlist", getApplicationContext());
        deleteArray("assignnames", getApplicationContext());
        deleteArray("assignnumbers", getApplicationContext());
        deleteArray("assignphotos", getApplicationContext());
        Toast.makeText(getApplicationContext(), getString(R.string.no_changes_assign_contact), Toast.LENGTH_LONG).show();
        super.onBackPressed();
    }

    public static String strSeparator = "__,__";
    public static String convertArrayToString(String[] array){
        if (array == null) return null;
        String str = "__,__";
        for (int i = 0;i<array.length; i++) {
            str = str+array[i];
            str = str+strSeparator;
        }
        return str.replace("__,____,__", "__,__");
    }
    public static String[] convertStringToArray(String str){
        if (str==null) return null;
        String[] arr = str.split(strSeparator);
        return arr;
    }

    public String[] removeItemFromArray(String[] input, String item) {
        if (input == null) {
            return null;
        } else if (input.length <= 0) {
            return input;
        } else {
            try {
                String[] output = new String[input.length - 2];
                int count = 0;
                for (int i = 1; i < input.length; i++) {
                    if (!input[i].equals(item)) {
                        output[count++] = input[i];
                    } else {
                        contained = true;
                    }
                }
                return output;
            } catch (Exception ex) {
                return input;
            }
        }
    }

}
