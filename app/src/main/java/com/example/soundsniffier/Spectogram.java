package com.example.soundsniffier;
import com.example.soundsniffier.R.id;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineDataSet;

import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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


import java.util.ArrayList;
import java.util.List;


public class Spectogram extends AppCompatActivity implements SoundDataObserver {
    private LineChart SpecChart;
    private ToggleButton startButtonSpec;
    private Boolean StopSwitchSpec = false;
    private SharedPreferences sharedPreferences;
    private static final String TAG = "SoundSniffer";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectogram);

        SpecChart =  findViewById(R.id.Spectogram);
        startButtonSpec = findViewById(id.toggleButton);

        // Inicjalizacja obiektu ReadSound
        ReadSound readSound = new ReadSound(this);
        // Dodanie tej klasy jako obserwatora
        readSound.addObserver(this);

        // Inicjalizacja SharedPreferences z nazwą pliku "MojeUstawienia"
        sharedPreferences = getSharedPreferences("MojeUstawieniaSpec", Spectogram.MODE_PRIVATE);

        // Odczytaj stan przycisku z SharedPreferences i ustaw go na przycisku
        boolean savedToggleButtonState = sharedPreferences.getBoolean("toggleButtonState", false);
        startButtonSpec.setChecked(savedToggleButtonState);

        // Odczytaj stan StopSwitch z SharedPreferences
        StopSwitchSpec = sharedPreferences.getBoolean("StopSwitchState", false);



        // Utwórz Intention i przekaż dane (jeśli potrzebne)
        Intent intent = new Intent(Spectogram.this, MainActivity.class);

        Button chartButton = findViewById(R.id.ChartButton);
        chartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(intent);

            }
        });

        startButtonSpec.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(Spectogram.this, "Switch jest włączony", Toast.LENGTH_LONG).show();
                    StopSwitchSpec = true;
                } else {
                    StopSwitchSpec = false;
                    Toast.makeText(Spectogram.this, "Switch jest wyłączony", Toast.LENGTH_LONG).show();
                }

                // Zapisz stan przycisku i StopSwitch do SharedPreferences po zmianie
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("toggleButtonState", isChecked);
                editor.putBoolean("StopSwitchState", StopSwitchSpec);
                editor.apply();
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
                    //dataSet = new LineDataSet(new ArrayList<>(entries), "Data");
                    dataSet2 = new LineDataSet(new ArrayList<>(entries2), "Data");
                }

                //LineData lineData = new LineData(dataSet);
                LineData lineData2 = new LineData(dataSet2);

                if (!StopSwitchSpec) {

                    // Update second chart data and refresh
                    SpecChart.setData(lineData2);
                    SpecChart.notifyDataSetChanged();
                    SpecChart.invalidate();

/*                    // Retrieve X and Y values from the second chart
                    float get_X_Value = chart2.getX();
                    float get_Y_Value = chart2.getY();

                    // Add the new entry to specData
                    specData.add(new Entry(get_Y_Value, get_X_Value));*/
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            int sampleRate = 5000; //44100
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

        //savedInstanceState.putBoolean("toggleButtonState", startButton.isChecked());
        //savedInstanceState.putBoolean("StopSwitchState", StopSwitch);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //startButton.setChecked(savedInstanceState.getBoolean("toggleButtonState"));
        //StopSwitch = savedInstanceState.getBoolean("StopSwitchState");
    }

}
