package tk.onecal.onecal.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class AlarmReminderDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "alarmreminder.db";

    private static final int DATABASE_VERSION = 6;

    public AlarmReminderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String SQL_CREATE_ALARM_TABLE =  "CREATE TABLE " + AlarmReminderContract.AlarmReminderEntry.TABLE_NAME + " ("
                + AlarmReminderContract.AlarmReminderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AlarmReminderContract.AlarmReminderEntry.KEY_TITLE + " TEXT, "
                + AlarmReminderContract.AlarmReminderEntry.KEY_DATE + " TEXT, "
                + AlarmReminderContract.AlarmReminderEntry.KEY_TIME + " TEXT, "
                + AlarmReminderContract.AlarmReminderEntry.KEY_LENGTH + " TEXT, "
                + AlarmReminderContract.AlarmReminderEntry.KEY_STARTS_AFTER + " TEXT, "
                + AlarmReminderContract.AlarmReminderEntry.KEY_LOCATION + " TEXT, "
                + AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT + " TEXT, "
                + AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO + " TEXT, "
                + AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE + " TEXT, "
                + AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_UNTIL + " TEXT, "
                + AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE + " TEXT, "
                + AlarmReminderContract.AlarmReminderEntry.KEY_IMPORTANCE_LEVEL + " TEXT, "
                + AlarmReminderContract.AlarmReminderEntry.KEY_PEOPLE_TAGGED + " TEXT, "
                + AlarmReminderContract.AlarmReminderEntry.KEY_ARCHIVED + " TEXT DEFAULT \"false\", "
                + AlarmReminderContract.AlarmReminderEntry.KEY_GROUP + " TEXT " +" );";
        sqLiteDatabase.execSQL(SQL_CREATE_ALARM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if (i<=2) sqLiteDatabase.execSQL(AlarmReminderContract.AlarmReminderEntry.UPGRADE_2_TO_3_QUERY);
        if (i<=3) sqLiteDatabase.execSQL(AlarmReminderContract.AlarmReminderEntry.UPGRADE_3_TO_4_QUERY);
        if (i<=4) sqLiteDatabase.execSQL(AlarmReminderContract.AlarmReminderEntry.UPGRADE_4_TO_5_QUERY);
        if (i<=5) sqLiteDatabase.execSQL(AlarmReminderContract.AlarmReminderEntry.UPGRADE_5_TO_6_QUERY);
    }
}
