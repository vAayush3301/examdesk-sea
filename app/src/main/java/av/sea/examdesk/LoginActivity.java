package av.sea.examdesk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());

        username_field = findViewById(R.id.username);
        password_field = findViewById(R.id.password);
        MaterialButton loginBtn = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(v -> {
            String username = String.valueOf(username_field.getText());

            if (username.isEmpty()) {
                ((TextInputLayout) findViewById(R.id.username_layout)).setError("Username is empty");
                return;
            }

            String password = String.valueOf(password_field.getText());

            if (username.equals(USERNAME) && password.equals(PASSWORD)) {
                SharedPreferences prefs = getSharedPreferences("ExamDesk", MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = prefs.edit();
                prefsEditor.putString("username", username);
                prefsEditor.putBoolean("isLoggedIn", true);
                prefsEditor.apply();

                startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                finish();
            } else {
                SharedPreferences prefs = getSharedPreferences("ExamDesk", MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = prefs.edit();
                prefsEditor.putString("username", username);
                prefsEditor.putBoolean("isLoggedIn", true);
                prefsEditor.apply();

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("ExamDesk", MODE_PRIVATE);

        String username = prefs.getString("username", "null");
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            if (!username.equals(USERNAME)) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                finish();
            }
        }
    }
}