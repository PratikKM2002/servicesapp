package com.example.serviceapp;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class DownloadService extends Service {

    private static final String TAG = "DownloadService";

    public DownloadService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        List<String> urls = intent.getStringArrayListExtra("pdfUrls");
        if (urls != null && !urls.isEmpty()) {
            new Thread(() -> {
                downloadPdfs(urls);
                // Send broadcast after downloads complete
                Intent broadcastIntent = new Intent("com.example.serviceapp.DOWNLOAD_COMPLETE");
                sendBroadcast(broadcastIntent);
                stopSelf();
            }).start();
        } else {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void downloadPdfs(List<String> urls) {
        for (String urlStr : urls) {
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                Log.d(TAG, "Downloading: " + urlStr);
                URL url = new URL(urlStr);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(true);
                connection.connect();

                int responseCode = connection.getResponseCode();
                // Check if the server sends a redirect response (301/302)
                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                        responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    String newUrlStr = connection.getHeaderField("Location");
                    Log.d(TAG, "Redirected to: " + newUrlStr);
                    connection.disconnect();
                    url = new URL(newUrlStr);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    responseCode = connection.getResponseCode();
                }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Determine a filename (based on the original URL)
                    String fileName = urlStr.substring(urlStr.lastIndexOf('/') + 1);
                    if (fileName.isEmpty()) {
                        fileName = "downloaded_file.pdf";
                    }

                    // Save to the app's Documents directory
                    File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                    if (dir != null && !dir.exists()) {
                        dir.mkdirs();
                    }
                    File outputFile = new File(dir, fileName);

                    inputStream = new BufferedInputStream(connection.getInputStream());
                    outputStream = new FileOutputStream(outputFile);

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();

                    Log.d(TAG, "Downloaded and saved to: " + outputFile.getAbsolutePath());
                } else {
                    Log.e(TAG, "Server returned HTTP " + responseCode + " for " + urlStr);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error downloading file: " + urlStr, e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This service is not designed for binding
        return null;
    }
}
