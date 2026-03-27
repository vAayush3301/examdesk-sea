package av.sea.examdesk.admin;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import av.sea.examdesk.R;
import av.sea.examdesk.model.Image;
import av.sea.examdesk.model.Question;
import av.sea.examdesk.model.Test;

public class NewTestActivity extends AppCompatActivity {
    private MaterialTextView questionCount;
    private TextInputEditText testName, questionText, o1, o2, o3, o4, correctOption;
    private Test test;
    private List<Image> imageKeys = new ArrayList<>();
    private List<Question> questions = new ArrayList<>();

    private boolean editFlag = false;
    private int currentQuestion = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        questionCount = findViewById(R.id.questionCount);
        testName = findViewById(R.id.testName);
        questionText = findViewById(R.id.questionText);
        o1 = findViewById(R.id.option1);
        o2 = findViewById(R.id.option2);
        o3 = findViewById(R.id.option3);
        o4 = findViewById(R.id.option4);
        correctOption = findViewById(R.id.correctOption);

        if (getIntent() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                test = getIntent().getSerializableExtra("test", Test.class);
            } else {
                test = (Test) getIntent().getSerializableExtra("test");
            }

            questionCount.setText("1.");

            if (test != null) {
                editFlag = true;

                imageKeys = test.getImageKeys();
                questions = test.getQuestions();

                testName.setText(test.getTestName());
                topAppBar.setSubtitle(testName.getText());

                loadQuestion(1);
            }
        }

        testName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String tName = s.toString();
                topAppBar.setSubtitle(tName);
            }
        });

        correctOption.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String correctCode = s.toString();
                TextInputLayout correctOption_layout = findViewById(R.id.correctOption_layout);
                if (!correctCode.matches("^[1-4]$")) {
                    correctOption_layout.setError("Correct Option is between 1 and 4.");
                } else {
                    correctOption_layout.setError("");
                }
            }
        });

        bottomAppBar.replaceMenu(R.menu.create_test_bottom_app_bar_menu);
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
                handleImages();
                return true;
            }
        });
    }

    private void loadQuestion(int key) {
        currentQuestion = key;
        questionCount.setText(String.valueOf(key).concat("."));

        if (key > questions.size()) {
            questionText.setText("");
            o1.setText("");
            o2.setText("");
            o3.setText("");
            o4.setText("");
            Objects.requireNonNull(correctOption.getText()).clear();

            return;
        }

        Question question = questions.get(currentQuestion - 1);

        String qText = question.getQuestionText();
        String option1 = question.getOption1();
        String option2 = question.getOption2();
        String option3 = question.getOption3();
        String option4 = question.getOption4();
        String correct = question.getCorrectOption();

        questionText.setText(qText);
        o1.setText(option1);
        o2.setText(option2);
        o3.setText(option3);
        o4.setText(option4);
        correctOption.setText(correct);
    }

    private void handleImages() {
    }

    private void handlePrevious() {
        if (currentQuestion == 1) {
            Toast.makeText(this, "This is the first question.", Toast.LENGTH_SHORT).show();
            return;
        }
        currentQuestion--;

        loadQuestion(currentQuestion);
    }

    private void handleNext() {
        if (!saveQuestion()) return;

        currentQuestion++;
        loadQuestion(currentQuestion);
    }

    private boolean saveQuestion() {
        String qText = String.valueOf(questionText.getText());
        String option1 = String.valueOf(o1.getText());
        String option2 = String.valueOf(o2.getText());
        String option3 = String.valueOf(o3.getText());
        String option4 = String.valueOf(o4.getText());
        String correct = String.valueOf(correctOption.getText());

        if (qText.isEmpty() || option1.isEmpty() || option2.isEmpty() || correct.isEmpty()) {
            Toast.makeText(this, "Fill all mandatory fields!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!correct.matches("^[1-4]$")) {
            Toast.makeText(this, "Please enter option code only!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (currentQuestion - 1 == questions.size()) {
            questions.add(new Question(qText, option1, option2, option3, option4, correct));
        } else {
            questions.set(currentQuestion - 1, new Question(qText, option1, option2, option3, option4, correct));
        }

        return true;
    }
}