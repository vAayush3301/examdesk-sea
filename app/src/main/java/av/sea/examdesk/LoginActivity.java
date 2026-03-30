package av.sea.examdesk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import av.sea.examdesk.admin.AdminActivity;
import av.sea.examdesk.helpers.ApiService;
import av.sea.examdesk.helpers.Statics;
import av.sea.examdesk.model.User;
import av.sea.examdesk.user.MainActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private static final String USERNAME = "admin0";
    private static final String PASSWORD = "null@00";

    private TextInputEditText username_field, password_field;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        FirebaseApp.initializeApp(this);

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

                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) return;

                            String token = task.getResult();
                            User user = new User(username, token);

                            Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl(Statics.BASE_URL)
                                    .addConverterFactory(ScalarsConverterFactory.create())
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build();

                            ApiService apiService = retrofit.create(ApiService.class);

                            Call<ResponseBody> createUserTokenCall = apiService.createUser(Statics.CLIENT_ID, user);
                            createUserTokenCall.enqueue(new Callback<>() {
                                @Override
                                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Log.i("TOKEN", response.body() + ", Token Created" + token);
                                    } else {
                                        Log.i("TOKEN", response.body() + ", Token Created" + token);
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                    Log.e("TOKEN", t.getMessage() + ", Token Created" + token);
                                    t.printStackTrace();
                                }
                            });
                        });
            }

            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}