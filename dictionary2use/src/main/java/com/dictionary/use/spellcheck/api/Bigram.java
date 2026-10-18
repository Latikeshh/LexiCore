package com.dictionary.use.spellcheck.api;

import java.util.Objects;

/**
 * Immutable class representing a bigram word pair.
 */
public final class Bigram {
    private final String word1;
    private final String word2;

    public Bigram(String word1, String word2) {
        this.word1 = word1 != null ? word1 : "";
        this.word2 = word2 != null ? word2 : "";
    }

    public String getWord1() {
        return word1;
    }

    public String getWord2() {
        return word2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bigram)) return false;
        Bigram bigram = (Bigram) o;
        return Objects.equals(word1, bigram.word1) &&
                Objects.equals(word2, bigram.word2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word1, word2);
    }

    @Override
    public String toString() {
        return word1 + ' ' + word2;
    }
}
