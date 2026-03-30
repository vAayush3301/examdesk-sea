package av.sea.examdesk.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import av.sea.examdesk.R;
import av.sea.examdesk.helpers.ApiService;
import av.sea.examdesk.helpers.Statics;
import av.sea.examdesk.model.User;
import av.sea.examdesk.user.MainActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MessageService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String title = message.getData().get("title");
        String body = message.getData().get("body");

        showNotification(title, body);
    }

    @SuppressLint("MissingPermission")
    private void showNotification(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "default_channel")
                        .setSmallIcon(R.drawable.sea)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager =
                NotificationManagerCompat.from(this);

        manager.notify(1, builder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        SharedPreferences preferences = getSharedPreferences("ExamDesk", MODE_PRIVATE);
        String username = preferences.getString("username", "admin0");

        if (username.equals("admin0")) return;

        User user = new User(username, token);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Statics.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<ResponseBody> createUserTokenCall = apiService.createUser(Statics.CLIENT_ID, user);
        createUserTokenCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.i("TOKEN", response.body() + ", Token Created" + token);
                } else {
                    Log.i("TOKEN", response.body() + ", Token Created" + token);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("TOKEN", t.getMessage() + ", Token Created" + token);
                t.printStackTrace();
            }
        });
    }
}