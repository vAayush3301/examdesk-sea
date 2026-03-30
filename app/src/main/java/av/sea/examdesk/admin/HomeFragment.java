package av.sea.examdesk.admin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView testRecycler;
    private LinearLayout noDataLayout;

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

        refreshLayout = view.findViewById(R.id.swipeRefresh);
        noDataLayout = view.findViewById(R.id.noDataLayout);
        testRecycler = view.findViewById(R.id.testList);
        testRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));

        List<Test> tests = new ArrayList<>();
        adapter = new TestRecyclerAdapter(requireContext(), tests, api);
        testRecycler.setAdapter(adapter);

        startPeriodicTask(api);

        ExtendedFloatingActionButton newTestButton = view.findViewById(R.id.create_new_test);
        newTestButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), NewTestActivity.class)));

        refreshLayout.setOnRefreshListener(() -> loadData(api));
    }

    private void startPeriodicTask(ApiService api) {
        Runnable periodicRunnable = new Runnable() {
            @Override
            public void run() {
                loadData(api);

                handler.postDelayed(this, 120000);
            }
        };

        handler.post(periodicRunnable);
    }

    private void loadData(ApiService api) {
        api.getTests(Statics.CLIENT_ID).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Test>> call, @NonNull Response<List<Test>> response) {
                if (response.isSuccessful()) {
                    List<Test> tests = response.body();
                    if (tests == null || tests.isEmpty()) {
                        refreshLayout.setVisibility(View.GONE);
                        noDataLayout.setVisibility(View.VISIBLE);
                        testRecycler.setVisibility(View.GONE);
                    } else {
                        refreshLayout.setVisibility(View.VISIBLE);
                        noDataLayout.setVisibility(View.GONE);
                        testRecycler.setVisibility(View.VISIBLE);
                        adapter.setTests(tests);
                    }
                }
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<List<Test>> call, @NonNull Throwable t) {
                refreshLayout.setRefreshing(false);
                Log.e("LOAD TEST", "FAILED TO LOAD TEST: " + t.getMessage());
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
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