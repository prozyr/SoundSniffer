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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SoundSniffer";

    private static final int PERMISSION_RECORD_AUDIO = 1;
    private LineChart chart;
    private LineChart chart2;
    private BarChart barChart;
    private Button SpecButton;
    protected ToggleButton startButton;
    protected Boolean StopSwitch=false;
    private AudioRecord audioRecord;
    private HandlerThread audioThread;
    private Handler audioHandler;
    public List<Entry> entries = new ArrayList<>();

    public  List<Entry> entries2 = new ArrayList<>();

    private ArrayList barEntriesArrayList;
    BarData barData;
    private int xValue = 0;
    private double[] window;

    private double[] applyWindow(short[] input) {
        double[] res = new double[input.length];

        buildHammWindow(input.length);
        for(int i = 0; i < input.length; ++i) {
            res[i] = (double)input[i] * window[i];
        }
        return res;
    }
    private void buildHammWindow(int size) {
        if(window != null && window.length == size) {
            return;
        }
        window = new double[size];
        for(int i = 0; i < size; ++i) {
            window[i] = .54 - .46 * Math.cos(2 * Math.PI * i / (size - 1.0));
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        chart = findViewById(R.id.chart);
        chart2 = findViewById(R.id.mychart2);


        startButton = findViewById((R.id.toggleButton));
        if (savedInstanceState != null) {
       //     boolean toggleButtonState = savedInstanceState.getBoolean("toggleButtonState", true);
       //     startButton.setChecked(toggleButtonState);
       //     StopSwitch = savedInstanceState.getBoolean("StopSwitchState", false); // Ustaw stan StopSwitch z zapisanego stanu


        }

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean savedToggleButtonState = preferences.getBoolean("toggleButtonState", false);
        boolean savedStopSwitchState = preferences.getBoolean("StopSwitchState", false);

        startButton.setChecked(savedToggleButtonState);
        StopSwitch = savedStopSwitchState;

        SpecButton =  findViewById(R.id.SpecButton);
        SpecButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, Spectogram.class);
                intent.putExtra("StopSwitchState", StopSwitch);
                intent.putExtra("ToggleButtonState", startButton.isChecked());

                startActivity(intent);
                //    Log.d("BUTTONS", "User tapped the Supabutton");
                //finish();
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
        }
    });


        int sampleRate = 9000;  // TODO:
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
                        List<Short> data_toHist = new ArrayList<>(); // TODO: Data for histogram
                        for (int i = 0; i < bytesRead / 2; i++) {
                            int audioSample = (int) ((buffer[i * 2] & 0xFF) | (buffer[i * 2 + 1] << 8));
                            /////////////////entries.add(new Entry(xValue++, audioSample)); // TODO: Data for histogram
                            entries2.add(new Entry(xValue++, (int)audioSample));
                            data_toHist.add((short)audioSample);
                        }
                        short[] datatoHist = new short[data_toHist.size()];

                        for (int i = 0; i < data_toHist.size(); i++) {
                            datatoHist[i] = data_toHist.get(i);
                        }

                        // START TODO: Workspace for histogram
                        DoubleFFT_1D fft = new DoubleFFT_1D(datatoHist.length + 24 * datatoHist.length);
                        double[] a = new double[(datatoHist.length + 24 * datatoHist.length) * 2];

                        System.arraycopy(applyWindow(datatoHist), 0, a, 0, datatoHist.length);
                        fft.realForward(a);

                        /* find the peak magnitude and it's index */
                        double maxMag = Double.NEGATIVE_INFINITY;
                        int maxInd = -1;

                        double[] frequency = new double[a.length/2];
                        double[] magnitude = new double[a.length/2];

                        for(int i = 0; i < a.length / 2; ++i) {
                            double re  = a[2*i];
                            double im  = a[2*i+1];
                            double mag = Math.sqrt(re * re + im * im);
                            double freq = (double)sampleRate * i / (a.length / 2);

                            if(mag > maxMag) {
                                maxMag = mag;
                                maxInd = i;
                            }

                            frequency[i] = freq;
                            magnitude[i] = mag;
                        }
                        for (int i = 0; i < magnitude.length; i++) {
                            // Upewnij się, że magnitudy nie są równe zeru, aby uniknąć błędów logarytmicznych
                            if (magnitude[i] != 0) {
                                magnitude[i] = 10 * Math.log10(magnitude[i]+1); // Logarytm dziesiętny
                            } else {
                                magnitude[i] = 0; // Wartość -∞ w przypadku magnitudy równego zeru
                            }
                        }
                        entries.clear();
                        for(int i = 0; i < a.length / 4; ++i) {
                            entries.add(new Entry((float)frequency[i],(float)magnitude[i]));
                        }

                        // END TODO: Workspace for histogram

                        if (entries2.size() > bufferSize*20) { // TODO: LENGHT of dataset to draw in chart
                            for (int i = 0; i < bytesRead; i++)
                                entries2.remove(0);
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LineDataSet dataSet,dataSet2;
                            BarDataSet barDataSet;
                            synchronized (lock) {
                                dataSet = new LineDataSet(new ArrayList<>(entries), "Data"); // Tworzymy kopię listy
                                dataSet2 = new LineDataSet(new ArrayList<>(entries2), "Data");

                                //barDataSet = new BarDataSet(barEntriesArrayList,"Data");
                            }

                            LineData lineData = new LineData(dataSet);
                            LineData lineData2 = new LineData(dataSet2);
                            //barData = new BarData(barDataSet);



                                if(!StopSwitch) {

                                    chart.setData(lineData);
                                    chart.notifyDataSetChanged();
                                    chart.invalidate();

                                //    barChart.setData(barData);
                                //    barChart.notifyDataSetChanged();
                                //    barChart.invalidate();

                                    chart2.setData(lineData2);
                                    chart2.notifyDataSetChanged();
                                    chart2.invalidate();


                                }
                        }
                    });
                }
            }
        });
    }

  //  @Override
  //  protected void onDestroy() {
  //      super.onDestroy();
  //      if (audioRecord != null) {
  //          audioRecord.stop();
  //          audioRecord.release();
  //          audioThread.quitSafely();
  //      }
  //  }

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

    @Override
    protected void onPause() {
        super.onPause();

        // Zapisz aktualny stan przycisku ToggleButton i StopSwitch
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("toggleButtonState", startButton.isChecked());
        editor.putBoolean("StopSwitchState", StopSwitch);
        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        //Toast.makeText(this, "Wywołanie INSTANCE ON SAVE " + startButton.isChecked(), Toast.LENGTH_SHORT).show();
        // Zapisujemy stan przycisku ToggleButton do obiektu Bundle
        savedInstanceState.putBoolean("toggleButtonState", startButton.isChecked());
        savedInstanceState.putBoolean("StopSwitchState", StopSwitch);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Toast.makeText(this, "Wywołanie INSTANCE RESTORE " + startButton.isChecked(), Toast.LENGTH_SHORT).show();

        // Przywróć stan przycisku z Bundle i ustaw na widoku przycisku
        startButton.setChecked(savedInstanceState.getBoolean("toggleButtonState"));
        StopSwitch = savedInstanceState.getBoolean("StopSwitchState");
    }

}

