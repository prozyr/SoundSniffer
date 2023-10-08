package com.example.soundsniffier;

import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SoundSniffer"; // Dodaj tag debugowania

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        int sampleRate = 44100; // Standardowa częstotliwość próbkowania
        int channelConfig = AudioFormat.CHANNEL_IN_MONO; // Jedno kanałowy
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT; // 16-bitowe dane PCM

        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e(TAG, "Brak uprawnień do nagrywania dźwięku.");

            return;
        }

        AudioRecord audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
        );

        LineChart chart = findViewById(R.id.chart);

        List<Entry> entries = new ArrayList<>();

        audioRecord.startRecording();
        byte[] buffer = new byte[bufferSize];
        int xValue = 0;


        long timeInterval = 5000, timeloop = 0, timeEvent = 0;
        chart.invalidate();

        while (true) { // przechwytywanieAktywne to zmienna kontrolująca przechwytywanie dźwięku
            timeloop = System.currentTimeMillis();


            if( timeloop - timeEvent >= timeInterval) {
                int bytesRead = audioRecord.read(buffer, 0, bufferSize);

                for (int i = 0; i < bytesRead / 2; i++) {
                    int audioSample = (int)((buffer[i * 2] & 0xFF) | (buffer[i * 2 + 1] << 8));
                    // Przetwarzaj próbki audio i dodawaj je jako punkty na wykresie
                    entries.add(new Entry(xValue++, audioSample));
                }

                // Aktualizuj wykres na bieżąco

                LineDataSet dataSet = new LineDataSet(entries, "Data");
                LineData lineData = new LineData(dataSet);
                Log.d(TAG, "Odczytano próbki audio: " + lineData);
                chart.setData(lineData);
                chart.notifyDataSetChanged();
                chart.invalidate();
                timeEvent = timeloop;
            }


            // Log.d(TAG, "Odczytano próbki audio: " + bytesRead);
        }
    }
}
