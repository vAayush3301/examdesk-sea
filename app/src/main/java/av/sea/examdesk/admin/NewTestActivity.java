package av.sea.examdesk.admin;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import av.sea.examdesk.R;
import av.sea.examdesk.admin.adapters.ImageListAdapter;
import av.sea.examdesk.helpers.ApiService;
import av.sea.examdesk.helpers.Statics;
import av.sea.examdesk.model.Image;
import av.sea.examdesk.model.Question;
import av.sea.examdesk.model.Test;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewTestActivity extends AppCompatActivity {
    private MaterialTextView questionCount;
    private TextInputEditText testName, questionText, o1, o2, o3, o4, correctOption;
    private Test test;
    private List<Image> imageKeys = new ArrayList<>();
    private List<Question> questions = new ArrayList<>();

    private boolean editFlag = false;
    private int currentQuestion = 1;

    private ActivityResultLauncher<String> pickImageLauncher;
    private String imageAlt;

    private ImageListAdapter imageListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadToServer(uri);
                    }
                }
        );

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

        FloatingActionButton publishBtn = findViewById(R.id.publishTest);
        publishBtn.setOnClickListener(v -> {
            View view = getLayoutInflater().inflate(R.layout.duration_picker, null);
            TextInputEditText duration_edit = view.findViewById(R.id.duration);

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Set Test Duration")
                    .setIcon(R.drawable.sea)
                    .setView(view)
                    .setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()))
                    .setPositiveButton("Publish", ((dialog, which) -> {
                        try {
                            int duration = Integer.parseInt(String.valueOf(duration_edit.getText()));

                            if (duration <= 0) {
                                Toast.makeText(this, "Test duration cannot be less than 1 minute.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            publishTest(duration);
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Time is numeric.", Toast.LENGTH_SHORT).show();
                        }

                    })).show();
        });
    }

    private void publishTest(int duration) {
        String testName = String.valueOf(this.testName.getText());

        Test test = new Test(Statics.CLIENT_ID, testName, questions, imageKeys, duration);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Statics.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<ResponseBody> createTestCall = apiService.createTest(Statics.CLIENT_ID, test);
        createTestCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(NewTestActivity.this, "Test Published", Toast.LENGTH_SHORT).show();

                    if (editFlag) {
                        Call<ResponseBody> deleteTestCall = apiService.deleteTest(Statics.CLIENT_ID, NewTestActivity.this.test);
                        deleteTestCall.enqueue(new Callback<>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.i("DELETE_TEST", "Test Deleted: " + response.code());
                                } else {
                                    Log.e("DELETE_TEST", "Error while deleting Test: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                Log.e("DELETE_TEST", "Failed to Delete Test: " + t.getMessage());
                            }
                        });
                    }

                    finish();
                } else {
                    Toast.makeText(NewTestActivity.this, "Error while publishing Test.", Toast.LENGTH_SHORT).show();
                    Log.e("CREATE_TEST", "SERVER: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(NewTestActivity.this, "Failed to publish test.", Toast.LENGTH_SHORT).show();
                Log.e("CREATE_TEST", "SERVER: " + t.getMessage());
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
        View view = getLayoutInflater().inflate(R.layout.add_image_dialog, null);

        RecyclerView imageRecycler = view.findViewById(R.id.imageRecycler);
        imageRecycler.setLayoutManager(new LinearLayoutManager(this));
        imageListAdapter = new ImageListAdapter(this, imageKeys);
        imageRecycler.setAdapter(imageListAdapter);

        MaterialButton addImage = view.findViewById(R.id.addImageBtn);
        TextInputEditText altImage = view.findViewById(R.id.imageAlt);

        addImage.setOnClickListener(v -> {
            String imageAlt = String.valueOf(altImage.getText());

            if (imageAlt.isEmpty()) {
                Toast.makeText(NewTestActivity.this, "Image Alt is Empty", Toast.LENGTH_SHORT).show();
                return;
            }

            NewTestActivity.this.imageAlt = imageAlt;
            pickImageLauncher.launch("image/*");
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Images")
                .setIcon(R.drawable.sea)
                .setView(view)
                .setNegativeButton("Close", (d, w) -> d.dismiss())
                .show();
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

    private void uploadToServer(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");

            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = Objects.requireNonNull(inputStream).read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            outputStream.close();
            inputStream.close();

            RequestBody requestFile =
                    RequestBody.create(Objects.requireNonNull(MediaType.parse("image/*")), file);

            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Statics.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);

            Call<ResponseBody> call = apiService.uploadImage(body);

            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            if (response.body() != null) {
                                String key = response.body().string();
                                Image image = new Image(key, imageAlt);
                                imageKeys.add(image);
                                imageListAdapter.notifyDataSetChanged();
                            } else
                                Toast.makeText(NewTestActivity.this, "Error getting image key.", Toast.LENGTH_SHORT).show();

                        } catch (NullPointerException | IOException e) {
                            Toast.makeText(NewTestActivity.this, "Error getting image key.", Toast.LENGTH_SHORT).show();
                            throw new RuntimeException(e);
                        }
                    } else {
                        Toast.makeText(NewTestActivity.this, "Error while uploading image.", Toast.LENGTH_SHORT).show();
                        Log.e("UPLOAD", "Error: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Toast.makeText(NewTestActivity.this, "Failed to upload image. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.e("UPLOAD", "Failed: " + t.getMessage());
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}