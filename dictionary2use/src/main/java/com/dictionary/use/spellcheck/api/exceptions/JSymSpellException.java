package com.dictionary.use.spellcheck.api.exceptions;

/**
 * Base exception class for SymSpell spellchecking errors.
 */
public class JSymSpellException extends Exception {

    public JSymSpellException() {
        super();
    }

    public JSymSpellException(String message) {
        super(message);
    }

    public JSymSpellException(String message, Throwable cause) {
        super(message, cause);
    }
}
