package com.dictionary.use;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.dictionary.use.engine.OnlineUpdateManager;
import com.dictionary.use.engine.SQLiteDictionaryStore;
import com.dictionary.use.engine.StarterDatasetInstaller;
import com.dictionary.use.engine.DictionarySpellEngine;
import com.dictionary.use.models.DictionaryResult;
import com.dictionary.use.models.DictionaryStatus;
import com.dictionary.use.models.LookupCallback;
import com.dictionary.use.models.UpdateCheckResult;
import com.dictionary.use.models.UpdateProgressCallback;
import com.dictionary.use.providers.IDictionaryProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Primary public facade for the Dictionary2Use library.
 * Provides sync/async lookup, prefix autocompletion, phrase predictions, spelling correction,
 * local dynamic word learning, and secure OTA updates.
 */
public final class Dictionary2Use {
    private final Context context;
    private final IDictionaryProvider provider;
    private final DictionarySpellEngine spellEngine;
    private final OnlineUpdateManager updateManager;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final boolean autoLearningEnabled;

    private Dictionary2Use(Builder builder) {
        this.context = builder.context.getApplicationContext();
        this.autoLearningEnabled = builder.enableAutoLearning;
        this.executor = Executors.newFixedThreadPool(2);
        this.mainHandler = new Handler(Looper.getMainLooper());

        if (builder.customProvider != null) {
            this.provider = builder.customProvider;
        } else {
            SQLiteDictionaryStore store = new SQLiteDictionaryStore(this.context);
            StarterDatasetInstaller.ensureStarterDatasetInstalled(this.context, store);
            this.provider = store;
        }

        this.spellEngine = new DictionarySpellEngine(2);
        this.updateManager = new OnlineUpdateManager(this.context, builder.datasetUrl);

        // Warm up spell engine asynchronously with top starter words
        executor.execute(this::warmupSpellEngine);
    }

    private void warmupSpellEngine() {
        if (provider != null) {
            List<DictionaryResult> topWords = provider.searchPrefix("", 1000);
            for (DictionaryResult res : topWords) {
                spellEngine.addWord(res.getQueryWord(), res.getFrequency());
            }
        }
    }

    /**
     * Synchronous word lookup. Safe to call on background threads.
     */
    public DictionaryResult lookupSync(String word) {
        if (word == null || word.trim().isEmpty()) {
            return DictionaryResult.notFound(word, new ArrayList<>());
        }

        String cleaned = word.trim();
        DictionaryResult result = provider.lookup(cleaned);

        if (!result.isFound()) {
            // Find fuzzy spelling suggestions if exact word not found
            List<String> suggestions = spellEngine.lookupSuggestions(cleaned, 2, 5);
            return DictionaryResult.notFound(cleaned, suggestions);
        }

        if (autoLearningEnabled && result.isFound()) {
            provider.learnWord(cleaned, result.getFrequency() + 1);
        }

        return result;
    }

    /**
     * Asynchronous word lookup. Dispatches result back on main UI thread.
     */
    public void lookup(String word, LookupCallback callback) {
        executor.execute(() -> {
            DictionaryResult result = lookupSync(word);
            if (callback != null) {
                mainHandler.post(() -> callback.onResult(result));
            }
        });
    }

    /**
     * Synchronous prefix search / autocompletion.
     */
    public List<DictionaryResult> searchPrefixSync(String prefix, int limit) {
        return provider.searchPrefix(prefix, limit);
    }

    /**
     * Synchronous phrase / next-word prediction lookup.
     */
    public List<String> getNextWordPredictionsSync(String previousWord, int limit) {
        return provider.getPredictions(previousWord, limit);
    }

    /**
     * Synchronous spelling correction lookup.
     */
    public List<String> getSpellingSuggestionsSync(String queryWord, int maxEditDistance) {
        return spellEngine.lookupSuggestions(queryWord, maxEditDistance, 5);
    }

    /**
     * Dynamic user word learning.
     */
    public void learnWord(String word) {
        if (word == null || word.trim().isEmpty()) return;
        executor.execute(() -> {
            provider.learnWord(word.trim(), 1);
            spellEngine.addWord(word.trim(), 1);
        });
    }

    /**
     * Returns operational status and metadata of current dictionary store.
     */
    public DictionaryStatus getStatus() {
        return provider.getStatus();
    }

    /**
     * Checks online dataset endpoint for updates.
     */
    public void checkForUpdates(Callback<UpdateCheckResult> callback) {
        DictionaryStatus status = getStatus();
        updateManager.checkForUpdates(status.getVersion(), result -> {
            if (callback != null) {
                mainHandler.post(() -> callback.onResult(result));
            }
        });
    }

    /**
     * Downloads and installs an OTA dataset update.
     */
    public void downloadUpdate(UpdateCheckResult updateInfo, UpdateProgressCallback callback) {
        updateManager.downloadUpdate(updateInfo, new UpdateProgressCallback() {
            @Override
            public void onProgress(int percent, long bytesDownloaded, long totalBytes) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onProgress(percent, bytesDownloaded, totalBytes));
                }
            }

            @Override
            public void onSuccess(String installedVersion) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onSuccess(installedVersion));
                }
            }

            @Override
            public void onError(Exception exception) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onError(exception));
                }
            }
        });
    }

    /**
     * Closes underlying connections and thread pools.
     */
    public void close() {
        if (provider != null) provider.close();
        if (!executor.isShutdown()) executor.shutdown();
    }

    public interface Callback<T> {
        void onResult(T result);
    }

    /**
     * Builder for constructing Dictionary2Use instances.
     */
    public static class Builder {
        private final Context context;
        private String datasetUrl = "";
        private IDictionaryProvider customProvider = null;
        private boolean enableAutoLearning = true;

        public Builder(Context context) {
            if (context == null) throw new IllegalArgumentException("Context must not be null");
            this.context = context.getApplicationContext();
        }

        public Builder setDatasetUrl(String manifestUrl) {
            this.datasetUrl = manifestUrl;
            return this;
        }

        public Builder setCustomProvider(IDictionaryProvider customProvider) {
            this.customProvider = customProvider;
            return this;
        }

        public Builder setEnableAutoLearning(boolean enableAutoLearning) {
            this.enableAutoLearning = enableAutoLearning;
            return this;
        }

        public Dictionary2Use build() {
            return new Dictionary2Use(this);
        }
    }
}
