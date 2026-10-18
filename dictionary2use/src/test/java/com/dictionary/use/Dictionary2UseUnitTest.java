package com.dictionary.use;

import com.dictionary.use.engine.DictionarySpellEngine;
import com.dictionary.use.models.DictionaryResult;
import com.dictionary.use.models.DictionaryStatus;
import com.dictionary.use.models.UpdateCheckResult;
import com.dictionary.use.providers.IDictionaryProvider;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests verifying Dictionary2Use lookup, normalization, status, and spellcheck algorithms.
 */
public class Dictionary2UseUnitTest {

    private IDictionaryProvider mockProvider;

    @Before
    public void setUp() {
        mockProvider = new IDictionaryProvider() {
            private final Map<String, DictionaryResult> store = new HashMap<>();

            {
                store.put("computer", new DictionaryResult.Builder("computer")
                        .setFound(true)
                        .setFrequency(120)
                        .setDefinition("An electronic device")
                        .setCategory("Technology")
                        .build());

                store.put("android", new DictionaryResult.Builder("android")
                        .setFound(true)
                        .setFrequency(110)
                        .setDefinition("Mobile operating system")
                        .setCategory("Technology")
                        .build());
            }

            @Override
            public DictionaryResult lookup(String word) {
                if (word == null) return DictionaryResult.notFound("", new ArrayList<>());
                String cleaned = word.trim().toLowerCase();
                if (store.containsKey(cleaned)) {
                    return store.get(cleaned);
                }
                return DictionaryResult.notFound(word, new ArrayList<>());
            }

            @Override
            public List<DictionaryResult> searchPrefix(String prefix, int limit) {
                List<DictionaryResult> res = new ArrayList<>();
                for (String k : store.keySet()) {
                    if (k.startsWith(prefix.toLowerCase())) {
                        res.add(store.get(k));
                    }
                }
                return res;
            }

            @Override
            public List<String> getPredictions(String previousWord, int limit) {
                List<String> pred = new ArrayList<>();
                if ("android".equalsIgnoreCase(previousWord)) {
                    pred.add("developer");
                    pred.add("studio");
                }
                return pred;
            }

            @Override
            public void learnWord(String word, long frequency) {
                store.put(word.toLowerCase(), new DictionaryResult.Builder(word)
                        .setFound(true)
                        .setFrequency(frequency)
                        .setCategory("UserLearned")
                        .build());
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public DictionaryStatus getStatus() {
                return new DictionaryStatus(true, "2026.10.18", store.size(), 2, System.currentTimeMillis(), "/mock/db");
            }

            @Override
            public void close() {
            }
        };
    }

    @Test
    public void testKnownWordLookup() {
        DictionaryResult result = mockProvider.lookup("computer");
        assertTrue(result.isFound());
        assertEquals("computer", result.getQueryWord());
        assertEquals("An electronic device", result.getDefinition());
        assertEquals("Technology", result.getCategory());
    }

    @Test
    public void testUppercaseAndWhitespaceHandling() {
        DictionaryResult result = mockProvider.lookup("   COMPUTER  ");
        assertTrue(result.isFound());
        assertEquals("An electronic device", result.getDefinition());
    }

    @Test
    public void testUnknownWordReturnsNotFound() {
        DictionaryResult result = mockProvider.lookup("nonexistentword123");
        assertFalse(result.isFound());
    }

    @Test
    public void testSpellCheckerDamerauLevenshteinDistance() {
        int dist = DictionarySpellEngine.damerauLevenshteinDistance("compter", "computer");
        assertEquals(1, dist);

        int dist2 = DictionarySpellEngine.damerauLevenshteinDistance("teh", "the");
        assertEquals(1, dist2);
    }

    @Test
    public void testSpellCheckerSuggestions() {
        DictionarySpellEngine engine = new DictionarySpellEngine(2);
        engine.addWord("computer", 100);
        engine.addWord("commuter", 90);

        List<String> suggestions = engine.lookupSuggestions("compter", 2, 5);
        assertFalse(suggestions.isEmpty());
        assertTrue(suggestions.contains("computer"));
    }

    @Test
    public void testPhrasePredictions() {
        List<String> predictions = mockProvider.getPredictions("android", 2);
        assertNotNull(predictions);
        assertEquals(2, predictions.size());
        assertEquals("developer", predictions.get(0));
    }

    @Test
    public void testStatusAndVersion() {
        DictionaryStatus status = mockProvider.getStatus();
        assertTrue(status.isInstalled());
        assertEquals("2026.10.18", status.getVersion());
    }

    @Test
    public void testUpdateCheckNoUpdate() {
        UpdateCheckResult noUpdate = UpdateCheckResult.noUpdate("2026.10.18");
        assertFalse(noUpdate.isUpdateAvailable());
        assertEquals("2026.10.18", noUpdate.getCurrentVersion());
    }
}
