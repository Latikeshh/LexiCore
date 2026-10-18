package com.dictionary.use.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Symmetric Delete Spelling Correction algorithm for generating fuzzy search suggestions.
 */
public class SymSpellEngine {
    private final int maxDictionaryEditDistance;
    private final int prefixLength;
    private final Map<String, Long> words = new HashMap<>();
    private final Map<String, List<String>> deletes = new HashMap<>();

    public SymSpellEngine(int maxDictionaryEditDistance) {
        this.maxDictionaryEditDistance = maxDictionaryEditDistance;
        this.prefixLength = 7;
    }

    public synchronized void addWord(String word, long frequency) {
        if (word == null || word.trim().isEmpty()) return;
        String cleaned = word.trim().toLowerCase(Locale.ROOT);
        words.put(cleaned, frequency);

        Set<String> deleteKeys = getDeletes(cleaned, maxDictionaryEditDistance);
        for (String del : deleteKeys) {
            deletes.computeIfAbsent(del, k -> new ArrayList<>()).add(cleaned);
        }
    }

    public synchronized List<String> lookupSuggestions(String input, int maxDistance, int maxResults) {
        if (input == null || input.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String cleaned = input.trim().toLowerCase(Locale.ROOT);
        if (words.containsKey(cleaned)) {
            // Exact match exists
            List<String> exact = new ArrayList<>();
            exact.add(cleaned);
            return exact;
        }

        Set<String> candidates = new HashSet<>();
        Set<String> inputDeletes = getDeletes(cleaned, maxDistance);

        for (String del : inputDeletes) {
            List<String> matchedWords = deletes.get(del);
            if (matchedWords != null) {
                for (String w : matchedWords) {
                    if (damerauLevenshteinDistance(cleaned, w) <= maxDistance) {
                        candidates.add(w);
                    }
                }
            }
        }

        List<String> sortedCandidates = new ArrayList<>(candidates);
        sortedCandidates.sort((a, b) -> {
            int distA = damerauLevenshteinDistance(cleaned, a);
            int distB = damerauLevenshteinDistance(cleaned, b);
            if (distA != distB) return Integer.compare(distA, distB);
            long freqA = words.getOrDefault(a, 0L);
            long freqB = words.getOrDefault(b, 0L);
            return Long.compare(freqB, freqA);
        });

        if (sortedCandidates.size() > maxResults) {
            return new ArrayList<>(sortedCandidates.subList(0, maxResults));
        }

        return sortedCandidates;
    }

    private Set<String> getDeletes(String key, int maxEditDistance) {
        Set<String> deletesSet = new HashSet<>();
        deletesSet.add(key);
        generateDeletes(key, 0, maxEditDistance, deletesSet);
        return deletesSet;
    }

    private void generateDeletes(String key, int currentDistance, int maxEditDistance, Set<String> deletesSet) {
        if (currentDistance >= maxEditDistance) return;

        for (int i = 0; i < key.length(); i++) {
            String del = key.substring(0, i) + key.substring(i + 1);
            if (deletesSet.add(del)) {
                generateDeletes(del, currentDistance + 1, maxEditDistance, deletesSet);
            }
        }
    }

    public static int damerauLevenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] d = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) d[i][0] = i;
        for (int j = 0; j <= len2; j++) d[0][j] = j;

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                d[i][j] = Math.min(
                        Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1),
                        d[i - 1][j - 1] + cost
                );
                if (i > 1 && j > 1 && s1.charAt(i - 1) == s2.charAt(j - 2) && s1.charAt(i - 2) == s2.charAt(j - 1)) {
                    d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + cost);
                }
            }
        }
        return d[len1][len2];
    }
}
