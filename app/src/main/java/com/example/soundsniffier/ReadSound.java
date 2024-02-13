package com.example.soundsniffier;

import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;

import androidx.core.app.ActivityCompat;
// icon play button By Freepik
// icon pause button By inkubators
//package com.example.soundsniffier;

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
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import org.jtransforms.fft.DoubleFFT_1D;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.util.List;

import org.jtransforms.fft.DoubleFFT_1D;

public class ReadSound {
    ////// Maciejowy raj
    int ileWEntry2 = 5;
    int ileWHistogram = 1;

    int ileWFFT = 2048;
    byte[] buffer;
    int bytesRead, audioSample;
    short[] datatoHist;
    double[] a, magnitude, frequency;
    int imax;

    DoubleFFT_1D fft;
    double maxMag, re, im, mag, freq;
    int maxInd;
    /////
    private static final int PERMISSION_RECORD_AUDIO = 1;
    private AudioRecord audioRecord;
    private HandlerThread audioThread;
    private Handler audioHandler;
    private Context context;
    private int xValue = 0;
    private double[] window;
    public List<Entry> entries = new ArrayList<>();
    public List<Entry> entries2 = new ArrayList<>();
    int sampleRate = 44100/2;  // TODO: 44100
    int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private List<SoundDataObserver> observers = new ArrayList<>();

    private Object lock = new Object();

    public ReadSound(Context context) {
        this.context = context;
    }

    public void addObserver(SoundDataObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(SoundDataObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(List<Entry> entries, List<Entry> entries2) {
        for (SoundDataObserver observer : observers) {
            observer.onDataReceived(entries, entries2);
        }
    }

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

    public void startRecording() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((MainActivity) context, new String[]{android.Manifest.permission.RECORD_AUDIO}, PERMISSION_RECORD_AUDIO);
            //Toast.makeText(MainActivity.this, "Switch jest wyłączony", Toast.LENGTH_LONG).show();
        } else {
            initializeAudioRecording(sampleRate, channelConfig, audioFormat, bufferSize);
            //Toast.makeText(MainActivity.this, "Switch jest wyłączony", Toast.LENGTH_LONG).show();
        }
    }

    private void initializeAudioRecording(int sampleRate, int channelConfig, int audioFormat, int bufferSize) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        {
            /// Maciejowa delkaracja
            datatoHist = new short[bufferSize*ileWHistogram];
            fft = new DoubleFFT_1D(ileWFFT);
            a = new double[ileWFFT];
            buffer = new byte[bufferSize];
            for(int i=0; i < bufferSize*ileWEntry2; i++) {
                entries2.add(new Entry(0, 0));
            }
            frequency = new double[a.length];
            magnitude = new double[a.length];
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

                while (!Thread.currentThread().isInterrupted()) {
                    bytesRead = audioRecord.read(buffer, 0, bufferSize);

                    synchronized (lock) {

                        for (int i = 0; i < bytesRead / 2; i++) {
                            audioSample = (int) ((buffer[i * 2] & 0xFF) | (buffer[i * 2 + 1] << 8));
                            entries2.add(new Entry(xValue++, (int)audioSample));
                        }
                        imax = entries2.size();

                        {
                            int i = 0, j = 0;
                            for (Entry entries : entries2) {
                                i++;
                                if (i > imax-bufferSize*ileWHistogram) {
                                    datatoHist[j++] = (short)entries.getY();
                                }
                            }
                        }

                        // START TODO: Workspace for histogram
                        for (int i = 0; i < a.length; i++) {
                            a[i]= 0;
                        }
                        System.arraycopy(applyWindow(datatoHist), 0, a, 0, datatoHist.length);
                        fft.realForward(a);

                        maxMag = Double.NEGATIVE_INFINITY;
                        maxInd = -1;

                        for(int i = 0; i < a.length / 2; ++i) {
                            re  = a[2*i];
                            im  = a[2*i+1];
                            mag = Math.sqrt(re * re + im * im);
                            freq = (double)sampleRate * i / (a.length);

                            if(mag > maxMag) {
                                maxMag = mag;
                                maxInd = i;
                            }

                            frequency[i] = freq;
                            magnitude[i] = mag;
                        }
                        for (int i = 0; i < magnitude.length; i++) {
                            if (magnitude[i] != 0) {
                                magnitude[i] = 10 * Math.log10(magnitude[i]+1);
                            } else {
                                magnitude[i] = 0;
                            }
                        }
                        entries.clear();
                        for(int i = 0; i < a.length/2; ++i) {
                                entries.add(new Entry((float)frequency[i],(float)magnitude[i]));
                        }

                        // END TODO: Workspace for histogram

                        if (entries2.size() > bufferSize*20) {
                            for (int i = 0; i < bytesRead/2+1; i++) {
                                entries2.remove(0);
                            }
                        }

                        // Notify observers with the updated data
                        notifyObservers(new ArrayList<>(entries), new ArrayList<>(entries2));
                    }
                }
            }
        });
    }
}