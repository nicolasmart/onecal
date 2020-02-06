package tk.onecal.onecal.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class PeopleCustomContract {

    private PeopleCustomContract() {}

    public static final String CONTENT_AUTHORITY = "tk.onecal.onecal.people";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_VEHICLE = "people-path";

    public static final class PeopleCustomEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VEHICLE);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLE;

        public final static String TABLE_NAME = "vehicles";

        public final static String _ID = BaseColumns._ID;

        public static final String KEY_CONTACT_ID = "android_id";
        public static final String KEY_GROUP = "group_name";
        public static final String KEY_DISPLAY_NAME = "display_name";
        public static final String KEY_PHOTO_URI = "photo_uri";
        public static final String KEY_PHONE_NUMBER = "phone_number";
        public static final String KEY_EVENTS = "events";


    }

    public static String getColumnString(Cursor cursor, String columnName) {
        return cursor.getString( cursor.getColumnIndex(columnName) );
    }
}
