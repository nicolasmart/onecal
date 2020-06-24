package tk.onecal.onecal.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class AlarmReminderContract {

    private AlarmReminderContract() {}

    public static final String CONTENT_AUTHORITY = "tk.onecal.onecal";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_VEHICLE = "reminder-path";

    public static final class AlarmReminderEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VEHICLE);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLE;

        public final static String TABLE_NAME = "vehicles";

        public final static String _ID = BaseColumns._ID;

        public static final String KEY_TITLE = "title";
        public static final String KEY_DATE = "date";
        public static final String KEY_TIME = "time";
        public static final String KEY_REPEAT = "repeat";
        public static final String KEY_REPEAT_NO = "repeat_no";
        public static final String KEY_REPEAT_TYPE = "repeat_type";
        public static final String KEY_REPEAT_UNTIL = "repeat_until";
        public static final String KEY_ACTIVE = "active";
        public static final String KEY_GROUP = "group_name";
        public static final String KEY_IMPORTANCE_LEVEL = "importance_level";
        public static final String KEY_PEOPLE_TAGGED = "people_tagged";
        public static final String KEY_ARCHIVED = "archived";
        public static final String KEY_LOCATION = "location";
        public static final String KEY_LENGTH = "length";
        public static final String KEY_STARTS_AFTER = "starts_after";

        public static final String UPGRADE_2_TO_3_QUERY = "ALTER TABLE "
                + TABLE_NAME + " ADD COLUMN " + KEY_REPEAT_UNTIL + " string;";
        public static final String UPGRADE_3_TO_4_QUERY = "ALTER TABLE "
                + TABLE_NAME + " ADD COLUMN " + KEY_LOCATION + " string;";
        public static final String UPGRADE_4_TO_5_QUERY = "ALTER TABLE "
                + TABLE_NAME + " ADD COLUMN " + KEY_LENGTH + " string;";
        public static final String UPGRADE_5_TO_6_QUERY = "ALTER TABLE "
                + TABLE_NAME + " ADD COLUMN " + KEY_STARTS_AFTER + " string;";

    }

    public static String getColumnString(Cursor cursor, String columnName) {
        return cursor.getString( cursor.getColumnIndex(columnName) );
    }
}
