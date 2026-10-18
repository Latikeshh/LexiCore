package com.dictionary.use.spellcheck.api.exceptions;

/**
 * Exception thrown when lookup is requested before unigram lexicon is initialized.
 */
public class NotInitializedException extends SpellCheckerException {

    public NotInitializedException() {
        super();
    }

    public NotInitializedException(String message) {
        super(message);
    }
}
