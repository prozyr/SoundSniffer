package com.example.soundsniffier;
import com.example.soundsniffier.R.id;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineDataSet;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
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



import java.util.ArrayList;
import java.util.List;


public class Spectogram extends AppCompatActivity implements SoundDataObserver {
    private LineChart SpecChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectogram);


        SpecChart =  findViewById(R.id.Spectogram);
        // Odbierz dane z Intent
        boolean stopSwitchState = getIntent().getBooleanExtra("StopSwitchState", false);
        boolean toggleButtonState = getIntent().getBooleanExtra("ToggleButtonState", false);

        // Utwórz Intention i przekaż dane (jeśli potrzebne)
        Intent intent = new Intent(Spectogram.this, MainActivity.class);
        intent.putExtra("StopSwitchState", stopSwitchState);
        intent.putExtra("ToggleButtonState", toggleButtonState);

        Button chartButton = findViewById(R.id.ChartButton);

            LineDataSet dataSet2;
            dataSet2 = new LineDataSet(new ArrayList<>(MainActivity.entries2), "Data");
            LineData lineData2 = new LineData(dataSet2);

            SpecChart.setData(lineData2);
            SpecChart.notifyDataSetChanged();
            SpecChart.invalidate();

        Toast.makeText(this, "Ilość punktów w dataSet2: " + dataSet2.getEntryCount(), Toast.LENGTH_SHORT).show();

        chartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(intent);
                //finish();
            }
        });
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

                //if (!StopSwitch) {

                    // Update second chart data and refresh
                    SpecChart.setData(lineData2);
                    SpecChart.notifyDataSetChanged();
                    SpecChart.invalidate();

/*                    // Retrieve X and Y values from the second chart
                    float get_X_Value = chart2.getX();
                    float get_Y_Value = chart2.getY();

                    // Add the new entry to specData
                    specData.add(new Entry(get_Y_Value, get_X_Value));*/
                //}
            }
        });
    }
}
