package av.sea.examdesk.admin.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import av.sea.examdesk.R;
import av.sea.examdesk.admin.NewTestActivity;
import av.sea.examdesk.model.Test;

public class TestRecyclerAdapter extends RecyclerView.Adapter<TestRecyclerAdapter.ViewHolder> {
    Context context;
    List<Test> tests;

    public TestRecyclerAdapter(Context context, List<Test> tests) {
        this.context = context;
        this.tests = tests;
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

    @Override
    public void onBindViewHolder(@NonNull TestRecyclerAdapter.ViewHolder holder, int position) {
        Test test = tests.get(position);

        holder.testName.setText(test.getTestName());
        holder.testDuration.setText(String.valueOf(test.getDuration()));

        holder.editTest.setOnClickListener(v -> {
            Intent editTestIntent = new Intent(context, NewTestActivity.class);
            editTestIntent.putExtra("test", test);
            context.startActivity(editTestIntent);
        });
    }

    @Override
    public int getItemCount() {
        return tests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView testName, testDuration;
        MaterialButton resultTest, editTest, deleteTest;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            testName = itemView.findViewById(R.id.testName);
            testDuration = itemView.findViewById(R.id.testDuration);

            resultTest = itemView.findViewById(R.id.resultTest);
            editTest = itemView.findViewById(R.id.editTest);
            deleteTest = itemView.findViewById(R.id.deleteTest);
        }
    }
}
