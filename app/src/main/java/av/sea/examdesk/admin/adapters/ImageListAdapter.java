package av.sea.examdesk.admin.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
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
import av.sea.examdesk.model.Image;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder> {
    Context context;
    List<Image> imageKeys;

    public ImageListAdapter(Context context, List<Image> imageKeys) {
        this.context = context;
        this.imageKeys = imageKeys;
    }

    @NonNull
    @Override
    public ImageListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageListAdapter.ViewHolder holder, int position) {
        Image image = imageKeys.get(position);

        holder.imageAltText.setText(image.getImageAlt());

        holder.copyAltBtn.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Image Alt", String.format("[$%s$]", image.getImageAlt()));
            clipboard.setPrimaryClip(clip);
        });
    }

    @Override
    public int getItemCount() {
        return imageKeys.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView imageAltText;
        MaterialButton copyAltBtn, deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageAltText = itemView.findViewById(R.id.imageAltText);
            copyAltBtn = itemView.findViewById(R.id.copyImageAlt);
            deleteBtn = itemView.findViewById(R.id.deleteImage);
        }
    }
}
