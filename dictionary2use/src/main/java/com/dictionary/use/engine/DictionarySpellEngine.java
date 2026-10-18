package com.dictionary.use.engine;

import com.dictionary.use.spellcheck.SpellCorrectionEngine;
import com.dictionary.use.spellcheck.SpellCheckerBuilder;
import com.dictionary.use.spellcheck.Verbosity;
import com.dictionary.use.spellcheck.api.SuggestItem;
import com.dictionary.use.spellcheck.api.DamerauLevenshteinOSA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * High-level spellchecking engine adapter for Dictionary2Use lookup workflows.
 */
public class DictionarySpellEngine {

    private final SpellCorrectionEngine spellChecker;

    public DictionarySpellEngine(int maxDictionaryEditDistance) {
        SpellCheckerBuilder builder = new SpellCheckerBuilder()
                .setMaxDictionaryEditDistance(maxDictionaryEditDistance)
                .setPrefixLength(7);
        this.spellChecker = builder.createSpellChecker();
    }

    public synchronized void addWord(String word, long frequency) {
        if (word == null || word.trim().isEmpty()) return;
        spellChecker.addWord(word.trim(), frequency);
    }

    public synchronized List<String> lookupSuggestions(String input, int maxDistance, int maxResults) {
        if (input == null || input.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<SuggestItem> items = spellChecker.lookup(input, Verbosity.ALL, maxDistance, true);
            List<String> results = new ArrayList<>();
            for (SuggestItem item : items) {
                if (!results.contains(item.getSuggestion())) {
                    results.add(item.getSuggestion());
                }
                if (results.size() >= maxResults) break;
            }
            return results;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static int damerauLevenshteinDistance(String s1, String s2) {
        DamerauLevenshteinOSA osa = new DamerauLevenshteinOSA();
        return osa.distanceWithEarlyStop(s1, s2, 100);
    }

    public SpellCorrectionEngine getSpellChecker() {
        return spellChecker;
    }
}
