package com.dictionary.use.spellcheck;

import com.dictionary.use.spellcheck.api.Bigram;
import com.dictionary.use.spellcheck.api.DamerauLevenshteinOSA;
import com.dictionary.use.spellcheck.api.StringDistance;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder class for creating configured SymSpellImpl instances.
 */
public class SymSpellBuilder {
    private int maxDictionaryEditDistance = 2;
    private int prefixLength = 7;
    private StringDistance stringDistanceAlgorithm = new DamerauLevenshteinOSA();
    private Map<String, Long> unigramLexicon = new HashMap<>();
    private Map<Bigram, Long> bigramLexicon = new HashMap<>();

    public SymSpellBuilder setMaxDictionaryEditDistance(int maxDictionaryEditDistance) {
        this.maxDictionaryEditDistance = maxDictionaryEditDistance;
        return this;
    }

    public SymSpellBuilder setPrefixLength(int prefixLength) {
        this.prefixLength = prefixLength;
        return this;
    }

    public SymSpellBuilder setUnigramLexicon(Map<String, Long> unigramLexicon) {
        if (unigramLexicon != null) {
            this.unigramLexicon = unigramLexicon;
        }
        return this;
    }

    public SymSpellBuilder setBigramLexicon(Map<Bigram, Long> bigramLexicon) {
        if (bigramLexicon != null) {
            this.bigramLexicon = bigramLexicon;
        }
        return this;
    }

    public SymSpellBuilder setStringDistanceAlgorithm(StringDistance distanceAlgorithm) {
        if (distanceAlgorithm != null) {
            this.stringDistanceAlgorithm = distanceAlgorithm;
        }
        return this;
    }

    public Map<String, Long> getUnigramLexicon() {
        return unigramLexicon;
    }

    public Map<Bigram, Long> getBigramLexicon() {
        return bigramLexicon;
    }

    public int getMaxDictionaryEditDistance() {
        return maxDictionaryEditDistance;
    }

    public int getPrefixLength() {
        return prefixLength;
    }

    public StringDistance getStringDistanceAlgorithm() {
        return stringDistanceAlgorithm;
    }

    public SymSpellImpl createSymSpell() {
        return new SymSpellImpl(this);
    }
}
