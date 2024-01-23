package com.example.soundsniffier;
import com.github.mikephil.charting.data.LineDataSet;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class Spectogram extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectogram);

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

        Toast.makeText(this, "Ilość punktów w dataSet2: " + dataSet2.getEntryCount(), Toast.LENGTH_SHORT).show();

        chartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(intent);
                //finish();
            }
        });
    }
}
