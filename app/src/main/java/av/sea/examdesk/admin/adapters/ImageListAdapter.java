package av.sea.examdesk.admin.adapters;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import av.sea.examdesk.R;
import av.sea.examdesk.helpers.ApiService;
import av.sea.examdesk.helpers.Statics;
import av.sea.examdesk.model.Image;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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

        holder.deleteBtn.setOnClickListener(v -> {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Statics.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
            Call<ResponseBody> deleteCall = apiService.deleteImage(image.getImageKey());

            deleteCall.enqueue(new Callback<>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        imageKeys.remove(image);
                        notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, "Error while deleting image.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Toast.makeText(context, "Failed to delete image.", Toast.LENGTH_SHORT).show();
                }
            });
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
