package av.sea.examdesk.admin.adapters;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import av.sea.examdesk.R;
import av.sea.examdesk.admin.HomeFragment;
import av.sea.examdesk.admin.NewTestActivity;
import av.sea.examdesk.helpers.ApiService;
import av.sea.examdesk.helpers.ResultEvaluator;
import av.sea.examdesk.helpers.Statics;
import av.sea.examdesk.model.Question;
import av.sea.examdesk.model.SubmitResponse;
import av.sea.examdesk.model.Test;
import av.sea.examdesk.model.UserResult;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestRecyclerAdapter extends RecyclerView.Adapter<TestRecyclerAdapter.ViewHolder> {
    private final ApiService apiService;
    private final ActivityResultLauncher<String> saveFileLauncher;
    Context context;
    List<Test> tests;

    public TestRecyclerAdapter(Context context, List<Test> tests, ApiService apiService, ActivityResultLauncher<String> saveFileLauncher) {
        this.context = context;
        this.tests = tests;
        this.apiService = apiService;
        this.saveFileLauncher = saveFileLauncher;
    }

    public void setTests(List<Test> tests) {
        this.tests = tests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TestRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_test_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TestRecyclerAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Test test = tests.get(position);

        holder.testName.setText(test.getTestName());
        holder.testDuration.setText(String.valueOf(test.getDuration()));

        holder.editTest.setOnClickListener(v -> {
            Intent editTestIntent = new Intent(context, NewTestActivity.class);
            editTestIntent.putExtra("test", test);
            context.startActivity(editTestIntent);
        });

        holder.deleteTest.setOnClickListener(v -> {
            MaterialTextView confirmationText = new MaterialTextView(context);
            confirmationText.setText("Do you want to delete this test? This action cannot be undone.");
            confirmationText.setPadding(30, 30, 30, 30);

            new MaterialAlertDialogBuilder(context)
                    .setTitle("Delete Test?")
                    .setIcon(R.drawable.sea)
                    .setView(confirmationText)
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Delete", (dialog, which) -> {
                        Call<ResponseBody> deleteTestCall = apiService.deleteTest(Statics.CLIENT_ID, test);
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
                    })
                    .show();
        });

        holder.downloadTest.setOnClickListener(v -> {
            HomeFragment.position = position;
            saveFileLauncher.launch(test.getTestName() + ".xlsx");
        });

        holder.resultTest.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.result_dialog, null);
            RecyclerView resultRecycler = dialogView.findViewById(R.id.resultRecycler);
            resultRecycler.setLayoutManager(new LinearLayoutManager(context));

            final List<SubmitResponse>[] responses = new List[]{new ArrayList<>()};
            final List<UserResult>[] results = new List[]{new ResultEvaluator(responses[0]).getTotalResult()};

            ResultAdapter resultAdapter = new ResultAdapter(context, results[0]);
            resultRecycler.setAdapter(resultAdapter);

            Call<List<SubmitResponse>> getResponseCall = apiService.getTestResult(test.getTestId(), Statics.CLIENT_ID);
            getResponseCall.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<List<SubmitResponse>> call, @NonNull Response<List<SubmitResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        responses[0] = response.body();
                        results[0] = new ResultEvaluator(responses[0]).getTotalResult();
                        resultAdapter.setResults(results[0]);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<SubmitResponse>> call, @NonNull Throwable t) {
                    Toast.makeText(context, "Failed to fetch results.", Toast.LENGTH_SHORT).show();
                    Log.e("RESULTS", "Failed ot fetch results: " + t.getMessage());
                }
            });


            new MaterialAlertDialogBuilder(context)
                    .setTitle("Results")
                    .setView(dialogView)
                    .setIcon(R.drawable.sea)
                    .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Export", (dialog, which) -> {

                        ContentValues values = new ContentValues();
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME,
                                "results_" + test.getTestName().replace(" ", "-") + "_" + System.currentTimeMillis() + ".csv");
                        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                        Uri uri = context.getContentResolver()
                                .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                        if (uri == null) {
                            Toast.makeText(context, "Failed to create file.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {

                            if (os == null) {
                                Toast.makeText(context, "Failed to open file.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            os.write("Username,Attempted,Correct,Marks Obtained\n".getBytes());
                            for (UserResult result : results[0]) {
                                String row = escape(result.getUserId()) + "," +
                                        result.getNumAttempted() + "," +
                                        result.getNumCorrect() + "," +
                                        result.getMarksObtained() + "\n";

                                os.write(row.getBytes());
                            }
                            os.flush();

                            Toast.makeText(context, "Exported to Downloads", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(context, "Error while exporting data.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }).show();
        });
    }

    private String escape(String value) {
        if (value == null) return "";

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    @Override
    public int getItemCount() {
        return tests.size();
    }

    public void saveExcel(Uri uri, int position) {
        Test test = tests.get(position);
        List<Question> questions = test.getQuestions();

        try (
                OutputStream os = context.getContentResolver().openOutputStream(uri);
                Workbook workbook = new XSSFWorkbook()
        ) {

            Sheet sheet = workbook.createSheet(test.getTestName());

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Question Text");
            header.createCell(1).setCellValue("Option 1");
            header.createCell(2).setCellValue("Option 2");
            header.createCell(3).setCellValue("Option 3");
            header.createCell(4).setCellValue("Option 4");
            header.createCell(5).setCellValue("Correct Option");

            for (int i = 1; i < questions.size(); i++) {
                Question question = questions.get(i);

                Row row = sheet.createRow(i);
                row.createCell(0).setCellValue(question.getQuestionText());
                row.createCell(1).setCellValue(question.getOption1());
                row.createCell(2).setCellValue(question.getOption2());
                row.createCell(3).setCellValue(question.getOption3());
                row.createCell(4).setCellValue(question.getOption4());
                row.createCell(5).setCellValue(question.getCorrectOption());
            }

            workbook.write(os);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView testName, testDuration;
        MaterialButton resultTest, editTest, deleteTest, downloadTest;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            testName = itemView.findViewById(R.id.testName);
            testDuration = itemView.findViewById(R.id.testDuration);

            resultTest = itemView.findViewById(R.id.resultTest);
            editTest = itemView.findViewById(R.id.editTest);
            deleteTest = itemView.findViewById(R.id.deleteTest);
            downloadTest = itemView.findViewById(R.id.downloadTest);
        }
    }
}
