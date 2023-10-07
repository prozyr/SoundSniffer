package com.example.soundsniffier;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LineChart chart = findViewById(R.id.chart);

        int[] dataObjectsX = {1, 2, 3, 4, 5, 6};
        int[] dataObjectsY = {11, 12, 13, 14, 15, 16};

        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < dataObjectsX.length; i++) {
            // Tworzymy obiekty Entry dla danych X i Y
            entries.add(new Entry(dataObjectsX[i], dataObjectsY[i]));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Label");
        // Możesz dalej dostosować wygląd dataSet, np. kolor, itp.

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // Odświeżenie wykresu
    }
}
