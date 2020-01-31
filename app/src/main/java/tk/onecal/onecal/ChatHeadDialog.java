package tk.onecal.onecal;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;


public class ChatHeadDialog extends Activity {
	public static boolean active = false;
	public static Activity chatHeadDialog;
	public static String LogTag = "ChatHeadDialog";
	public static String EXTRA_MSG = "extra_msg";
	
	EditText edt;
	Button btn;
	View top;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		chatHeadDialog = ChatHeadDialog.this;
		
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String str = edt.getText().toString();
				if(str.length() > 0){
					Intent it = new Intent(ChatHeadDialog.this, ChatHeadService.class);
					it.putExtra(EXTRA_MSG, str);
					startService(it);
				}
			}
		});
		
		
		top.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}
		
	
	@Override
	protected void onResume() {
		super.onResume();
		active = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		active = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		active = false;
	}

	
	
}
