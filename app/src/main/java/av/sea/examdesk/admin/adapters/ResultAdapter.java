package av.sea.examdesk.admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import av.sea.examdesk.R;
import av.sea.examdesk.model.UserResult;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {
    private final Context context;
    private List<UserResult> results;

    public ResultAdapter(Context context, List<UserResult> results) {
        this.context = context;
        this.results = results;
    }

    public void setResults(List<UserResult> results) {
        this.results = results;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.result_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultAdapter.ViewHolder holder, int position) {
        UserResult result = results.get(position);

        holder.username.setText(result.getUserId());
        holder.obtained.setText(String.valueOf(result.getMarksObtained()));
        holder.attempted.setText(String.valueOf(result.getNumAttempted()));
        holder.correct.setText(String.valueOf(result.getNumCorrect()));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView username, correct, obtained, attempted;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username_result);
            correct = itemView.findViewById(R.id.correct_result);
            obtained = itemView.findViewById(R.id.obtained_result);
            attempted = itemView.findViewById(R.id.attempted_result);
        }
    }
}
