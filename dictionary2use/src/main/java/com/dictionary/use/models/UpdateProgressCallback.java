package com.dictionary.use.models;

/**
 * Callback for tracking background download and installation progress.
 */
public interface UpdateProgressCallback {
    void onProgress(int percent, long bytesDownloaded, long totalBytes);
    void onSuccess(String installedVersion);
    void onError(Exception exception);
}
