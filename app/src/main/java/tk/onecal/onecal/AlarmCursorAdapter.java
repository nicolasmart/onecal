package tk.onecal.onecal;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import tk.onecal.onecal.data.AlarmReminderContract;

public class AlarmCursorAdapter extends CursorAdapter {

    private TextView mTitleText, mDateAndTimeText, mRepeatInfoText;
    private ImageView mImportanceImage;
    private ColorGenerator mColorGenerator = ColorGenerator.DEFAULT;
    private TextDrawable mDrawableBuilder;

    private Context mContext;

    public AlarmCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 );
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.fragment_appointments, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        mTitleText = (TextView) view.findViewById(R.id.item_info);
        mDateAndTimeText = (TextView) view.findViewById(R.id.date_info);
        mRepeatInfoText = (TextView) view.findViewById(R.id.repeat_info);
        mImportanceImage = (ImageView) view.findViewById(R.id.busyview);

        int titleColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE);
        int dateColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_DATE);
        int timeColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TIME);
        int repeatColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT);
        int repeatNoColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO);
        int repeatTypeColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE);
        int activeColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE);
        int importanceLevelColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_IMPORTANCE_LEVEL);
        int groupColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_GROUP);

        String title = cursor.getString(titleColumnIndex);
        String date = cursor.getString(dateColumnIndex);
        String time = cursor.getString(timeColumnIndex);
        String repeat = cursor.getString(repeatColumnIndex);
        String repeatNo = cursor.getString(repeatNoColumnIndex);
        String repeatType = cursor.getString(repeatTypeColumnIndex);
        String active = cursor.getString(activeColumnIndex);
        String importanceLevel = cursor.getString(importanceLevelColumnIndex);
        String group = cursor.getString(groupColumnIndex);

        mContext=context;

        setReminderTitle(title);

        if (date != null){
            String dateTime = date + " " + time;
            setReminderDateTime(dateTime);
        }else{
            mDateAndTimeText.setText(context.getString(R.string.date_not_set));
        }

        if(repeat != null){
            setReminderRepeatInfo(repeat, repeatNo, repeatType);
        }else{
            mRepeatInfoText.setText(context.getString(R.string.repeat_not_set));
        }

        if (active != null){
            setActiveImage(active, importanceLevel);
        }else{
            mImportanceImage.setVisibility(View.GONE);
        }
    }

    public void setReminderTitle(String title) {
        mTitleText.setText(title);
    }

    public void setReminderDateTime(String datetime) {
        mDateAndTimeText.setText(datetime);
    }

    public void setReminderRepeatInfo(String repeat, String repeatNo, String repeatType) {
        if(repeat.equals("true")){
            mRepeatInfoText.setText(mContext.getString(R.string.every_repeat, repeatNo, repeatType));
        }else if (repeat.equals("false")) {
            mRepeatInfoText.setText(mContext.getString(R.string.repeat_is_off));
        }
    }

    public void setActiveImage(String active, String importanceLevel){
        if(active.equals("true")){
            mImportanceImage.setVisibility(View.VISIBLE);
            if (importanceLevel.contains(mContext.getString(R.string.medium_importance))){
                mImportanceImage.setImageResource(R.drawable.yellow_bar);
            }
            else if (importanceLevel.contains(mContext.getString(R.string.urgent_importance))){
                mImportanceImage.setImageResource(R.drawable.red_bar);
            }
            else{
                mImportanceImage.setImageResource(R.drawable.green_bar);
            }
        }else if (active.equals("false")) {
            mImportanceImage.setVisibility(View.GONE);
        }

    }
}
