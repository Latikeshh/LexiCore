package com.dictionary.use.spellcheck.api;

/**
 * Interface for comparing characters during distance calculation.
 */
public interface CharComparator {
    boolean areEqual(char char1, char char2);
    boolean areDistinct(char char1, char char2);
}
