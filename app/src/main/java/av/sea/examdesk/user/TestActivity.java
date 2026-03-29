package av.sea.examdesk.user;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import av.sea.examdesk.R;
import av.sea.examdesk.model.Test;

public class TestActivity extends AppCompatActivity {
    private Test test;

    private int focusViolations = 0;
    private long lastViolationTime = 0;

    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState != null) {
            focusViolations = savedInstanceState.getInt("violations", 0);
            lastViolationTime = savedInstanceState.getLong("lastViolationTime", 0);
        }

        startLockTask();

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                visibility -> getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )
        );

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        if (getIntent() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                test = getIntent().getSerializableExtra("test", Test.class);
            } else {
                test = (Test) getIntent().getSerializableExtra("test");
            }

            if (test == null) {
                Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setTitle(test.getTestName());

        new CountDownTimer(1000L * 60 * test.getDuration(), 1000) {

            @Override
            public void onFinish() {
                topAppBar.setSubtitle("Time Over");
                stopLockTask();
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;

                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;

                topAppBar.setSubtitle(String.format("%02d:%02d:%02d", hours, minutes, secs));
            }
        }.start();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                logViolation("onBackPressed");
                Toast.makeText(TestActivity.this, "Do not leave the test screen", Toast.LENGTH_SHORT).show();
                Log.i("TEST", "Back pressed. Violations left: " + (3 - focusViolations));
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                    visibility -> getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
            );
        } else {
            logViolation("onWindowFocusChanged");
            Toast.makeText(this, "Do not leave the test screen", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        logViolation("onPause");
        Toast.makeText(this, "Do not leave the test screen", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        logViolation("onUserLeaveHint");
        Toast.makeText(this, "Do not leave the test screen", Toast.LENGTH_SHORT).show();
    }

    void logViolation(String source) {
        long now = System.currentTimeMillis();

        if (now - lastViolationTime < 1000) return;
        lastViolationTime = now;

        focusViolations++;

        Log.i("TEST", source + " | Violations: " + focusViolations);

        if (focusViolations >= 3) {
            // submitExam();
            finish();
            stopLockTask();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("violations", focusViolations);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        focusViolations = savedInstanceState.getInt("violations", 0);
    }
}