package tk.onecal.onecal;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.CharMatcher;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;


import tk.onecal.onecal.data.AlarmReminderContract;
import tk.onecal.onecal.reminder.AlarmScheduler;

public class BootService extends IntentService {

    public BootService() {
        super("BootService");
    }

    private static final long milMinute = 60000L;
    private static final long milHour = 3600000L;
    private static final long milDay = 86400000L;
    private static final long milWeek = 604800000L;
    private static final long milMonth = 2592000000L;
    private static final long milYear = 31556952000L;
    long mRepeatTime;

    @TargetApi(26)
    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            Cursor cursor_active = getContentResolver().query(
                    AlarmReminderContract.AlarmReminderEntry.CONTENT_URI,
                    null,
                    AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE + " LIKE \""
                            + "true" + "\"", null, null);

            cursor_active.moveToFirst();
            while (cursor_active.moveToNext()) {
                if (!cursor_active.getString(cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_ARCHIVED)).contains("false")) continue; ///TODO: REPEATED ALARMS

                Uri mCurrentReminderUri = ContentUris
                        .withAppendedId(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI,
                                cursor_active.getInt(cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry._ID)));

                int timeidx = cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TIME);
                int dateidx = cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_DATE);
                int repeatidx = cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT);
                int repeatnoidx = cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO);
                int repeattypeidx = cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE);
                String mTime = cursor_active.getString(timeidx);
                String mDate = cursor_active.getString(dateidx);
                String mRepeatNo = cursor_active.getString(repeatnoidx);
                String mRepeatType = cursor_active.getString(repeattypeidx);
                String mRepeat = cursor_active.getString(repeatidx);

                String mNewTime, mNewDate;
                if (mDate.charAt(4) == '/' && mDate.charAt(1) == '/')
                    mNewDate = "0" + CharMatcher.anyOf("/").removeFrom(mDate);
                else if (mDate.charAt(4) == '/' && mDate.charAt(2) == '/')
                    mNewDate = mDate.substring(0, 2) + "0" + CharMatcher.anyOf("/").removeFrom(mDate.substring(3));
                else if (mDate.charAt(3) == '/' && mDate.charAt(1) == '/')
                    mNewDate = "0" + mDate.charAt(0) + "0" + CharMatcher.anyOf("/").removeFrom(mDate.substring(2));
                else mNewDate = CharMatcher.anyOf("/").removeFrom(mDate);
                mNewDate = mNewDate.substring(4, 8) + mNewDate.substring(2, 4) + mNewDate.substring(0, 2);
                if (mTime.charAt(1) == ':')
                    mNewTime = CharMatcher.anyOf(":").removeFrom("0" + mTime);
                else if (mTime.length() == 4)
                    mNewTime = CharMatcher.anyOf(":").removeFrom(mTime.charAt(0) + mTime.charAt(1) + "0" + mTime.charAt(2));
                else mNewTime = CharMatcher.anyOf(":").removeFrom(mTime);

                if (mRepeatType.equals(getString(R.string.repeat_minute))) {
                    mRepeatTime = Integer.parseInt(mRepeatNo) * milMinute;
                    mRepeatType = "MINUTELY";
                } else if (mRepeatType.equals(getString(R.string.repeat_hour))) {
                    mRepeatTime = Integer.parseInt(mRepeatNo) * milHour;
                    mRepeatType = "HOURLY";
                } else if (mRepeatType.equals(getString(R.string.repeat_day))) {
                    mRepeatTime = Integer.parseInt(mRepeatNo) * milDay;
                    mRepeatType = "DAILY";
                } else if (mRepeatType.equals(getString(R.string.repeat_week))) {
                    mRepeatTime = Integer.parseInt(mRepeatNo) * milWeek;
                    mRepeatType = "WEEKLY";
                } else if (mRepeatType.equals(getString(R.string.repeat_month))) {
                    mRepeatTime = Integer.parseInt(mRepeatNo) * milMonth;
                    mRepeatType = "MONTHLY";
                } else if (mRepeatType.equals(getString(R.string.repeat_year))) {
                    mRepeatTime = Integer.parseInt(mRepeatNo) * milYear;
                    mRepeatType = "YEARLY";
                }

                Calendar mCalendar = Calendar.getInstance();

                Integer mMonth = Integer.parseInt(mNewDate.substring(4, 6));
                mCalendar.set(Calendar.MONTH, --mMonth);
                mCalendar.set(Calendar.YEAR, Integer.parseInt(mNewDate.substring(0, 4)));
                mCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mNewDate.substring(6, 8)));
                mCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(mNewTime.substring(0, 2)));
                mCalendar.set(Calendar.MINUTE, Integer.parseInt(mNewTime.substring(2, 4)));
                mCalendar.set(Calendar.SECOND, 0);

                long selectedTimestamp = mCalendar.getTimeInMillis();

                if (mRepeat.equals("true")) {
                    new AlarmScheduler().cancelAlarm(getApplicationContext(), mCurrentReminderUri); ///TODO: CANCEL STUFF IN ARCHIVED
                    new AlarmScheduler().setRepeatAlarm(getApplicationContext(), selectedTimestamp, mCurrentReminderUri, mRepeatTime);
                } else if (mRepeat.equals("false")) {
                    new AlarmScheduler().cancelAlarm(getApplicationContext(), mCurrentReminderUri);
                    new AlarmScheduler().setAlarm(getApplicationContext(), selectedTimestamp, mCurrentReminderUri);
                    Log.d("BootService", String.valueOf(selectedTimestamp) + " " + String.valueOf(mCurrentReminderUri) + " " + cursor_active.getString(cursor_active.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE)) +" that's bootservice");
                }
            }
        } catch (Exception ex) {
            Log.d("BootService", ex.getMessage());
        }

    }
}
