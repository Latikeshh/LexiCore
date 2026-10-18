package com.dictionary.use.spellcheck;

import com.dictionary.use.spellcheck.api.Bigram;
import com.dictionary.use.spellcheck.api.StringDistance;
import com.dictionary.use.spellcheck.api.SuggestItem;
import com.dictionary.use.spellcheck.api.exceptions.NotInitializedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Symmetric Delete Spelling Correction algorithm implementation.
 */
public class SymSpellImpl implements SymSpell {

    private final int maxDictionaryEditDistance;
    private final int prefixLength;
    private final Map<String, Collection<String>> deletes = new ConcurrentHashMap<>();
    private final Map<Bigram, Long> bigramLexicon;
    private final Map<String, Long> unigramLexicon;
    private final StringDistance stringDistance;
    private int maxDictionaryWordLength;
    private long n;

    public SymSpellImpl(SymSpellBuilder builder) {
        this.unigramLexicon = new ConcurrentHashMap<>(builder.getUnigramLexicon());
        this.maxDictionaryEditDistance = builder.getMaxDictionaryEditDistance();
        this.prefixLength = builder.getPrefixLength();
        this.bigramLexicon = new ConcurrentHashMap<>(builder.getBigramLexicon());
        this.stringDistance = builder.getStringDistanceAlgorithm();

        long sum = 0;
        for (Long val : unigramLexicon.values()) {
            if (val != null) sum += val;
        }
        this.n = sum;

        for (String word : unigramLexicon.keySet()) {
            addDeletes(word);
        }

        int maxLen = 0;
        for (String word : unigramLexicon.keySet()) {
            if (word.length() > maxLen) {
                maxLen = word.length();
            }
        }
        this.maxDictionaryWordLength = maxLen;
    }

    private void addDeletes(String word) {
        Map<String, Collection<String>> edits = generateEdits(word);
        for (Map.Entry<String, Collection<String>> entry : edits.entrySet()) {
            Collection<String> set = deletes.computeIfAbsent(entry.getKey(), k -> ConcurrentHashMap.newKeySet());
            set.addAll(entry.getValue());
        }
    }

    @Override
    public synchronized void addWord(String word, long frequency) {
        if (word == null || word.trim().isEmpty()) return;
        String cleaned = word.trim().toLowerCase(Locale.ROOT);
        Long existingFreq = unigramLexicon.get(cleaned);
        if (existingFreq != null) {
            unigramLexicon.put(cleaned, existingFreq + frequency);
            n += frequency;
        } else {
            unigramLexicon.put(cleaned, frequency);
            n += frequency;
            if (cleaned.length() > maxDictionaryWordLength) {
                maxDictionaryWordLength = cleaned.length();
            }
            addDeletes(cleaned);
        }
    }

    private Map<String, Collection<String>> generateEdits(String key) {
        Set<String> edits = editsPrefix(key);
        Map<String, Collection<String>> generatedDeletes = new HashMap<>();
        for (String del : edits) {
            generatedDeletes.computeIfAbsent(del, k -> new ArrayList<>()).add(key);
        }
        return generatedDeletes;
    }

    private Set<String> editsPrefix(String key) {
        Set<String> set = new HashSet<>();
        if (key.length() <= maxDictionaryEditDistance) {
            set.add("");
        }
        if (key.length() > prefixLength) {
            key = key.substring(0, prefixLength);
        }
        set.add(key);
        return edits(key, 0, set);
    }

    private Set<String> edits(String word, int editDistance, Set<String> deleteWords) {
        editDistance++;
        if (word.length() > 1 && editDistance <= maxDictionaryEditDistance) {
            for (int i = 0; i < word.length(); i++) {
                StringBuilder sb = new StringBuilder(word);
                String delete = sb.deleteCharAt(i).toString();
                if (deleteWords.add(delete) && editDistance < maxDictionaryEditDistance) {
                    edits(delete, editDistance, deleteWords);
                }
            }
        }
        return deleteWords;
    }

    @Override
    public List<SuggestItem> lookup(String input, Verbosity verbosity, boolean includeUnknown) throws NotInitializedException {
        return lookup(input, verbosity, this.maxDictionaryEditDistance, includeUnknown);
    }

    @Override
    public List<SuggestItem> lookup(String input, Verbosity verbosity) throws NotInitializedException {
        return lookup(input, verbosity, false);
    }

    @Override
    public List<SuggestItem> lookup(String input, Verbosity verbosity, int maxEditDistance) throws NotInitializedException {
        return lookup(input, verbosity, maxEditDistance, false);
    }

    @Override
    public List<SuggestItem> lookup(String input, Verbosity verbosity, int maxEditDistance, boolean includeUnknown) throws NotInitializedException {
        if (maxEditDistance > maxDictionaryEditDistance) {
            throw new IllegalArgumentException("maxEditDistance > maxDictionaryEditDistance");
        }

        if (unigramLexicon.isEmpty()) {
            throw new NotInitializedException("There are no words in the unigram lexicon.");
        }

        if (input == null) input = "";
        String cleaned = input.trim().toLowerCase(Locale.ROOT);

        List<SuggestItem> suggestions = new ArrayList<>();
        int inputLen = cleaned.length();

        if (inputLen - maxEditDistance > maxDictionaryWordLength) {
            return suggestions;
        }

        Long count = unigramLexicon.get(cleaned);
        if (count != null) {
            suggestions.add(new SuggestItem(cleaned, 0, count));
            if (verbosity != Verbosity.ALL) {
                return suggestions;
            }
        }

        if (maxEditDistance == 0) {
            return suggestions;
        }

        Set<String> candidateSet = new HashSet<>();
        Set<String> deleteSet = new HashSet<>();

        if (inputLen > prefixLength) {
            deleteSet.add(cleaned.substring(0, prefixLength));
        } else {
            deleteSet.add(cleaned);
        }

        edits(cleaned, 0, deleteSet);

        for (String del : deleteSet) {
            Collection<String> suggestionCandidates = deletes.get(del);
            if (suggestionCandidates != null) {
                candidateSet.addAll(suggestionCandidates);
            }
        }

        for (String cand : candidateSet) {
            int dist = stringDistance.distanceWithEarlyStop(cleaned, cand, maxEditDistance);
            if (dist >= 0 && dist <= maxEditDistance) {
                long freq = unigramLexicon.getOrDefault(cand, 1L);
                suggestions.add(new SuggestItem(cand, dist, freq));
            }
        }

        Collections.sort(suggestions);

        if (verbosity == Verbosity.TOP && !suggestions.isEmpty()) {
            List<SuggestItem> top = new ArrayList<>();
            top.add(suggestions.get(0));
            return top;
        }

        return suggestions;
    }

    @Override
    public List<SuggestItem> lookupCompound(String input, int editDistanceMax, boolean includeUnknown) throws NotInitializedException {
        if (input == null || input.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String[] words = input.trim().split("\\s+");
        List<SuggestItem> compoundSuggestions = new ArrayList<>();

        for (String w : words) {
            List<SuggestItem> suggestions = lookup(w, Verbosity.TOP, editDistanceMax, includeUnknown);
            if (!suggestions.isEmpty()) {
                compoundSuggestions.add(suggestions.get(0));
            } else {
                compoundSuggestions.add(new SuggestItem(w, 0, 1.0));
            }
        }

        return compoundSuggestions;
    }

    @Override
    public Map<String, Long> getUnigramLexicon() {
        return Collections.unmodifiableMap(unigramLexicon);
    }

    @Override
    public Map<Bigram, Long> getBigramLexicon() {
        return Collections.unmodifiableMap(bigramLexicon);
    }

    @Override
    public int getMaxDictionaryEditDistance() {
        return maxDictionaryEditDistance;
    }
}
