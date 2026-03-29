package av.sea.examdesk.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.navigation.NavigationView;

import av.sea.examdesk.LoginActivity;
import av.sea.examdesk.R;
import av.sea.examdesk.user.MainActivity;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, new HomeFragment())
                .commit();

        DrawerLayout drawer = findViewById(R.id.main);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.open,
                R.string.close
        );

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView nav = findViewById(R.id.nav);
        nav.setNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.logout) {
                SharedPreferences preferences = getSharedPreferences("ExamDesk", MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = preferences.edit();
                prefsEditor.clear();
                prefsEditor.apply();
                startActivity(new Intent(AdminActivity.this, LoginActivity.class));
                finish();
                return true;
            } else if (menuItem.getItemId() == R.id.about) {
                //TODO create about page
                return true;
            }

            return false;
        });
    }
}