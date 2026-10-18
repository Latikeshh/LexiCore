package com.dictionary.use.models;

/**
 * Represents current status and metadata of the local dictionary database.
 */
public final class DictionaryStatus {
    private final boolean installed;
    private final String version;
    private final long wordCount;
    private final long phraseCount;
    private final long lastUpdateTimeMs;
    private final String databasePath;

    public DictionaryStatus(boolean installed, String version, long wordCount, long phraseCount, long lastUpdateTimeMs, String databasePath) {
        this.installed = installed;
        this.version = version != null ? version : "0.0.0";
        this.wordCount = wordCount;
        this.phraseCount = phraseCount;
        this.lastUpdateTimeMs = lastUpdateTimeMs;
        this.databasePath = databasePath != null ? databasePath : "";
    }

    public boolean isInstalled() {
        return installed;
    }

    public String getVersion() {
        return version;
    }

    public long getWordCount() {
        return wordCount;
    }

    public long getPhraseCount() {
        return phraseCount;
    }

    public long getLastUpdateTimeMs() {
        return lastUpdateTimeMs;
    }

    public String getDatabasePath() {
        return databasePath;
    }

    @Override
    public String toString() {
        return "DictionaryStatus{" +
                "installed=" + installed +
                ", version='" + version + '\'' +
                ", wordCount=" + wordCount +
                ", phraseCount=" + phraseCount +
                ", databasePath='" + databasePath + '\'' +
                '}';
    }
}
