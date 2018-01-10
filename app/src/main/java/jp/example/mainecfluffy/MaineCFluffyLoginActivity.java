package jp.example.mainecfluffy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.bootcampsns.R;

public class MaineCFluffyLoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maine_c_fluffy_login);

        final Activity activity = this;

        ImageButton loginButton = (ImageButton) findViewById(R.id.maine_login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) activity.findViewById(R.id.maine_login_id);
                String login_id = editText.getText().toString();
                if(!login_id.matches("^[a-zA-A0-9\\-\\._]+$")) {
                    Toast.makeText(activity, "IDは半角英数字で入力してください", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(MaineCFluffyLoginActivity.this, MaineCFluffyConfirmActivity.class);
                intent.putExtra("login_id", login_id);
                startActivity(intent);

                MaineCFluffyLoginActivity.this.finish();
            }
        });

        ImageButton cancelButton = (ImageButton) findViewById(R.id.maine_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaineCFluffyLoginActivity.this.finish();
            }
        });


    }
}
