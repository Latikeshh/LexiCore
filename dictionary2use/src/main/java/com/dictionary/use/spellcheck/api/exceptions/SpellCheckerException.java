package com.dictionary.use.spellcheck.api.exceptions;

/**
 * Base exception class for spellchecking errors.
 */
public class SpellCheckerException extends Exception {

    public SpellCheckerException() {
        super();
    }

    public SpellCheckerException(String message) {
        super(message);
    }

    public SpellCheckerException(String message, Throwable cause) {
        super(message, cause);
    }
}
