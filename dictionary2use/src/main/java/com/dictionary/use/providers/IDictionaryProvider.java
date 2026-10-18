package com.dictionary.use.providers;

import com.dictionary.use.models.DictionaryResult;
import com.dictionary.use.models.DictionaryStatus;

import java.util.List;

/**
 * Interface defining the underlying data store contract for Dictionary2Use.
 */
public interface IDictionaryProvider {
    /**
     * Performs a direct word lookup in the provider.
     */
    DictionaryResult lookup(String word);

    /**
     * Performs a prefix search (autocompletion) for words starting with prefix.
     */
    List<DictionaryResult> searchPrefix(String prefix, int limit);

    /**
     * Returns next-word phrase predictions following previousWord.
     */
    List<String> getPredictions(String previousWord, int limit);

    /**
     * Learns or increments the frequency of a user-entered word.
     */
    void learnWord(String word, long frequency);

    /**
     * Checks if the provider data store is loaded and ready.
     */
    boolean isReady();

    /**
     * Returns operational status and metadata of the underlying store.
     */
    DictionaryStatus getStatus();

    /**
     * Closes connections and releases background resources.
     */
    void close();
}
