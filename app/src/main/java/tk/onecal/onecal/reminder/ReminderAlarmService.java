package tk.onecal.onecal.reminder;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tk.onecal.onecal.AddReminderActivity;
import tk.onecal.onecal.AnnoyingAlarmActivity;
import tk.onecal.onecal.MainActivity;
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

        String description = "";
        String importanceLevel = "";
        String group = "";
        try {
            if (cursor != null && cursor.moveToFirst()) {
                description = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_TITLE);
                importanceLevel = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_IMPORTANCE_LEVEL);
                group = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_GROUP);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        String[] groupNames = loadArray2("names", getApplicationContext());

        notification(description, importanceLevel + " (" + groupNames[Integer.valueOf(group)] + ")", getApplicationContext());

        String[] eventTitles = loadArray("name", getApplicationContext());
        String[] eventImportances = loadArray("importance", getApplicationContext());
        String[] eventGroups = loadArray("group", getApplicationContext());
        String[] eventNotifyId = loadArray("notification", getApplicationContext());

        eventTitles[eventTitles.length-1] = description;
        eventImportances[eventImportances.length-1]=importanceLevel;
        eventGroups[eventGroups.length-1]=groupNames[Integer.valueOf(group)];
        eventNotifyId[eventNotifyId.length-1]=String.valueOf(notificationId);

        saveArray(eventTitles, "name", getApplicationContext());
        saveArray(eventImportances, "importance", getApplicationContext());
        saveArray(eventGroups, "group", getApplicationContext());
        saveArray(eventNotifyId, "notification", getApplicationContext());

        Intent annoyingNotification = new Intent(this, AnnoyingAlarmActivity.class);
        annoyingNotification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(annoyingNotification);

    }
}