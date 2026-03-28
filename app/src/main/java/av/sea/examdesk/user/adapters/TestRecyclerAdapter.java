package av.sea.examdesk.user.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import av.sea.examdesk.R;
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
        View view = LayoutInflater.from(context).inflate(R.layout.user_test_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestRecyclerAdapter.ViewHolder holder, int position) {
        Test test = tests.get(position);

        holder.testName.setText(test.getTestName());
        holder.testDuration.setText(test.getDuration() + " minute(s)");
    }

    @Override
    public int getItemCount() {
        return tests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView testName, testDuration;
        MaterialButton attemptTest;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            testName = itemView.findViewById(R.id.testNameUser);
            testDuration = itemView.findViewById(R.id.testDurationUser);

            attemptTest = itemView.findViewById(R.id.attemptTest);
        }
    }
}
