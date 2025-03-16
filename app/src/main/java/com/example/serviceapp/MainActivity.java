package com.example.serviceapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 100;

    private EditText pdfUrl1;
    private EditText pdfUrl2;
    private EditText pdfUrl3;
    private EditText pdfUrl4;
    private EditText pdfUrl5;
    private Button startDownloadBtn;

    // BroadcastReceiver to listen for download completion
    private BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "File has been downloaded.", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pdfUrl1 = findViewById(R.id.pdfUrl1);
        pdfUrl2 = findViewById(R.id.pdfUrl2);
        pdfUrl3 = findViewById(R.id.pdfUrl3);
        pdfUrl4 = findViewById(R.id.pdfUrl4);
        pdfUrl5 = findViewById(R.id.pdfUrl5);
        startDownloadBtn = findViewById(R.id.startDownloadBtn);

        // Register the broadcast receiver for download completion
        IntentFilter filter = new IntentFilter("com.example.serviceapp.DOWNLOAD_COMPLETE");
        registerReceiver(downloadCompleteReceiver, filter);

        startDownloadBtn.setOnClickListener(view -> {
            // For API < 29, request WRITE_EXTERNAL_STORAGE permission if needed
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                checkStoragePermission();
            } else {
                // On API 29+ saving to getExternalFilesDir() does not require the permission
                startDownloadService();
            }
        });
    }

    private void checkStoragePermission() {
        int permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        );

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE
            );
        } else {
            startDownloadService();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownloadService();
            } else {
                Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startDownloadService() {
        ArrayList<String> urls = new ArrayList<>();
        if (!pdfUrl1.getText().toString().trim().isEmpty()) {
            urls.add(pdfUrl1.getText().toString().trim());
        }
        if (!pdfUrl2.getText().toString().trim().isEmpty()) {
            urls.add(pdfUrl2.getText().toString().trim());
        }
        if (!pdfUrl3.getText().toString().trim().isEmpty()) {
            urls.add(pdfUrl3.getText().toString().trim());
        }
        if (!pdfUrl4.getText().toString().trim().isEmpty()) {
            urls.add(pdfUrl4.getText().toString().trim());
        }
        if (!pdfUrl5.getText().toString().trim().isEmpty()) {
            urls.add(pdfUrl5.getText().toString().trim());
        }

        if (!urls.isEmpty()) {
            Intent serviceIntent = new Intent(this, DownloadService.class);
            serviceIntent.putStringArrayListExtra("pdfUrls", urls);
            startService(serviceIntent);
        } else {
            Toast.makeText(this, "No URLs provided.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the receiver to avoid memory leaks
        unregisterReceiver(downloadCompleteReceiver);
    }
}
