package tk.onecal.onecal;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VolumeRunnable implements Runnable {

    private AudioManager mAudioManager;
    private Context mContext;
    private Handler mHandlerThatWillIncreaseVolume;
    private Vibrator v;
    private final static int DELAY_UNTILL_NEXT_INCREASE = 1 * 500;

    VolumeRunnable(AudioManager audioManager, Handler handler, Context appContext) {
        this.mAudioManager = audioManager;
        this.mHandlerThatWillIncreaseVolume = handler;
        this.mContext = appContext;
    }

    @Override
    public void run() {
        int currentAlarmVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        if (currentAlarmVolume != mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentAlarmVolume+1, 0);
            if (currentAlarmVolume+5 >= mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentAlarmVolume+1, 0);
                mHandlerThatWillIncreaseVolume.postDelayed(this, 5000);
                return;
            }
            mHandlerThatWillIncreaseVolume.postDelayed(this, DELAY_UNTILL_NEXT_INCREASE);
        }
        else {
            v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(1500);
            }
            mHandlerThatWillIncreaseVolume.postDelayed(this, 3500);
        }

    }
}