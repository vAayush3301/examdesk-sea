package av.sea.examdesk.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import av.sea.examdesk.model.Image;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TextImageRenderer {

    private final ApiService apiService;
    private final Map<String, String> urlCache = new HashMap<>();
    private final Map<String, Drawable> drawableCache = new HashMap<>();

    public TextImageRenderer() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Statics.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public void setImage(MaterialTextView textView, String qText, List<Image> images, Context context) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        Pattern pattern = Pattern.compile("\\[\\$(.*?)\\$]");
        Matcher matcher = pattern.matcher(qText);

        int lastEnd = 0;

        while (matcher.find()) {
            builder.append(qText, lastEnd, matcher.start());

            String alt = matcher.group(1);

            Image matched = null;
            for (Image img : images) {
                if (Objects.requireNonNull(alt).equals(img.getImageAlt())) {
                    matched = img;
                    break;
                }
            }

            if (matched != null) {
                int start = builder.length();
                builder.append(" ");

                builder.append("\n");

                handleImageSpan(textView, builder, start, matched.getImageKey(), context);

            } else {
                builder.append(matcher.group());
            }

            lastEnd = matcher.end();
        }

        builder.append(qText.substring(lastEnd));
        textView.setText(builder);
    }

    private void handleImageSpan(MaterialTextView textView,
                                 SpannableStringBuilder builder,
                                 int start,
                                 String key,
                                 Context context) {
        if (drawableCache.containsKey(key)) {
            applySpan(textView, builder, start, Objects.requireNonNull(drawableCache.get(key)));
            return;
        }

        if (urlCache.containsKey(key)) {
            loadDrawable(textView, builder, start, key, urlCache.get(key), context);
            return;
        }

        apiService.getImageUrl(key).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String url = response.body().string();
                        urlCache.put(key, url);

                        loadDrawable(textView, builder, start, key, url, context);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call,
                                  @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void loadDrawable(MaterialTextView textView,
                              SpannableStringBuilder builder,
                              int start,
                              String key,
                              String url,
                              Context context) {

        Glide.with(context)
                .asDrawable()
                .load(url)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource,
                                                @Nullable Transition<? super Drawable> transition) {

                        drawableCache.put(key, resource);
                        applySpan(textView, builder, start, resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    private void applySpan(MaterialTextView textView,
                           SpannableStringBuilder builder,
                           int start,
                           Drawable drawable) {

        int width = textView.getWidth();

        if (width == 0) {
            textView.post(() -> applySpan(textView, builder, start, drawable));
            return;
        }

        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();

        int scaledHeight = (int) ((float) width / intrinsicWidth * intrinsicHeight);

        drawable.setBounds(0, 0, width, scaledHeight);

        builder.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE),
                start, start + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(builder);
    }
}