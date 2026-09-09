package net.nikcain.altazgoto;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import net.nikcain.altazgoto.databinding.ActivityMainBinding;

import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDate;
import java.time.temporal.JulianFields;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppDatabase appDatabase;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    AppViewModel model;
    StarCalculations sc = new StarCalculations();
    AdvancedTelescopeAligner AlignmentMgr = new AdvancedTelescopeAligner();
    private double m_lat = 52.6019682;
    private double m_long = -3.0955309;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();

        assert ctx != null;

        appDatabase = Room.databaseBuilder(ctx, AppDatabase.class, "skyObjects")
                .createFromAsset("skyObjects5.db")
                .fallbackToDestructiveMigration(true)
                .build();

        ListenableFuture<List<calibrationstars>> future = appDatabase.targetsDao().getCalibrationStars();
        Futures.addCallback(future,
                new FutureCallback<List<calibrationstars>>() {
                    public void onSuccess(List<calibrationstars> result) {
                        // handle success
                        findThreeBrightStars(result);
                    }

                    public void onFailure(@NonNull Throwable thrown) {
                        // handle failure
                    }
                },
                ctx.getMainExecutor()
        );

        model = new ViewModelProvider(this).get(AppViewModel.class);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.setAppviewmodel(model);
        model.setCalibrationPointSet(0, false);
        model.setCalibrationPointSet(1, false);
        model.setCalibrationPointSet(2, false);

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void findThreeBrightStars(List<calibrationstars> stars) {

        for (calibrationstars star : stars) {

            LocalDate date = LocalDate.now();//  .of(2025, 4, 16);

            // Get Julian day number
            long jd = date.getLong(JulianFields.JULIAN_DAY);

            double[] pos = sc.raDecToAltAz(star.ra, star.dec, m_lat, m_long);

            // don't use stars too close to horizon or too vertically up
            // don't use stars close to 180 Az (makes scope judder)
            if (pos[0] > 30 && pos[0] < 60 && (pos[1] < 170 || pos[1] > 190)) {
                boolean usethisone = true;
                for (AlignmentStar cmp_star : model.getUiState().getValue().chosen) {
                    double[] cmp_pos = sc.raDecToAltAz(cmp_star.baseStar.ra, cmp_star.baseStar.dec, m_lat, m_long);
                    double az_separation = Math.abs(pos[1] - cmp_pos[1]);

                    // don't use stars too close to one another
                    if (az_separation < 70) {
                        usethisone = false;
                    }

                }
                if (usethisone) {
                    // we'll use this one
                    AlignmentStar al_star = new AlignmentStar(pos[0], pos[1], pos[0], pos[1]);
                    al_star.baseStar = star;
                    model.getUiState().getValue().chosen.add(al_star);
                    if (model.getUiState().getValue().chosen.size() > 2) break;
                }
            }
        }
    }
}