package com.dictionary.use.spellcheck;

import com.dictionary.use.spellcheck.api.Bigram;
import com.dictionary.use.spellcheck.api.SuggestItem;
import com.dictionary.use.spellcheck.api.exceptions.NotInitializedException;

import java.util.List;
import java.util.Map;

/**
 * Public SymSpell interface for spelling suggestions and compound phrase lookups.
 */
public interface SymSpell {

    List<SuggestItem> lookup(String input, Verbosity verbosity, boolean includeUnknown) throws NotInitializedException;

    List<SuggestItem> lookup(String input, Verbosity verbosity, int maxEditDistance) throws NotInitializedException;

    List<SuggestItem> lookup(String input, Verbosity verbosity, int maxEditDistance, boolean includeUnknown) throws NotInitializedException;

    List<SuggestItem> lookup(String input, Verbosity verbosity) throws NotInitializedException;

    List<SuggestItem> lookupCompound(String input, int editDistanceMax, boolean includeUnknown) throws NotInitializedException;

    Map<String, Long> getUnigramLexicon();

    Map<Bigram, Long> getBigramLexicon();

    int getMaxDictionaryEditDistance();

    void addWord(String word, long frequency);
}
