package tk.onecal.onecal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.FirebaseApp;
import com.firebase.client.Firebase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.UUID;

public class FeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.feedback));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final EditText namedata, messagedata, emaildata;
        final Button send, details;
        final Firebase firebase;

        namedata = findViewById(R.id.namedata);
        emaildata = findViewById(R.id.emaildata);
        messagedata = findViewById(R.id.messagedata);

        send = findViewById(R.id.btn_send);
        details = findViewById(R.id.btn_details);
        Firebase.setAndroidContext(this);

        final String UniqueID = UUID.randomUUID().toString();

        firebase = new Firebase("https://onecal-f3ef9.firebaseio.com/Users/" + UniqueID);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                String name = namedata.getText().toString();
                String email = emaildata.getText().toString();
                String message = messagedata.getText().toString();
                if (name.isEmpty()) {
                    namedata.setError(getString(R.string.required_name));
                    return;
                } else {
                    namedata.setError(null);
                    send.setEnabled(false);
                }
                if (email.isEmpty()) email="no e-mail supplied";
                if (message.isEmpty())
                {
                    messagedata.setError(getString(R.string.required_message));
                    return;
                }
                else {
                    messagedata.setError(null);
                    send.setEnabled(false);
                }

                Firebase child_name = firebase.child(getString(R.string.name_or_nickname));
                child_name.setValue(name);

                Firebase child_email = firebase.child(getString(R.string.email));
                child_email.setValue(email);

                Firebase child_message = firebase.child(getString(R.string.message));
                child_message.setValue(message);

                Firebase child_topic = firebase.child("ID");
                child_topic.setValue(UniqueID);

                FirebaseMessaging.getInstance().subscribeToTopic(UniqueID);

                Toast.makeText(FeedbackActivity.this, getString(R.string.data_sent), Toast.LENGTH_SHORT).show();
            }
        });

        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(FeedbackActivity.this)
                        .setTitle(getString(R.string.help_title))
                        .setMessage(getString(R.string.help_desc))
                        .show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
