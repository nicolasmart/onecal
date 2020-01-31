package tk.onecal.onecal;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class AnnoyingAlarmActivity extends AppCompatActivity {

    public static String EXTRA_MSG = "extra_msg";
    MediaPlayer mediaPlayerScan;

    private TextView groupText, titleText, importaceText;
    String[] titles, importances, groups, notifications;
    ImageView notifyIcon;
    Window window;
    Integer i=0;
    View view;
    Handler someHandler;
    Vibrator v;
    AudioManager audioManager;
    int preAlarmVolume;
    Boolean isdismiss=false;
    Button ignoreButton;

    ConstraintLayout thislayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annoying_alarm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Intent it = new Intent(AnnoyingAlarmActivity.this, ChatHeadService.class);
        stopService(it);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        groupText = findViewById(R.id.group_final);
        titleText = findViewById(R.id.title_final);
        importaceText = findViewById(R.id.importance_text_final);
        thislayout = findViewById(R.id.annoying_view);
        notifyIcon = findViewById(R.id.thumb);
        ignoreButton = (Button) findViewById(R.id.chat_head_button);

        window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.colorPrimary));
        importaceText.setText(getString(R.string.urgent_importance));

        titles=loadArray("name", getApplicationContext());
        importances=loadArray("importance", getApplicationContext());
        groups=loadArray("group", getApplicationContext());
        notifications=loadArray("notification", getApplicationContext());

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
        someHandler.post(new VolumeRunnable(audioManager, someHandler, this));

        final Context emCtx = this;





        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            ignoreButton.setVisibility(View.INVISIBLE);
        }
        ignoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (titles.length == 1) {
                    someHandler.removeCallbacksAndMessages(null);
                    v.cancel();
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, preAlarmVolume, 0);
                    it.putExtra(EXTRA_MSG, getString(R.string.dont_forget));
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

        Button dismissButton = (Button) findViewById(R.id.dismiss_button);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                someHandler.removeCallbacksAndMessages(null);
                v.cancel();
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, preAlarmVolume, 0);
                NotificationManager notificationManager = (NotificationManager) AnnoyingAlarmActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(Integer.valueOf(notifications[i]));
                if (titles.length == 1){
                    deleteFromArray(i, "name", getApplicationContext());
                    deleteFromArray(i, "importance", getApplicationContext());
                    deleteFromArray(i, "group", getApplicationContext());
                    deleteFromArray(i, "notification", getApplicationContext());
                    mediaPlayerScan.stop();
                    finish();
                } else {
                    deleteFromArray(i, "name", getApplicationContext());
                    deleteFromArray(i, "importance", getApplicationContext());
                    deleteFromArray(i, "group", getApplicationContext());
                    deleteFromArray(i, "notification", getApplicationContext());
                    titles=loadArray("name", getApplicationContext());
                    importances=loadArray("importance", getApplicationContext());
                    groups=loadArray("group", getApplicationContext());
                    notifications=loadArray("notification", getApplicationContext());
                    i = i - 1;
                    getTask();
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
            thislayout.setBackgroundResource(R.drawable.green_bar);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#64DD17"));
            ignoreButton.setTextColor(Color.parseColor("#ffffff"));
            groupText.setTextColor(Color.parseColor("#ffffff"));
            titleText.setTextColor(Color.parseColor("#ffffff"));
            importaceText.setTextColor(Color.parseColor("#ffffff"));

            notifyIcon.setColorFilter(Color.parseColor("#ffffff"), android.graphics.PorterDuff.Mode.SRC_IN);
            importaceText.setText(getString(R.string.light_importance));
        } else if (importances[i].contains(getString(R.string.medium_importance))) {
            thislayout.setBackgroundResource(R.drawable.yellow_screen_white_text);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(Color.parseColor("#fff200"));
            ignoreButton.setTextColor(Color.parseColor("#000000"));
            groupText.setTextColor(Color.parseColor("#000000"));
            titleText.setTextColor(Color.parseColor("#000000"));
            importaceText.setTextColor(Color.parseColor("#000000"));
            notifyIcon.setColorFilter(Color.parseColor("#000000"), android.graphics.PorterDuff.Mode.SRC_IN);
            importaceText.setText(getString(R.string.medium_importance));
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getColor(R.color.colorPrimary));
            importaceText.setText(getString(R.string.urgent_importance));
            thislayout.setBackgroundResource(R.color.colorPrimary);
            ignoreButton.setTextColor(Color.parseColor("#ffffff"));
            groupText.setTextColor(Color.parseColor("#ffffff"));
            titleText.setTextColor(Color.parseColor("#ffffff"));
            importaceText.setTextColor(Color.parseColor("#ffffff"));
            window.getDecorView().setSystemUiVisibility(0);
            notifyIcon.setColorFilter(Color.parseColor("#ffffff"), android.graphics.PorterDuff.Mode.SRC_IN);
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
