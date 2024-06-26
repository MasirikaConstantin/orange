package com.example.test_orange_summer_challenge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private ProgressBar Pro;
    private TextView textViewProgress;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Pro = findViewById(R.id.progress);
        textViewProgress = findViewById(R.id.textViewProgress);

        // Temps total pour l'attente
        final int totalTime = 4000; // en millisecondes

        // Définir la visibilité de la ProgressBar
        Pro.setVisibility(View.VISIBLE);

        // Démarrer un thread pour mettre à jour la ProgressBar
        new Thread(new Runnable() {
            public void run() {
                long startTime = System.currentTimeMillis();

                while (progressStatus < 100) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    progressStatus = (int) (100.0 * elapsedTime / totalTime);

                    // Mettre à jour la barre de progression sur l'UI thread
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Pro.setProgress(progressStatus);
                            textViewProgress.setText(progressStatus + "%"); // Mettre à jour le TextView avec le pourcentage
                        }
                    });

                    try {
                        // Attendre pour la mise à jour suivante
                        Thread.sleep(100); // Ajustez le délai selon vos besoins
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Une fois terminé, démarrer l'activité suivante
                runOnUiThread(new Runnable() {
                    public void run() {
                        Intent i = new Intent(getApplicationContext(), Buetooth.class);
                        startActivity(i);
                        finish();
                    }
                });
            }
        }).start();

    }
}
