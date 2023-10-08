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


import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SoundSniffer";
    private static final int PERMISSION_RECORD_AUDIO = 1;
    private LineChart chart;
    private AudioRecord audioRecord;
    private HandlerThread audioThread;
    private Handler audioHandler;
    private List<Entry> entries = new ArrayList<>();

    private int xValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chart = findViewById(R.id.chart);

        int sampleRate = 8000;  // TODO: 
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, PERMISSION_RECORD_AUDIO);
        } else {
            initializeAudioRecording(sampleRate, channelConfig, audioFormat, bufferSize);
        }
    }
    private Object lock = new Object(); // Obiekt do synchronizacji
    private void initializeAudioRecording(int sampleRate, int channelConfig, int audioFormat, int bufferSize) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
        );

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
                        }

                        if (entries.size() > bufferSize*20) { // TODO: LENGHT of dataset to draw in chart
                            for (int i = 0; i < bytesRead; i++)
                                entries.remove(0);
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LineDataSet dataSet;
                            synchronized (lock) {
                                dataSet = new LineDataSet(new ArrayList<>(entries), "Data"); // Tworzymy kopię listy
                            }
                            LineData lineData = new LineData(dataSet);
                            chart.setData(lineData);
                            chart.notifyDataSetChanged();
                            chart.invalidate();
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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_RECORD_AUDIO && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            int sampleRate = 44100;
            int channelConfig = AudioFormat.CHANNEL_IN_MONO;
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            initializeAudioRecording(sampleRate, channelConfig, audioFormat, bufferSize);
        } else {
            Log.e(TAG, "Brak uprawnień do nagrywania dźwięku.");
        }
    }
}

