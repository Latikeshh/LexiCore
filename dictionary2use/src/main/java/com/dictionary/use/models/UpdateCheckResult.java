package com.dictionary.use.models;

/**
 * Result of an online dataset update check.
 */
public final class UpdateCheckResult {
    private final boolean updateAvailable;
    private final String currentVersion;
    private final String latestVersion;
    private final long downloadSizeBytes;
    private final String downloadUrl;
    private final String sha256Checksum;
    private final String releaseNotes;

    public UpdateCheckResult(boolean updateAvailable, String currentVersion, String latestVersion, long downloadSizeBytes, String downloadUrl, String sha256Checksum, String releaseNotes) {
        this.updateAvailable = updateAvailable;
        this.currentVersion = currentVersion != null ? currentVersion : "";
        this.latestVersion = latestVersion != null ? latestVersion : "";
        this.downloadSizeBytes = downloadSizeBytes;
        this.downloadUrl = downloadUrl != null ? downloadUrl : "";
        this.sha256Checksum = sha256Checksum != null ? sha256Checksum : "";
        this.releaseNotes = releaseNotes != null ? releaseNotes : "";
    }

    public static UpdateCheckResult noUpdate(String currentVersion) {
        return new UpdateCheckResult(false, currentVersion, currentVersion, 0, "", "", "Dataset is up to date.");
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public long getDownloadSizeBytes() {
        return downloadSizeBytes;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getSha256Checksum() {
        return sha256Checksum;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }
}
