package av.sea.examdesk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import av.sea.examdesk.admin.AdminActivity;
import av.sea.examdesk.user.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private static final String USERNAME = "admin0";
    private static final String PASSWORD = "null@00";

    private TextInputEditText username_field, password_field;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        DynamicColors.applyToActivitiesIfAvailable(getApplication());

        SharedPreferences prefs = getSharedPreferences("ExamDesk", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        String savedUsername = prefs.getString("username", "null");

        if (isLoggedIn) {
            splashScreen.setKeepOnScreenCondition(() -> true);

            getWindow().getDecorView().post(() -> {
                Intent intent;
                if (USERNAME.equals(savedUsername)) {
                    intent = new Intent(this, AdminActivity.class);
                } else {
                    intent = new Intent(this, MainActivity.class);
                }
                startActivity(intent);
                finish();
            });
            return;
        }

        setContentView(R.layout.activity_login);

        username_field = findViewById(R.id.username);
        password_field = findViewById(R.id.password);
        MaterialButton loginBtn = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(v -> {

            String username = String.valueOf(username_field.getText());
            String password = String.valueOf(password_field.getText());

            if (username.isEmpty()) {
                ((TextInputLayout) findViewById(R.id.username_layout))
                        .setError("Username is empty");
                return;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("username", username);
            editor.putBoolean("isLoggedIn", true);
            editor.apply();

            Intent intent;
            if (USERNAME.equals(username) && PASSWORD.equals(password)) {
                intent = new Intent(this, AdminActivity.class);
            } else {
                intent = new Intent(this, MainActivity.class);
            }

            startActivity(intent);
            finish();
        });
    }
}