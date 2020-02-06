package tk.onecal.onecal.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PeopleCustomDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "customcontacts.db";

    private static final int DATABASE_VERSION = 2;

    public PeopleCustomDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_CONTACTS_TABLE =  "CREATE TABLE " + PeopleCustomContract.PeopleCustomEntry.TABLE_NAME + " ("
                + PeopleCustomContract.PeopleCustomEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PeopleCustomContract.PeopleCustomEntry.KEY_CONTACT_ID + " TEXT, "
                + PeopleCustomContract.PeopleCustomEntry.KEY_DISPLAY_NAME + " TEXT, "
                + PeopleCustomContract.PeopleCustomEntry.KEY_PHONE_NUMBER + " TEXT, "
                + PeopleCustomContract.PeopleCustomEntry.KEY_PHOTO_URI + " TEXT, "
                + PeopleCustomContract.PeopleCustomEntry.KEY_EVENTS + " TEXT, "
                + PeopleCustomContract.PeopleCustomEntry.KEY_GROUP + " TEXT " +" );";
        sqLiteDatabase.execSQL(SQL_CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public String getContactId(String androidId) {
        String out;
        SQLiteDatabase db = this.getReadableDatabase();
        String query="SELECT * FROM vehicles WHERE android_id = \"" + androidId + "\"";
        Cursor res =  db.rawQuery( query, null );
        if(res.getCount() <= 0){
            res.close();
            return null;
        }
        res.moveToFirst();
        out=res.getString(res.getColumnIndex(PeopleCustomContract.PeopleCustomEntry._ID));
        res.close();
        return out;
    }
}
