package tk.onecal.onecal;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AnnoyingAlarmActivity extends AppCompatActivity {

    public static String EXTRA_MSG = "extra_msg";
    MediaPlayer mediaPlayerScan;

    private TextView groupText, titleText, importaceText, notifyTimer;
    String[] titles, importances, groups, notifications, locations, delays;
    TextClock notifyIcon;
    Window window;
    Integer i=0;
    View view;
    Handler someHandler;
    Vibrator v;
    AudioManager audioManager;
    int preAlarmVolume;
    Boolean isdismiss=false;
    Button dismissButton;
    Button ignoreButton;
    Button locationButton;

    ConstraintLayout thislayout;
    CountDownTimer cdt, cdt_temp;
    Intent rootintent;

    NotificationManager notificationManager;
    SharedPreferences appPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annoying_alarm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rootintent = getIntent();

        final Intent it = new Intent(AnnoyingAlarmActivity.this, ChatHeadService.class);
        stopService(it);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        notificationManager = (NotificationManager) AnnoyingAlarmActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);

        appPrefs = getApplicationContext().getSharedPreferences(SettingsActivity.SettingsFragment.SETTINGS_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);

        groupText = findViewById(R.id.group_final);
        titleText = findViewById(R.id.title_final);
        importaceText = findViewById(R.id.importance_text_final);
        thislayout = findViewById(R.id.annoying_view);
        notifyIcon = findViewById(R.id.thumb);
        ignoreButton = (Button) findViewById(R.id.chat_head_button);
        dismissButton = (Button) findViewById(R.id.dismiss_button);
        locationButton = (Button) findViewById(R.id.directions_button);
        notifyTimer = findViewById(R.id.thumb2);

        window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //window.setStatusBarColor(getColor(R.color.colorPrimary));
        importaceText.setText(getString(R.string.urgent_importance));

        titles=loadArray("name", getApplicationContext());
        importances=loadArray("importance", getApplicationContext());
        locations=loadArray("location", getApplicationContext());
        groups=loadArray("group", getApplicationContext());
        notifications=loadArray("notification", getApplicationContext());
        delays=loadArray("delay", getApplicationContext());

        i = titles.length-1;
        getTask();

        Uri alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mediaPlayerScan = new MediaPlayer();
        try {
            mediaPlayerScan.setDataSource(this, alarmTone);
            if (Build.VERSION.SDK_INT >= 21) {
                mediaPlayerScan.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());
            } else {
                mediaPlayerScan.setAudioStreamType(AudioManager.STREAM_ALARM);
            }
            mediaPlayerScan.prepare();
        } catch (IOException e) {
            try {
                mediaPlayerScan.setDataSource(this,
                        Uri.parse("android.resource://tk.onecal.onecal/" + R.raw.default_ringtone));
                if (Build.VERSION.SDK_INT >= 21) {
                    mediaPlayerScan.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());
                } else {
                    mediaPlayerScan.setAudioStreamType(AudioManager.STREAM_ALARM);
                }
                mediaPlayerScan.prepare();
            } catch (IOException ex) {
                e.printStackTrace();
            }
            e.printStackTrace();
        }

        mediaPlayerScan.setVolume(0.1f, 0.1f);
        mediaPlayerScan.setLooping(true);
        mediaPlayerScan.start();

        someHandler = new Handler();
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        preAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        if (appPrefs.getBoolean("gradient_sound", true)) someHandler.post(new VolumeRunnable(audioManager, someHandler, this));

        final Context emCtx = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            ignoreButton.setVisibility(View.INVISIBLE);
        }
        ignoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    cdt_temp.cancel();
                } catch (Exception ex) {}
                if (titles.length == 1) {
                    someHandler.removeCallbacksAndMessages(null);
                    v.cancel();
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, preAlarmVolume, 0);
                    it.putExtra(EXTRA_MSG, getString(R.string.dont_forget));
                    if (!locations[i].isEmpty()) it.putExtra("location", locations[i]);
                    startService(it);
                    mediaPlayerScan.stop();
                    finish();
                } else {
                    someHandler.removeCallbacksAndMessages(null);
                    v.cancel();
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, preAlarmVolume, 0);
                    someHandler.postDelayed(new VolumeRunnable(audioManager, someHandler, emCtx), 5000);
                    i = i - 1;
                    getTask();
                }
            }
        });

        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    cdt_temp.cancel();
                } catch (Exception ex) {}
                someHandler.removeCallbacksAndMessages(null);
                v.cancel();
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, preAlarmVolume, 0);
                if (!TextUtils.isEmpty(delays[i])) cdt.cancel();
                notificationManager.cancel(Integer.valueOf(notifications[i]));
                if (titles.length == 1){
                    deleteFromArray(i, "name", getApplicationContext());
                    deleteFromArray(i, "importance", getApplicationContext());
                    deleteFromArray(i, "group", getApplicationContext());
                    deleteFromArray(i, "notification", getApplicationContext());
                    deleteFromArray(i, "location", getApplicationContext());
                    deleteFromArray(i, "delay", getApplicationContext());
                    mediaPlayerScan.stop();
                    finish();
                } else {
                    deleteFromArray(i, "name", getApplicationContext());
                    deleteFromArray(i, "importance", getApplicationContext());
                    deleteFromArray(i, "group", getApplicationContext());
                    deleteFromArray(i, "location", getApplicationContext());
                    deleteFromArray(i, "notification", getApplicationContext());
                    deleteFromArray(i, "delay", getApplicationContext());
                    titles=loadArray("name", getApplicationContext());
                    importances=loadArray("importance", getApplicationContext());
                    locations=loadArray("location", getApplicationContext());
                    groups=loadArray("group", getApplicationContext());
                    notifications=loadArray("notification", getApplicationContext());
                    delays=loadArray("delay", getApplicationContext());
                    i = i - 1;
                    getTask();
                }

            }
        });

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String destination = locations[i];
                String map = "http://maps.google.com/maps?q=" + destination;

                Uri gmmIntentUri = Uri.parse(map);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                    locationButton.setText(getString(R.string.directions_selected));
                    locationButton.setTextColor(Color.parseColor("#66FFFFFF"));
                    locationButton.setEnabled(false);
                }
            }
        });
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
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size];
        for(int i=0;i<size;i++)
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

    public void getTask()
    {
        if (i>=titles.length) {
            Intent it = new Intent(AnnoyingAlarmActivity.this, ChatHeadService.class);
            it.putExtra(EXTRA_MSG, getString(R.string.dont_forget));
            startService(it);
            someHandler.removeCallbacksAndMessages(null);
            v.cancel();
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, preAlarmVolume, 0);
            mediaPlayerScan.stop();
            finish();
            return;
        }
        if (importances[i].contains(getString(R.string.light_importance))) {
            window.getDecorView().setSystemUiVisibility(0);
            //thislayout.setBackgroundResource(R.drawable.green_bar);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#87F876"));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            importaceText.setText(getString(R.string.light_importance));
        } else if (importances[i].contains(getString(R.string.medium_importance))) {
            //thislayout.setBackgroundResource(R.drawable.yellow_screen_white_text);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#F8D36F"));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            importaceText.setText(getString(R.string.medium_importance));
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getColor(R.color.colorPrimary));
            importaceText.setText(getString(R.string.urgent_importance));
            //thislayout.setBackgroundResource(R.color.colorPrimary);
            window.getDecorView().setSystemUiVisibility(0);
        }
        if (TextUtils.isEmpty(locations[i])) {
            locationButton.setVisibility(View.GONE);
        } else {
            locationButton.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(delays[i])) {
            dismissButton.setText(getString(R.string.dismiss_timer));
            ignoreButton.setText(getString(R.string.delay_ignore));
            notifyIcon.setVisibility(View.INVISIBLE);
            notifyTimer.setVisibility(View.VISIBLE);

            if (!rootintent.getBooleanExtra("chathead", false)) {
                cdt_temp = new CountDownTimer(Integer.parseInt(delays[i])*60000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        long remSeconds = millisUntilFinished / 1000;
                        notifyTimer.setText(String.format(Locale.ENGLISH, "%d:%02d", remSeconds / 60, remSeconds % 60));
                    }

                    public void onFinish() {
                        notifyTimer.setText("00:00");
                    }
                }.start();
                cdt = new CountDownTimer(Integer.parseInt(delays[i])*60000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        String channelId = "event-id";
                        String channelName = getString(R.string.events);
                        int importance = NotificationManager.IMPORTANCE_LOW;

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            NotificationChannel mChannel = new NotificationChannel(
                                    channelId, channelName, importance);
                            notificationManager.createNotificationChannel(mChannel);
                        }

                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                                .setSmallIcon(R.drawable.ic_newcal)
                                .setContentTitle(notifyTimer.getText().toString())
                                .setContentText(titles[i] + " - " + groups[i])
                                .setAutoCancel(true)
                                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                                .setOngoing(true);

                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                        stackBuilder.addNextIntent(new Intent(getApplicationContext(), MainActivity.class));
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(resultPendingIntent);
                        notificationManager.notify(Integer.parseInt(notifications[i]), mBuilder.build());
                    }

                    public void onFinish() {
                        notificationManager.cancel(Integer.parseInt(notifications[i]));
                    }
                }.start();

            }
            else {
                notifyTimer.setVisibility(View.INVISIBLE);
                notifyIcon.setVisibility(View.VISIBLE);
            }
        } else {
            notifyTimer.setVisibility(View.INVISIBLE);
            notifyIcon.setVisibility(View.VISIBLE);
        }
        titleText.setText(titles[i]);
        groupText.setText(groups[i]);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

}
