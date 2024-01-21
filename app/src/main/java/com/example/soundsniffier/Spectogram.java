package com.example.soundsniffier;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
        chartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(intent);
                //finish();
            }
        });
    }
}
