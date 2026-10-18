package com.dictionary.use.spellcheck.api;

import java.util.Objects;

/**
 * Immutable class representing a spelling correction suggestion item.
 */
public final class SuggestItem implements Comparable<SuggestItem> {
    private final String suggestion;
    private final int editDistance;
    private final double frequencyOfSuggestionInDict;

    public SuggestItem(String suggestion, int editDistance, double frequencyOfSuggestionInDict) {
        this.suggestion = suggestion != null ? suggestion : "";
        this.editDistance = editDistance;
        this.frequencyOfSuggestionInDict = frequencyOfSuggestionInDict;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public int getEditDistance() {
        return editDistance;
    }

    public double getFrequencyOfSuggestionInDict() {
        return frequencyOfSuggestionInDict;
    }

    @Override
    public int compareTo(SuggestItem other) {
        if (this.editDistance == other.editDistance) {
            return Double.compare(other.frequencyOfSuggestionInDict, this.frequencyOfSuggestionInDict);
        } else {
            return Integer.compare(this.editDistance, other.editDistance);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuggestItem that = (SuggestItem) o;
        return editDistance == that.editDistance &&
                Double.compare(that.frequencyOfSuggestionInDict, frequencyOfSuggestionInDict) == 0 &&
                Objects.equals(suggestion, that.suggestion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suggestion, editDistance, frequencyOfSuggestionInDict);
    }

    @Override
    public String toString() {
        return "SuggestItem{" +
                "suggestion='" + suggestion + '\'' +
                ", editDistance=" + editDistance +
                ", frequency=" + frequencyOfSuggestionInDict +
                '}';
    }
}
