package com.dictionary.use.spellcheck;

/**
 * Controls the verbosity and candidate filtering for SymSpell lookups.
 */
public enum Verbosity {
    /**
     * Top suggestion with the highest frequency at the smallest edit distance.
     */
    TOP,
    /**
     * All suggestions at the smallest edit distance found.
     */
    CLOSEST,
    /**
     * All suggestions within maxDictionaryEditDistance.
     */
    ALL
}
