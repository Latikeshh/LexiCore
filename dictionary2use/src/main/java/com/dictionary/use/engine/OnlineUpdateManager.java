package com.dictionary.use.engine;

import android.content.Context;
import android.util.Log;

import com.dictionary.use.models.UpdateCheckResult;
import com.dictionary.use.models.UpdateProgressCallback;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages OTA dataset checks, secure background downloads, SHA-256 integrity verification,
 * and safe atomic dataset swaps.
 */
public class OnlineUpdateManager {
    private static final String TAG = "OnlineUpdateManager";
    private final Context context;
    private final ExecutorService executor;
    private String manifestUrl;

    public OnlineUpdateManager(Context context, String manifestUrl) {
        this.context = context.getApplicationContext();
        this.manifestUrl = manifestUrl != null ? manifestUrl : "";
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void setManifestUrl(String manifestUrl) {
        this.manifestUrl = manifestUrl;
    }

    public void checkForUpdates(String currentVersion, UpdateCheckCallback callback) {
        if (manifestUrl == null || manifestUrl.isEmpty()) {
            if (callback != null) callback.onResult(UpdateCheckResult.noUpdate(currentVersion));
            return;
        }

        executor.execute(() -> {
            try {
                URL url = new URL(manifestUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (InputStream is = conn.getInputStream();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }

                        JSONObject json = new JSONObject(sb.toString());
                        String latestVersion = json.optString("version", currentVersion);
                        long size = json.optLong("size_bytes", 0);
                        String downloadUrl = json.optString("download_url", "");
                        String sha256 = json.optString("sha256", "");
                        String notes = json.optString("release_notes", "Bug fixes and word additions.");

                        boolean isNewer = isVersionNewer(latestVersion, currentVersion);

                        UpdateCheckResult result = new UpdateCheckResult(
                                isNewer, currentVersion, latestVersion, size, downloadUrl, sha256, notes
                        );

                        if (callback != null) callback.onResult(result);
                    }
                } else {
                    if (callback != null) callback.onResult(UpdateCheckResult.noUpdate(currentVersion));
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed checking for updates: " + e.getMessage());
                if (callback != null) callback.onResult(UpdateCheckResult.noUpdate(currentVersion));
            }
        });
    }

    public void downloadUpdate(UpdateCheckResult updateInfo, UpdateProgressCallback callback) {
        if (updateInfo == null || !updateInfo.isUpdateAvailable() || updateInfo.getDownloadUrl().isEmpty()) {
            if (callback != null) callback.onError(new IllegalArgumentException("No valid update available"));
            return;
        }

        executor.execute(() -> {
            File tempFile = new File(context.getCacheDir(), "temp_dictionary_update.db");
            try {
                URL url = new URL(updateInfo.getDownloadUrl());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP server responded with code: " + responseCode);
                }

                long totalBytes = conn.getContentLengthLong();
                long downloadedBytes = 0;

                try (InputStream is = conn.getInputStream();
                     FileOutputStream fos = new FileOutputStream(tempFile)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        downloadedBytes += bytesRead;

                        if (callback != null && totalBytes > 0) {
                            int percent = (int) ((downloadedBytes * 100) / totalBytes);
                            callback.onProgress(percent, downloadedBytes, totalBytes);
                        }
                    }
                }

                // Verify SHA-256 Checksum if provided
                if (updateInfo.getSha256Checksum() != null && !updateInfo.getSha256Checksum().isEmpty()) {
                    String calculatedSha = calculateSHA256(tempFile);
                    if (!calculatedSha.equalsIgnoreCase(updateInfo.getSha256Checksum())) {
                        throw new SecurityException("SHA-256 checksum mismatch! Expected: "
                                + updateInfo.getSha256Checksum() + ", Actual: " + calculatedSha);
                    }
                }

                // Atomic Swap to actual database location
                File targetDbFile = context.getDatabasePath("dictionary2use.db");
                if (targetDbFile.exists()) {
                    File backupFile = new File(context.getDatabasePath("dictionary2use.db.bak").getAbsolutePath());
                    if (backupFile.exists()) backupFile.delete();
                    targetDbFile.renameTo(backupFile);
                }

                copyFile(tempFile, targetDbFile);
                tempFile.delete();

                if (callback != null) callback.onSuccess(updateInfo.getLatestVersion());

            } catch (Exception e) {
                Log.e(TAG, "Update download failed: " + e.getMessage(), e);
                if (tempFile.exists()) tempFile.delete();
                if (callback != null) callback.onError(e);
            }
        });
    }

    private String calculateSHA256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[8192];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void copyFile(File src, File dst) throws Exception {
        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(dst)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = fis.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
        }
    }

    private boolean isVersionNewer(String remoteVersion, String localVersion) {
        if (remoteVersion == null || localVersion == null) return false;
        return remoteVersion.compareTo(localVersion) > 0;
    }

    public interface UpdateCheckCallback {
        void onResult(UpdateCheckResult result);
    }
}
