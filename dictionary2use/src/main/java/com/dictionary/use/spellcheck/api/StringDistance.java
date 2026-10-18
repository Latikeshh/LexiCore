package com.dictionary.use.spellcheck.api;

/**
 * Interface for string distance algorithms with early stop support.
 */
public interface StringDistance {
    int distanceWithEarlyStop(String string1, String string2, int maxDistance);
}
