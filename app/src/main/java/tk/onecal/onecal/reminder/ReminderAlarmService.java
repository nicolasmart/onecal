package tk.onecal.onecal.reminder;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;

import org.w3c.dom.Text;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import tk.onecal.onecal.AddReminderActivity;
import tk.onecal.onecal.AnnoyingAlarmActivity;
import tk.onecal.onecal.MainActivity;
import tk.onecal.onecal.SettingsActivity;
import tk.onecal.onecal.data.AlarmReminderContract;

import tk.onecal.onecal.R;

public class ReminderAlarmService extends IntentService {
    private static final String TAG = ReminderAlarmService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 42;
    public static PendingIntent getReminderPendingIntent(Context context, Uri uri) {
        Intent action = new Intent(context, ReminderAlarmService.class);
        action.setData(uri);
        return PendingIntent.getService(context, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public ReminderAlarmService() {
        super(TAG);
    }

    int notificationId;

    public void notification(String title, String message, Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationId = createID();
        String channelId = "event-id";
        String channelName = getString(R.string.events);
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_newcal)
                .setContentTitle(title)
                .setContentText(message)
                .setVibrate(new long[]{100, 250})
                .setLights(Color.YELLOW, 500, 5000)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary));

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(new Intent(context, MainActivity.class));
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(notificationId, mBuilder.build());
    }

    public int createID() {
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.FRENCH).format(now));
        return id;
    }

    public String[] loadArray2(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("groupstore", 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);
        return array;
    }

    public boolean saveArray(String[] array, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("notificationstore", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName +"_size", array.length);
        for(int i=0;i<array.length;i++)
            editor.putString(arrayName + "_" + i, array[i]);
        return editor.commit();
    }

    public String[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("notificationstore", 0);
        int size = prefs.getInt(arrayName + "_size", 0) + 1;
        String array[] = new String[size];
        for(int i=0;i<size-1;i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);
        return array;
    }

    public boolean deleteFromArray(int position, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("notificationstore", 0);
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


    @Override
    protected void onHandleIntent(Intent intent) {

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Uri uri = intent.getData();

        Intent action = new Intent(this, AddReminderActivity.class);
        action.setData(uri);
        PendingIntent operation = TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(action)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        SharedPreferences appPrefs = getApplicationContext().getSharedPreferences(SettingsActivity.SettingsFragment.SETTINGS_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);

        String description = "";
        String importanceLevel = "";
        String group = "";
        String id = "";
        String repeatType = "";
        String location = "";
        String delay = "";

        try {
            if (cursor != null && cursor.moveToFirst()) {
                description = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_TITLE);
                importanceLevel = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_IMPORTANCE_LEVEL);
                group = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_GROUP);
                id = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry._ID);
                repeatType = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE);
                location = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_LOCATION);
                delay = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_STARTS_AFTER);

                if (AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT).contains("false")) {
                    try {
                        ContentValues values = new ContentValues();
                        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_ARCHIVED, "true");
                        Uri currUri = Uri.parse(ContentUris.withAppendedId(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, Integer.valueOf(id)).toString());
                        getContentResolver().update(currUri, values, null, null);

                        notification(description, importanceLevel + " (" + group + ")", getApplicationContext());

                        if (appPrefs.getBoolean("annoying_notifications", true)) {
                            Intent annoyingNotification = new Intent(this, AnnoyingAlarmActivity.class);
                            annoyingNotification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(annoyingNotification);
                        }

                        String[] eventTitles = loadArray("name", getApplicationContext());
                        String[] eventImportances = loadArray("importance", getApplicationContext());
                        String[] eventGroups = loadArray("group", getApplicationContext());
                        String[] eventLocations = loadArray("location", getApplicationContext());
                        String[] eventNotifyId = loadArray("notification", getApplicationContext());
                        String[] eventDelays = loadArray("delay", getApplicationContext());

                        eventTitles[eventTitles.length - 1] = description;
                        eventImportances[eventImportances.length - 1] = importanceLevel;
                        eventLocations[eventLocations.length - 1] = location;
                        eventGroups[eventGroups.length - 1] = group;
                        eventNotifyId[eventNotifyId.length - 1] = String.valueOf(notificationId);
                        eventDelays[eventDelays.length - 1] = delay;

                        saveArray(eventTitles, "name", getApplicationContext());
                        saveArray(eventImportances, "importance", getApplicationContext());
                        saveArray(eventGroups, "group", getApplicationContext());
                        saveArray(eventLocations, "location", getApplicationContext());
                        saveArray(eventNotifyId, "notification", getApplicationContext());
                        saveArray(eventDelays, "delay", getApplicationContext());
                    } catch (Exception ex) {

                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        ///TODO: Not add already existing titles, and also prevent old non-repeated reminders from appearing (the last one I'm not sure about)

        if (repeatType.contains(getString(R.string.repeat_minute)) || repeatType.contains(getString(R.string.repeat_hour)))
        {
            long time_now = System.currentTimeMillis(), time_duration;
            time_now = time_now/60000;
            String time_dur = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_UNTIL);
            time_duration = Integer.parseInt(time_dur.substring(0, 2))*60 + Integer.parseInt(time_dur.substring(3));


            if (time_duration>=time_now) {
                notification(description, importanceLevel + " (" + group + ")", getApplicationContext());

                String[] eventTitles = loadArray("name", getApplicationContext());
                String[] eventImportances = loadArray("importance", getApplicationContext());
                String[] eventGroups = loadArray("group", getApplicationContext());
                String[] eventLocations = loadArray("location", getApplicationContext());
                String[] eventNotifyId = loadArray("notification", getApplicationContext());
                String[] eventDelays = loadArray("delay", getApplicationContext());

                eventTitles[eventTitles.length - 1] = description;
                eventImportances[eventImportances.length - 1] = importanceLevel;
                eventLocations[eventLocations.length - 1] = location;
                eventGroups[eventGroups.length - 1] = group;
                eventNotifyId[eventNotifyId.length - 1] = String.valueOf(notificationId);
                eventDelays[eventDelays.length - 1] = delay;

                saveArray(eventTitles, "name", getApplicationContext());
                saveArray(eventImportances, "importance", getApplicationContext());
                saveArray(eventGroups, "group", getApplicationContext());
                saveArray(eventLocations, "location", getApplicationContext());
                saveArray(eventNotifyId, "notification", getApplicationContext());
                saveArray(eventDelays, "delay", getApplicationContext());

                if (appPrefs.getBoolean("annoying_notifications", true)) {
                    Intent annoyingNotification = new Intent(this, AnnoyingAlarmActivity.class);
                    annoyingNotification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(annoyingNotification);
                }
            }
        }

    }
}