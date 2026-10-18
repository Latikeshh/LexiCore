package com.dictionary.use.models;

/**
 * Asynchronous callback interface for dictionary lookup operations.
 */
public interface LookupCallback {
    void onResult(DictionaryResult result);
}
