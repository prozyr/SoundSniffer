package com.example.soundsniffier;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;

<<<<<<< Updated upstream
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
=======
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
>>>>>>> Stashed changes

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import com.example.soundsniffier.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

<<<<<<< Updated upstream
    private AppBarConfiguration mAppBarConfiguration;
=======
    private static final String TAG = "SoundSniffer";
    private static final int PERMISSION_RECORD_AUDIO = 1;
    private LineChart chart;

    private BarChart barChart;
    private AudioRecord audioRecord;
    private HandlerThread audioThread;
    private Handler audioHandler;
    private List<Entry> entries = new ArrayList<>();

    private List<Integer> entriesData = new ArrayList<>();

    private List<BarEntry> barEntries = new ArrayList<>();
    private int xValue = 0;
>>>>>>> Stashed changes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

<<<<<<< Updated upstream
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        if (binding.appBarMain.fab != null) {
            binding.appBarMain.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show());
=======
        chart = findViewById(R.id.chart);
        barChart = findViewById(R.id.barChart);

        int sampleRate = 4000;  // TODO:
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, PERMISSION_RECORD_AUDIO);
        } else {
            initializeAudioRecording(sampleRate, channelConfig, audioFormat, bufferSize);
>>>>>>> Stashed changes
        }
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        NavigationView navigationView = binding.navView;
        if (navigationView != null) {
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_transform, R.id.nav_reflow, R.id.nav_slideshow, R.id.nav_settings)
                    .setOpenableLayout(binding.drawerLayout)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
        }

<<<<<<< Updated upstream
        BottomNavigationView bottomNavigationView = binding.appBarMain.contentMain.bottomNavView;
        if (bottomNavigationView != null) {
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_transform, R.id.nav_reflow, R.id.nav_slideshow)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
=======
        audioThread = new HandlerThread("AudioThread");
        audioThread.start();
        audioHandler = new Handler(audioThread.getLooper());


        audioHandler.post(new Runnable() {
            @Override
            public void run() {
                audioRecord.startRecording();
                byte[] buffer = new byte[bufferSize];

                while (!Thread.currentThread().isInterrupted()) {
                    int bytesRead = audioRecord.read(buffer, 0, bufferSize);

                    synchronized (lock) {
                        for (int i = 0; i < bytesRead / 2; i++) {
                            int audioSample = (int) ((buffer[i * 2] & 0xFF) | (buffer[i * 2 + 1] << 8));
                            entries.add(new Entry(xValue++, audioSample));
                            entriesData.add(new Integer(audioSample));          // Clean read data
                        }

                            Map<Integer, Integer> uniqueValues = new HashMap<>();
                                // Counting unique values

                            for(Integer histSample : entriesData)
                            {
                                if(histSample !=0) {
                                    if (uniqueValues.containsKey(histSample)) {
                                        uniqueValues.put(histSample, uniqueValues.get(histSample) + 1);
                                    }
                                    else {
                                        uniqueValues.put(histSample, 1);
                                    }
                                }
                            }

                            for(Map.Entry<Integer,Integer> histogram: uniqueValues.entrySet()) {
                                barEntries.add(new BarEntry(histogram.getKey()+1,histogram.getValue()));
                            }



                        if (entries.size() > bufferSize*4) { // TODO: LENGHT of dataset to draw in chart
                            for (int i = 0; i < bytesRead; i++)
                                entries.remove(0);
                                entriesData.remove(0);
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LineDataSet dataSet;
                            BarDataSet barDataSet;
                            synchronized (lock) {
                                dataSet = new LineDataSet(new ArrayList<>(entries), "Data"); // Tworzymy kopię listy
                                barDataSet = new BarDataSet(new ArrayList<>(barEntries),"LOL XD");
                            }

                            LineData lineData = new LineData(dataSet);
                            chart.setData(lineData);
                            chart.notifyDataSetChanged();
                            chart.invalidate();
                            BarData barData = new BarData(barDataSet);
                            barChart.setData(barData);
                            barChart.notifyDataSetChanged();
                            barChart.invalidate();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioThread.quitSafely();
>>>>>>> Stashed changes
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        // Using findViewById because NavigationView exists in different layout files
        // between w600dp and w1240dp
        NavigationView navView = findViewById(R.id.nav_view);
        if (navView == null) {
            // The navigation drawer already has the items including the items in the overflow menu
            // We only inflate the overflow menu if the navigation drawer isn't visible
            getMenuInflater().inflate(R.menu.overflow, menu);
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_settings) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_settings);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}