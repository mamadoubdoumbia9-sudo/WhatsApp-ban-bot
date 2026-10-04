package com.example.whatsappban;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText etTargetNumber;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etTargetNumber = findViewById(R.id.etTargetNumber);
        tvStatus = findViewById(R.id.tvStatus);
        Button btnBan = findViewById(R.id.btnBan);
        Button btnCheck = findViewById(R.id.btnCheck);

        // Vérifier si le service d'accessibilité est activé
        if (!AccessibilityService.isServiceEnabled(this)) {
            tvStatus.setText("Activez le service dans Paramètres > Accessibilité !");
        }

        btnBan.setOnClickListener(v -> {
            String number = etTargetNumber.getText().toString();
            if (number.isEmpty()) {
                Toast.makeText(this, "Entrez un numéro", Toast.LENGTH_SHORT).show();
                return;
            }
            // Lancer le processus de ban
            AccessibilityService.startBanProcess(this, number);
        });

        btnCheck.setOnClickListener(v -> {
            String number = etTargetNumber.getText().toString();
            if (number.isEmpty()) {
                Toast.makeText(this, "Entrez un numéro", Toast.LENGTH_SHORT).show();
                return;
            }
            // Lancer le processus de check
            AccessibilityService.checkBanStatus(this, number);
        });
    }
  }
