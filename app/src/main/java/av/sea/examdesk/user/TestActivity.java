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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import av.sea.examdesk.R;
import av.sea.examdesk.helpers.TextImageRenderer;
import av.sea.examdesk.model.Image;
import av.sea.examdesk.model.Question;
import av.sea.examdesk.model.Response;
import av.sea.examdesk.model.Test;

public class TestActivity extends AppCompatActivity {
    private Test test;
    private List<Image> imageKeys = new ArrayList<>();
    private List<Question> questions = new ArrayList<>();

    private HashMap<Integer, Response> responses = new HashMap<>();

    private int focusViolations = 0;
    private long lastViolationTime = 0;

    private MaterialToolbar topAppBar;

    private MaterialTextView questionCount, questionText;
    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton o1, o2, o3, o4;

    private int currentQuestion = 1;

    private TextImageRenderer renderer;

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

        questionCount = findViewById(R.id.questionCount);
        questionText = findViewById(R.id.questionText);

        toggleGroup = findViewById(R.id.toggleGroup);
        o1 = findViewById(R.id.btn1);
        o2 = findViewById(R.id.btn2);
        o3 = findViewById(R.id.btn3);
        o4 = findViewById(R.id.btn4);

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

        imageKeys = test.getImageKeys();
        questions = test.getQuestions();
        Collections.shuffle(questions);

        renderer = new TextImageRenderer();

        topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setTitle(test.getTestName());
        loadQuestion(1);

        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        bottomAppBar.replaceMenu(R.menu.test_navigation_menu);
        bottomAppBar.setContentInsetStartWithNavigation(16);
        bottomAppBar.setContentInsetEndWithActions(16);

        bottomAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_next) {
                handleNext();
                return true;
            } else if (item.getItemId() == R.id.action_previous) {
                handlePrevious();
                return true;
            } else {
                clearSelection();
                return true;
            }
        });

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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("violations", focusViolations);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        focusViolations = savedInstanceState.getInt("violations", 0);
    }

    @SuppressLint("SetTextI18n")
    private void loadQuestion(int key) {
        currentQuestion = key;

        if (key > questions.size() || key <= 0) {
            return;
        }

        Question question = questions.get(currentQuestion - 1);

        String qText = question.getQuestionText();
        String option1 = question.getOption1();
        String option2 = question.getOption2();
        String option3 = question.getOption3();
        String option4 = question.getOption4();

        questionText.setText(question.getQuestionText());
        o1.setText(option1);
        o2.setText(option2);
        o3.setText(option3);
        o4.setText(option4);

        questionCount.setText(currentQuestion + ".");

        renderer.setImage(questionText, qText, imageKeys, this);
    }

    private void handlePrevious() {
        if (currentQuestion == 1) {
            Toast.makeText(this, "This is the first question.", Toast.LENGTH_SHORT).show();
            return;
        }

        saveResponse(currentQuestion);
        currentQuestion--;
        loadQuestion(currentQuestion);
        loadResponse(currentQuestion);
    }

    private void handleNext() {
        if (currentQuestion == questions.size()) {
            Toast.makeText(this, "This is the last question.", Toast.LENGTH_SHORT).show();
            return;
        }

        saveResponse(currentQuestion);
        currentQuestion++;
        loadQuestion(currentQuestion);
        loadResponse(currentQuestion);
    }

    private void saveResponse(int key) {
        int selected = toggleGroup.getCheckedButtonId();

        int selectedId = -1;
        if (selected == R.id.btn1) {
            selectedId = 1;
        } else if (selected == R.id.btn2) {
            selectedId = 2;
        } else if (selected == R.id.btn3) {
            selectedId = 3;
        } else if (selected == R.id.btn4) {
            selectedId = 4;
        }

        Response response = new Response(questions.get(key - 1), selectedId);
        responses.put(key - 1, response);
    }

    private void loadResponse(int currentCount) {
        Response response;
        if (currentCount > responses.size()) {
            clearSelection();
            return;
        } else {
            response = responses.get(currentCount - 1);
            assert response != null;
            if (response.getResponseCode() == -1) {
                clearSelection();
                return;
            }
        }

        int selected = response.getResponseCode();
        MaterialButton o = null;

        if (selected == 1) o = o1;
        else if (selected == 2) o = o2;
        else if (selected == 3) o = o3;
        else if (selected == 4) o = o4;

        if (o == null) return;
        toggleGroup.check(o.getId());
    }

    public void clearSelection() {
        toggleGroup.clearChecked();
    }
}