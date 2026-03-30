package av.sea.examdesk.admin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import av.sea.examdesk.R;
import av.sea.examdesk.admin.adapters.TestRecyclerAdapter;
import av.sea.examdesk.helpers.ApiService;
import av.sea.examdesk.helpers.Statics;
import av.sea.examdesk.model.Test;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {
    private final Handler handler = new Handler();
    Parcelable state;
    private TestRecyclerAdapter adapter;

    private RecyclerView testRecycler;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Statics.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        testRecycler = view.findViewById(R.id.testList);
        testRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));

        List<Test> tests = new ArrayList<>();
        adapter = new TestRecyclerAdapter(requireContext(), tests, api);
        testRecycler.setAdapter(adapter);

        startPeriodicTask(api);

        ExtendedFloatingActionButton newTestButton = view.findViewById(R.id.create_new_test);
        newTestButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), NewTestActivity.class)));
    }

    private void startPeriodicTask(ApiService api) {
        Runnable periodicRunnable = new Runnable() {
            @Override
            public void run() {
                api.getTests(Statics.CLIENT_ID).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Test>> call, @NonNull Response<List<Test>> response) {
                        if (response.isSuccessful()) {
                            adapter.setTests(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Test>> call, @NonNull Throwable t) {
                        t.printStackTrace();
                    }
                });

                handler.postDelayed(this, 5000);
            }
        };

        handler.post(periodicRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        state = Objects.requireNonNull(testRecycler.getLayoutManager()).onSaveInstanceState();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (state != null) {
            Objects.requireNonNull(testRecycler.getLayoutManager()).onRestoreInstanceState(state);
        }
    }
}