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
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
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
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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
    public List<Entry> logEntries = new ArrayList<>();
    static public float get_X_Value;
    static public float get_Y_Value;
    public float touchX;
    public float touchY;
    public  float max_1_Y = 0;
    public  float max_2_Y = 0;
    public  float min_1_Y = 1000;
    public  float min_2_Y = 1000;
    String infoText = "";
    static public List<Entry> specData = new ArrayList<>();

    private ArrayList barEntriesArrayList;
    BarData barData;
    private int xValue = 0;
    private double[] window;
    private SharedPreferences sharedPreferences;

    /////////////////////////////////////////////////
    LineDataSet dataSet, dataSet2;
    float chart1_max_Y, chart2_max_Y, chart1_min_Y, chart2_min_Y;
    YAxis yAxis1;
    YAxis yAxis2;
    YAxis yAxis3;
    YAxis yAxis4;
    YAxis yAxis30;
    YAxis yAxis40;
    YAxis yAxis300;
    YAxis yAxis400;
    LineData lineData;
    LineData lineData2;
    ////////////////////////////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        chart = findViewById(R.id.chart);
        chart2 = findViewById(R.id.mychart2);
        SpecButton = findViewById(R.id.SpecButton);
        startButton = findViewById((R.id.toggleButton));


        int textSize = 10;

        chart.getXAxis().setTextColor(Color.WHITE);  // Ustawia kolor tekstu na osi X
        chart.getXAxis().setAxisLineColor(Color.WHITE);  // Ustawia kolor linii osi X
        chart.getAxisLeft().setTextColor(Color.WHITE);  // Ustawia kolor tekstu na osi Y (lewej)
        chart.getAxisLeft().setAxisLineColor(Color.WHITE);  // Ustawia kolor linii osi Y (lewej)
        chart.getAxisRight().setTextColor(Color.WHITE);  // Ustawia kolor tekstu na osi Y (prawej)
        chart.getAxisRight().setAxisLineColor(Color.WHITE);
        chart.getDescription().setTextColor(Color.WHITE);
        chart.getAxisLeft().setTextSize(textSize);
        chart.getAxisRight().setTextSize(textSize);
        chart.getXAxis().setTextSize(textSize);
        chart.getDescription().setText("Histogram");
        chart.getDescription().setTextSize(textSize);
        chart.getLegend().setTextColor(Color.WHITE);


        chart2.getXAxis().setTextColor(Color.WHITE);  // Ustawia kolor tekstu na osi X
        chart2.getXAxis().setAxisLineColor(Color.WHITE);  // Ustawia kolor linii osi X
        chart2.getAxisLeft().setTextColor(Color.WHITE);  // Ustawia kolor tekstu na osi Y (lewej)
        chart2.getAxisLeft().setAxisLineColor(Color.WHITE);  // Ustawia kolor linii osi Y (lewej)
        chart2.getAxisRight().setTextColor(Color.WHITE);  // Ustawia kolor tekstu na osi Y (prawej)
        chart2.getAxisRight().setAxisLineColor(Color.WHITE);
        chart2.getDescription().setTextColor(Color.WHITE);
        chart2.getAxisLeft().setTextSize(textSize);
        chart2.getAxisRight().setTextSize(textSize);
        chart2.getXAxis().setTextSize(textSize);
        chart2.getDescription().setText("Przebieg");
        chart2.getDescription().setTextSize(textSize);
        chart2.getLegend().setTextColor(Color.WHITE);
        synchronized (lock) {
            dataSet = new LineDataSet(null, "Data");
            dataSet2 = new LineDataSet(null, "Data");
            lineData = new LineData(dataSet);
            lineData2 = new LineData(dataSet2);
        }
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
                   // Toast.makeText(MainActivity.this, "Switch jest włączony", Toast.LENGTH_LONG).show();
                    StopSwitch = true;
                } else {
                    StopSwitch = false;
                   // Toast.makeText(MainActivity.this, "Switch jest wyłączony", Toast.LENGTH_LONG).show();
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
                    touchX = entry.getX();

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

                int bagno = 0;

                synchronized (lock) {

                    dataSet.clear();
                    dataSet2.clear();

                    for (Entry entry: entries) {
                        dataSet.addEntry(entry);
                    }
                    for (Entry entry: entries2) {
                        dataSet2.addEntry(entry);
                        bagno ++;
                    }

                    lineData.clearValues();
                    lineData.addDataSet(dataSet);
                    lineData2.clearValues();
                    lineData2.addDataSet(dataSet2);

                }
                chart1_max_Y = findMaxValue(entries);
                chart2_max_Y = findMaxValue(entries2);

                chart1_min_Y = findMinValue(entries);
                chart2_min_Y = findMinValue(entries2);

                logEntries.clear();
                // Toast.makeText(MainActivity.this, "min: " + min_1_Y + " max: " + max_1_Y, Toast.LENGTH_SHORT).show();
                 //Toast.makeText(MainActivity.this, "min: " + min_2_Y + " max: " + max_2_Y, Toast.LENGTH_SHORT).show();

                // Find max
                if(chart1_max_Y > max_1_Y)
                {
                    max_1_Y = chart1_max_Y;
                    yAxis1 = chart.getAxisLeft();
                    yAxis2 = chart.getAxisRight();
                    yAxis1.setAxisMaximum(max_1_Y);
                    yAxis2.setAxisMaximum(max_1_Y);

                }

                // Find min
                if(chart1_min_Y < min_1_Y)
                {
                    min_1_Y = chart1_min_Y;
                    yAxis3 = chart.getAxisLeft();
                    yAxis4 = chart.getAxisRight();
                    yAxis3.setAxisMinimum(min_1_Y);
                    yAxis4.setAxisMinimum(min_1_Y);

                }


                // Find max
                if (chart2_max_Y > max_2_Y) {
                    max_2_Y = chart2_max_Y;
                }

                // Find min
                if (chart2_min_Y < min_2_Y) {
                    min_2_Y = chart2_min_Y;
                }

                if(max_2_Y > Math.abs(min_2_Y))
                {
                    min_2_Y = -max_2_Y;
                    yAxis30 = chart2.getAxisLeft();
                    yAxis40 = chart2.getAxisRight();
                    yAxis30.setAxisMinimum(Math.min(min_2_Y, -max_2_Y));
                    yAxis40.setAxisMinimum(Math.min(min_2_Y, -max_2_Y));
                    yAxis30.setAxisMaximum(Math.max(max_2_Y, -min_2_Y));
                    yAxis40.setAxisMaximum(Math.max(max_2_Y, -min_2_Y));
                }
                else if (max_2_Y < Math.abs(min_2_Y))
                {
                    max_2_Y = Math.abs(min_2_Y);
                    yAxis30 = chart2.getAxisLeft();
                    yAxis40 = chart2.getAxisRight();
                    yAxis30.setAxisMinimum(Math.min(min_2_Y, -max_2_Y));
                    yAxis40.setAxisMinimum(Math.min(min_2_Y, -max_2_Y));
                    yAxis30.setAxisMaximum(Math.max(max_2_Y, -min_2_Y));
                    yAxis40.setAxisMaximum(Math.max(max_2_Y, -min_2_Y));
                }
                else
                {
                    min_2_Y += 20;
                    max_2_Y -= 20;
                    yAxis300 = chart2.getAxisLeft();
                    yAxis400 = chart2.getAxisRight();
                    yAxis300.setAxisMinimum(Math.min(min_2_Y, -max_2_Y));
                    yAxis400.setAxisMinimum(Math.min(min_2_Y, -max_2_Y));
                    yAxis300.setAxisMaximum(Math.max(max_2_Y, -min_2_Y));
                    yAxis400.setAxisMaximum(Math.max(max_2_Y, -min_2_Y));
                }


               // Toast.makeText(MainActivity.this,"After "  + "min: " + min_2_Y + " max: " + max_2_Y, Toast.LENGTH_SHORT).show();



                if (!StopSwitch) {
                    // Update chart data and refresh
                    chart.setData(lineData);
                    chart.notifyDataSetChanged();
                    chart.invalidate();

                    // Update second chart data and refresh
                    chart2.setData(lineData2);
                    chart2.notifyDataSetChanged();
                    chart2.invalidate();

                    // Set visible range before moving view
                    chart2.setVisibleXRangeMaximum(bagno-1000); // Set the visible number of points on the X axis
                    chart2.moveViewToX(lineData2.getXMax() - bagno+1000); //

                    if(touchX != 0.0f)
                    {
                        highlightPoint(chart, touchX, entries);
                    }
                }
            }
        });
    }

    public List<Entry> LogEntries(List<Entry> entries) {

        List<Entry> LogEntries = new ArrayList<>();
        float LogY = 0;

        for (Entry entry : entries) {
            //Toast.makeText(this, "Entry Y: " + entry.getY(), Toast.LENGTH_SHORT).show();
            if(entry.getY() > 0)
            {
                LogY = (float) Math.log(entry.getY()) / (float)Math.log(10);
            }
            else if(entry.getY() < 0)
            {
                LogY = -1*((float) Math.log(Math.abs(entry.getY())) / (float)Math.log(10));
            }
            else if (entry.getY() == 0)
            {
                LogY = 0.0f;
            }
            //Toast.makeText(this,"Entry Y: " + entry.getY() +  " Log Y: " + LogY, Toast.LENGTH_SHORT).show();
            LogEntries.add(new Entry(entry.getX(),LogY));

            }
        return LogEntries;
    }

    public float findMaxValue(List<Entry> entries) {
        float maxValue = Float.MIN_VALUE;

        for (Entry entry : entries) {
            float value = entry.getY();
            if (value > maxValue) {
                maxValue = value;
            }
        }

        return maxValue;
    }

    public float findMinValue(List<Entry> entries) {
        float minValue = Float.MAX_VALUE;

        for (Entry entry : entries) {
            float value = entry.getY();
            if (value < minValue) {
                minValue = value;
            }
        }

        return minValue;
    }

private void highlightPoint(LineChart chart, float touchX, List<Entry> entries) {
    TextView infoTextView1 = findViewById(R.id.infoTextView);

    for (Entry entry : entries) {
        if (entry != null) {
            if (entry.getX() == touchX) {
                chart.highlightValue(entry.getX(), entry.getY(), 0);
                String infoText = "Zaznaczono punkt - X: " + entry.getX() + ", Y: " + entry.getY();
                //Toast.makeText(this, "Znaleziono X ", Toast.LENGTH_SHORT).show();
                infoTextView1.setText(infoText);
                return; // Zatrzymaj pętlę po znalezieniu odpowiedniego punktu
            }
        }
    }

    // Jeśli nie znaleziono punktu o wartości X równą touchX
    //Toast.makeText(this, "Nie znaleziono punktu o wartości X: " + touchX, Toast.LENGTH_SHORT).show();
}


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            int sampleRate = 44100/2; // 44100
            int channelConfig = AudioFormat.CHANNEL_IN_MONO;
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            //initializeAudioRecording(sampleRate, channelConfig, audioFormat, bufferSize);
            //Toast.makeText(this, "Nagrywanie Działa ", Toast.LENGTH_SHORT).show();

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