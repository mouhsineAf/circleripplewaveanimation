package com.devm22.ripplewavesview;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.devm22.circleripplewaveanimation.CircleRippleWaveView;

public class MainActivity extends AppCompatActivity {

    Button btnPlay, btnStop;
    CircleRippleWaveView waveView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnPlay = findViewById(R.id.btnPlay);
        btnStop = findViewById(R.id.btnStop);
        waveView = findViewById(R.id.waveView);



        waveView.setWaveCount(4);
        waveView.setMainWaveColor(Color.BLUE);
        waveView.setAnimationSpeed(400);
        waveView.setCenterText("PLAY");
        waveView.setTextAllCaps(true);
        waveView.setCenterTextColor(Color.WHITE);
        waveView.setCenterTextSize(48f);
        waveView.setCenterTextStyle(android.graphics.Typeface.BOLD);
        waveView.setCenterImageTint(Color.YELLOW);
        waveView.setCenterImagePadding(16f);

        btnPlay.setOnClickListener(view -> {
            waveView.startAnimation();
        });

        btnStop.setOnClickListener(view -> {
            waveView.stopAnimation();
        });


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        waveView.stopAnimation();
    }



}