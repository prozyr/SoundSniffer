// icon play button By Freepik
// icon pause button By inkubators
package com.example.soundsniffier;

import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.content.SharedPreferences;
import java.io.Serializable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import android.view.MotionEvent;
import android.widget.TextView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import org.jtransforms.fft.DoubleFFT_1D;

public class MainActivity extends AppCompatActivity implements SoundDataObserver {

    private static final String TAG = "SoundSniffer";

    private LineChart chart;
    private LineChart chart2;
    private BarChart barChart;
    private Button SpecButton;
    private ToggleButton startButton;
    private Boolean StopSwitch = false;
    private AudioRecord audioRecord;
    private HandlerThread audioThread;
    private Handler audioHandler;
    public List<Entry> entries = new ArrayList<>();

    static public List<Entry> entries2 = new ArrayList<>();
    static public float get_X_Value;
    static public float get_Y_Value;
    public float touchX;
    public float touchY;
    String infoText = "";
    static public List<Entry> specData = new ArrayList<>();

    private ArrayList barEntriesArrayList;
    BarData barData;
    private int xValue = 0;
    private double[] window;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chart = findViewById(R.id.chart);
        chart2 = findViewById(R.id.mychart2);
        SpecButton = findViewById(R.id.SpecButton);
        startButton = findViewById((R.id.toggleButton));

        // Inicjalizacja obiektu ReadSound
        ReadSound readSound = new ReadSound(this);
        // Dodanie tej klasy jako obserwatora
        readSound.addObserver(this);

        // Inicjalizacja SharedPreferences z nazwą pliku "MojeUstawienia"
        sharedPreferences = getSharedPreferences("MojeUstawienia", MainActivity.MODE_PRIVATE);

        // Odczytaj stan przycisku z SharedPreferences i ustaw go na przycisku
        boolean savedToggleButtonState = sharedPreferences.getBoolean("toggleButtonState", false);
        startButton.setChecked(savedToggleButtonState);

        // Odczytaj stan StopSwitch z SharedPreferences
        StopSwitch = sharedPreferences.getBoolean("StopSwitchState", false);


/*        // Działa słabo lepiej użyć Shared Preferences albo przekazywać dane do innej klasy
        if (savedInstanceState != null) {
            savedToggleButtonState = savedInstanceState.getBoolean("toggleButtonState", false);
            boolean savedStopSwitchState = savedInstanceState.getBoolean("StopSwitchState", false);

            startButton.setChecked(savedToggleButtonState);
            StopSwitch = savedStopSwitchState;
        }*/


        SpecButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Spectogram.class);
                //intent.putExtra("StopSwitchStateM", StopSwitch);
                //intent.putExtra("ToggleButtonStateM", startButton.isChecked());
                startActivity(intent);
            }
        });

        startButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "Switch jest włączony", Toast.LENGTH_LONG).show();
                    StopSwitch = true;
                } else {
                    StopSwitch = false;
                    Toast.makeText(MainActivity.this, "Switch jest wyłączony", Toast.LENGTH_LONG).show();
                }

                // Zapisz stan przycisku i StopSwitch do SharedPreferences po zmianie
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("toggleButtonState", isChecked);
                editor.putBoolean("StopSwitchState", StopSwitch);
                editor.apply();
            }
        });





        TextView infoTextView = findViewById(R.id.infoTextView);
        TextView infoTextView2 = findViewById(R.id.infoTextView2);

        // Dodaj onTouchListener do chart i chart2
        chart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                touchX = event.getX();
                touchY = event.getY();
                Entry entry = chart.getEntryByTouchPoint(touchX, touchY);
                if (entry != null) {
                    String infoText = "Zaznaczono punkt - X: " + entry.getX() + ", Y: " + entry.getY();
                    infoTextView.setText(infoText);
                }
                return false;
            }
        });

        chart2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float touchX = event.getX();
                float touchY = event.getY();
                Entry entry = chart2.getEntryByTouchPoint(touchX, touchY);
                if (entry != null) {
                    String infoText = "Zaznaczono punkt - X: " + entry.getX() + ", Y: " + entry.getY();
                    infoTextView2.setText(infoText);
                }
                return false;
            }
        });

        // Rozpoczęcie nagrywania
        readSound.startRecording();
    }
    private Object lock = new Object(); // Object for synchronization
    @Override
    public void onDataReceived(List<Entry> entries, List<Entry> entries2) {
        // Ta metoda zostanie wywołana, gdy dane zostaną odebrane od obserwatora (ReadSound)
        // Tutaj możesz zaktualizować widoki lub inne operacje na danych
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LineDataSet dataSet, dataSet2;

                synchronized (lock) {
                    // Create copies of the lists inside the synchronized block
                    dataSet = new LineDataSet(new ArrayList<>(entries), "Data");
                    dataSet2 = new LineDataSet(new ArrayList<>(entries2), "Data");
                }

                LineData lineData = new LineData(dataSet);
                LineData lineData2 = new LineData(dataSet2);

                if (!StopSwitch) {
                    // Update chart data and refresh
                    chart.setData(lineData);
                    chart.notifyDataSetChanged();
                    chart.invalidate();

                    // Update second chart data and refresh
                    chart2.setData(lineData2);
                    chart2.notifyDataSetChanged();
                    chart2.invalidate();


                    if(touchX != 0.0f)
                    {
                        highlightPoint(chart, touchX, entries);
                    }

/*                    // Retrieve X and Y values from the second chart
                    float get_X_Value = chart2.getX();
                    float get_Y_Value = chart2.getY();

                    // Add the new entry to specData
                    specData.add(new Entry(get_Y_Value, get_X_Value));*/
                }
/*
                TextView infoTextView1 = findViewById(R.id.infoTextView);

                // Załóżmy, że touchX to pozycja dotknięcia na wykresie
                Entry entryForTouchX = dataSet.getEntryForXValue(touchX, Float.NaN);

                if (entryForTouchX != null) {
                    float xValue = entryForTouchX.getX();
                    float yValue = entryForTouchX.getY();

                    // Teraz masz prawidłowe wartości X i Y
                    infoText = "Zaznaczono punkt - X: " + xValue + ", Y: " + yValue;
                } else {
                    infoText = "Zaznaczono punkt - X: " + touchX + ", Y: Brak";
                }
                infoTextView1.setText(infoText);*/


            }
        });
    }

    private void highlightPoint(LineChart chart, float touchX, List<Entry> entries) {
        TextView infoTextView1 = findViewById(R.id.infoTextView);
        // Automatyczne zaznaczanie punktu na podstawie wartości X
        Entry entryForTouchX = getEntryForXWithMargin(entries, touchX, 5f);
        if (entryForTouchX != null) {
            chart.highlightValue(entryForTouchX.getX(), entryForTouchX.getY(), 0);
            String infoText = "Zaznaczono punkt - X: " + entryForTouchX.getX() + ", Y: " + entryForTouchX.getY();
            // Ustaw informacje w odpowiednim TextView
            // (możesz użyć findViewWithTag dla dynamicznego znajdowania odpowiedniego TextView)
             infoTextView1.setText(infoText);
        }
    }

    private Entry getEntryForXWithMargin(List<Entry> entries, float xValue, float marginError) {
        for (Entry entry : entries) {
            // Sprawdź, czy wartość X jest w zakresie z uwzględnieniem marginesu błędu
            if (Math.abs(entry.getX() - xValue) <= marginError) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            int sampleRate = 5000; // 44100
            int channelConfig = AudioFormat.CHANNEL_IN_MONO;
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            //initializeAudioRecording(sampleRate, channelConfig, audioFormat, bufferSize);
            Toast.makeText(this, "Nagrywanie Działa ", Toast.LENGTH_SHORT).show();

        } else {
            Log.e(TAG, "Brak uprawnień do nagrywania dźwięku.");
        }
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean("toggleButtonState", startButton.isChecked());
        savedInstanceState.putBoolean("StopSwitchState", StopSwitch);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        startButton.setChecked(savedInstanceState.getBoolean("toggleButtonState"));
        StopSwitch = savedInstanceState.getBoolean("StopSwitchState");
    }
}