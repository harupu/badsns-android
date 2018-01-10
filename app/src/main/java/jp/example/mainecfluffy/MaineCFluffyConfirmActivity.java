package jp.example.mainecfluffy;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bootcampsns.R;

public class MaineCFluffyConfirmActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maine_c_fluffy_confirm);
        Intent intent = getIntent();
        final String login_id = intent.getStringExtra("login_id");

        final Activity activity = this;

        ImageButton confirmButton = (ImageButton) findViewById(R.id.maine_confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaineCFluffyUser user = new MaineCFluffyUser(login_id,
                        login_id, login_id+"@example.com");
                MaineCFluffyResult result = new MaineCFluffyResult(user, "__dummy_token__");
                MaineCFluffyManager.getInstance().onLoginFinished(result);

                MaineCFluffyConfirmActivity.this.finish();
            }
        });

        ImageButton cancelButton = (ImageButton) findViewById(R.id.maine_confirm_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaineCFluffyConfirmActivity.this.finish();
            }
        });

        TextView idTextView = (TextView) findViewById(R.id.maine_confirm_login_id);
        idTextView.setText(login_id+"でログインしています。");
    }
}
