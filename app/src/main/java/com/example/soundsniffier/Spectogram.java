package com.example.soundsniffier;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.soundsniffier.ReadSound;
import com.example.soundsniffier.SoundDataObserver;
import com.github.mikephil.charting.data.Entry;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Collections;

public class Spectogram extends AppCompatActivity implements SoundDataObserver {
    private ImageView spectogramImageView;
    private ToggleButton startButtonSpec;
    private Button chartButton;
    private Boolean StopSwitchSpec = false;
    private SharedPreferences sharedPreferences;
    private static final String TAG = "SoundSniffer";
    private static final int SAMPLE_RATE = 14000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    private static final int NUM_SAMPLES = 650; // Adjust this value as needed
    private static final int SPECTROGRAM_HEIGHT = 665; // Adjust this value as needed
    private static final int SPECTROGRAM_WIDTH = NUM_SAMPLES / 2; // Half of NUM_SAMPLES
    private int[] pixels;
    private  TextView axisLeft;
    private  TextView axisRight;
    float maxAxis = Float.MAX_VALUE;
    float minAxis  = Float.MIN_VALUE;
    long Old_time = 0;

    private Queue<List<Float>> spectrogramQueue;
    private HandlerThread handlerThread;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectogram);

        spectogramImageView = findViewById(R.id.spectogramImageView);
        startButtonSpec = findViewById(R.id.toggleButton);
        chartButton = findViewById(R.id.ChartButton);
        axisLeft = findViewById(R.id.Axis_Left);
        axisRight = findViewById(R.id.Axis_Right);


        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        // Initialize ReadSound object
        ReadSound readSound = new ReadSound(this);
        // Add this class as an observer
        readSound.addObserver(this);

        // Initialize SharedPreferences with the name "MySettingsSpec"
        sharedPreferences = getSharedPreferences("MySettingsSpec", Spectogram.MODE_PRIVATE);

        // Odczytaj stan przycisku z SharedPreferences i ustaw go na przycisku
        boolean savedToggleButtonState = sharedPreferences.getBoolean("toggleButtonState", false);
        startButtonSpec.setChecked(savedToggleButtonState);

        // Odczytaj stan StopSwitch z SharedPreferences
        StopSwitchSpec = sharedPreferences.getBoolean("StopSwitchState", false);

        chartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Spectogram.this, MainActivity.class);
                //intent.putExtra("StopSwitchStateM", StopSwitch);
                //intent.putExtra("ToggleButtonStateM", startButton.isChecked());
                startActivity(intent);
            }
        });

        startButtonSpec.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Toast.makeText(MainActivity.this, "Switch jest włączony", Toast.LENGTH_LONG).show();
                    StopSwitchSpec = true;
                } else {
                    StopSwitchSpec = false;
                    // Toast.makeText(MainActivity.this, "Switch jest wyłączony", Toast.LENGTH_LONG).show();
                }

                // Zapisz stan przycisku i StopSwitch do SharedPreferences po zmianie
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("toggleButtonState", isChecked);
                editor.putBoolean("StopSwitchState", StopSwitchSpec);
                editor.apply();
            }
        });



        handlerThread = new HandlerThread("SpectrogramHandlerThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        // Start recording
        readSound.startRecording();

        // Initialize spectrogramQueue
        spectrogramQueue = new LinkedList<>();

        // Initialize pixels array
        pixels = new int[SPECTROGRAM_WIDTH * SPECTROGRAM_HEIGHT];
    }

    @Override
    public void onDataReceived(List<Entry> entries, List<Entry> entries2) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (spectrogramQueue) {
                    List<Float> amplitudes = calculateAmplitudes(entries);
                    spectrogramQueue.offer(amplitudes); // Add new entry to the queue
                    while (spectrogramQueue.size() > SPECTROGRAM_HEIGHT - 1) {
                        spectrogramQueue.poll(); // Remove oldest entry if queue is full
                    }

                    // Update spectrogram pixels
                    if (!StopSwitchSpec)
                    {
                        updateSpectrogramPixels(entries);

                    }

                    maxAxis = findMaxValue(entries);
                    minAxis = findMinValue(entries);

                    // Update spectogram image view
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                                if(System.currentTimeMillis() - Old_time > 1000)
                                {
                                    axisRight.setText(String.valueOf(minAxis));
                                    axisLeft.setText(String.valueOf(maxAxis));
                                    Old_time = System.currentTimeMillis();
                                }

                                spectogramImageView.setImageBitmap(Bitmap.createBitmap(pixels, SPECTROGRAM_WIDTH, SPECTROGRAM_HEIGHT, Bitmap.Config.RGB_565));

                        }
                    });
                }
            }
        });
    }

    private List<Float> calculateAmplitudes(List<Entry> entries) {
        List<Float> amplitudes = new ArrayList<>();
        for (Entry entry : entries) {
            amplitudes.add(Math.abs(entry.getY()));
        }
        return amplitudes;
    }

    private void updateSpectrogramPixels(List<Entry> entries) {
        // Move all lines one down
        for (int y = SPECTROGRAM_HEIGHT-1; y > 0; y--) {
            for (int x = 0; x < SPECTROGRAM_WIDTH; x++) {
                pixels[x + y * SPECTROGRAM_WIDTH] = pixels[x + (y - 1) * SPECTROGRAM_WIDTH];
            }
        }

        int index = 0;
        int numBins = SPECTROGRAM_WIDTH;

        // Calculate the number of frequency bins per row
        int binSize = entries.size() / numBins;

        for (int i = 0; i < numBins; i++) {
            int startIndex = i * binSize;
            int endIndex = (i + 1) * binSize;
            if (endIndex >= entries.size()) {
                endIndex = entries.size() - 1;
            }

            // Calculate average amplitude for the current frequency bin
            float sumAmplitude = 0;
            for (int j = startIndex; j <= endIndex; j++) {
                sumAmplitude += Math.abs(entries.get(j).getY());
            }
            float avgAmplitude = sumAmplitude / (endIndex - startIndex + 1);

            // Calculate color for the current amplitude
            int color = calculateColor(avgAmplitude);

            // Fill the first row with the new data
            pixels[index++] = color;
        }
    }

    public float findMaxValue(List<Entry> entries) {
        float maxValue = Float.MIN_VALUE;

        for (Entry entry : entries) {
            float value = entry.getX();
            if (value > maxValue) {
                maxValue = value;
            }
        }

        return maxValue;
    }

    public float findMinValue(List<Entry> entries) {
        float minValue = Float.MAX_VALUE;

        for (Entry entry : entries) {
            float value = entry.getX();
            if (value < minValue) {
                minValue = value;
            }
        }

        return minValue;
    }

    private int calculateColor(float amplitude) {
        // Adjust color intensity based on amplitude
        int intensity = (int) (amplitude*3);
        if (intensity > 255) intensity = 255;
        return 0xFF000000 | (intensity << 16) | (intensity << 8) | intensity; // RGB color with alpha set to 255
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerThread.quitSafely();
    }
}
