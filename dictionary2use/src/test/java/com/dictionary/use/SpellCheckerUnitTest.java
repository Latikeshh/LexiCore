package com.dictionary.use;

import com.dictionary.use.spellcheck.SpellCorrectionEngine;
import com.dictionary.use.spellcheck.SpellCheckerBuilder;
import com.dictionary.use.spellcheck.Verbosity;
import com.dictionary.use.spellcheck.api.DamerauLevenshteinOSA;
import com.dictionary.use.spellcheck.api.SuggestItem;
import com.dictionary.use.spellcheck.api.exceptions.NotInitializedException;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for the extracted spellchecking implementation.
 */
public class SpellCheckerUnitTest {

    private SpellCorrectionEngine spellChecker;

    @Before
    public void setUp() {
        Map<String, Long> lexicon = new HashMap<>();
        lexicon.put("computer", 100L);
        lexicon.put("commuter", 80L);
        lexicon.put("keyboard", 90L);
        lexicon.put("android", 95L);
        lexicon.put("the", 500L);

        SpellCheckerBuilder builder = new SpellCheckerBuilder()
                .setMaxDictionaryEditDistance(2)
                .setPrefixLength(7)
                .setUnigramLexicon(lexicon);

        spellChecker = builder.createSpellChecker();
    }

    @Test
    public void testExactLookup() throws Exception {
        List<SuggestItem> items = spellChecker.lookup("computer", Verbosity.TOP);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals("computer", items.get(0).getSuggestion());
        assertEquals(0, items.get(0).getEditDistance());
    }

    @Test
    public void testTypoCorrection() throws Exception {
        List<SuggestItem> items = spellChecker.lookup("compter", Verbosity.CLOSEST);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals("computer", items.get(0).getSuggestion());
        assertEquals(1, items.get(0).getEditDistance());
    }

    @Test
    public void testTranspositionCorrection() throws Exception {
        List<SuggestItem> items = spellChecker.lookup("teh", Verbosity.TOP);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals("the", items.get(0).getSuggestion());
        assertEquals(1, items.get(0).getEditDistance());
    }

    @Test
    public void testDamerauLevenshteinOSA() {
        DamerauLevenshteinOSA osa = new DamerauLevenshteinOSA();
        int dist1 = osa.distanceWithEarlyStop("compter", "computer", 2);
        assertEquals(1, dist1);

        int dist2 = osa.distanceWithEarlyStop("teh", "the", 2);
        assertEquals(1, dist2);

        int dist3 = osa.distanceWithEarlyStop("abcdef", "xyz", 2);
        assertEquals(-1, dist3);
    }

    @Test
    public void testDynamicWordAddition() throws Exception {
        spellChecker.addWord("newcustomword", 50L);
        List<SuggestItem> items = spellChecker.lookup("newcustomword", Verbosity.TOP);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals("newcustomword", items.get(0).getSuggestion());
    }

    @Test
    public void testLookupCompound() throws Exception {
        List<SuggestItem> compound = spellChecker.lookupCompound("compter teh", 2, true);
        assertNotNull(compound);
        assertEquals(2, compound.size());
        assertEquals("computer", compound.get(0).getSuggestion());
        assertEquals("the", compound.get(1).getSuggestion());
    }

    @Test(expected = NotInitializedException.class)
    public void testUninitializedThrowsException() throws Exception {
        SpellCorrectionEngine emptyChecker = new SpellCheckerBuilder().createSpellChecker();
        emptyChecker.lookup("test", Verbosity.TOP);
    }
}
