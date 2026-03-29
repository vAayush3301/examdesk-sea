package av.sea.examdesk.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import av.sea.examdesk.LoginActivity;
import av.sea.examdesk.R;
import av.sea.examdesk.helpers.ApiService;
import av.sea.examdesk.helpers.Statics;
import av.sea.examdesk.model.Test;
import av.sea.examdesk.user.adapters.TestRecyclerAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private final Handler handler = new Handler();
    private Runnable periodicRunnable;

    private TestRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Statics.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        RecyclerView testRecycler = findViewById(R.id.testListUser);
        testRecycler.setLayoutManager(new LinearLayoutManager(this));

        List<Test> tests = new ArrayList<>();
        adapter = new TestRecyclerAdapter(this, tests);
        testRecycler.setAdapter(adapter);

        startPeriodicTask(api);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.logout) {
                SharedPreferences preferences = getSharedPreferences("ExamDesk", MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = preferences.edit();
                prefsEditor.clear();
                prefsEditor.apply();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.about) {
                //TODO create about page
                return true;
            }

            return false;
        });
    }

    private void startPeriodicTask(ApiService api) {
        periodicRunnable = new Runnable() {
            @Override
            public void run() {
                api.getTests(Statics.CLIENT_ID).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Test>> call, @NonNull Response<List<Test>> response) {
                        if (response.isSuccessful()) {
                            adapter.setTests(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Test>> call, @NonNull Throwable t) {
                        t.printStackTrace();
                    }
                });

                handler.postDelayed(this, 5000);
            }
        };

        handler.post(periodicRunnable);
    }
}