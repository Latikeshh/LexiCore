package com.dictionary.use.spellcheck.api;

/**
 * Default equality character comparator.
 */
public class DefaultCharComparator implements CharComparator {

    @Override
    public boolean areEqual(char char1, char char2) {
        return char1 == char2;
    }

    @Override
    public boolean areDistinct(char char1, char char2) {
        return char1 != char2;
    }
}
