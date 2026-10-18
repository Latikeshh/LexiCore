package com.dictionary.use.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the immutable result of a dictionary lookup operation.
 */
public final class DictionaryResult {
    private final String queryWord;
    private final boolean found;
    private final long frequency;
    private final String definition;
    private final List<String> synonyms;
    private final String language;
    private final String category;
    private final List<String> suggestions;
    private final List<String> nextWordPredictions;

    private DictionaryResult(Builder builder) {
        this.queryWord = builder.queryWord;
        this.found = builder.found;
        this.frequency = builder.frequency;
        this.definition = builder.definition;
        this.synonyms = builder.synonyms != null ? Collections.unmodifiableList(new ArrayList<>(builder.synonyms)) : Collections.emptyList();
        this.language = builder.language;
        this.category = builder.category;
        this.suggestions = builder.suggestions != null ? Collections.unmodifiableList(new ArrayList<>(builder.suggestions)) : Collections.emptyList();
        this.nextWordPredictions = builder.nextWordPredictions != null ? Collections.unmodifiableList(new ArrayList<>(builder.nextWordPredictions)) : Collections.emptyList();
    }

    public static DictionaryResult notFound(String queryWord, List<String> suggestions) {
        return new Builder(queryWord)
                .setFound(false)
                .setSuggestions(suggestions)
                .build();
    }

    public String getQueryWord() {
        return queryWord;
    }

    public boolean isFound() {
        return found;
    }

    public long getFrequency() {
        return frequency;
    }

    public String getDefinition() {
        return definition;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public String getLanguage() {
        return language;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public List<String> getNextWordPredictions() {
        return nextWordPredictions;
    }

    @Override
    public String toString() {
        return "DictionaryResult{" +
                "queryWord='" + queryWord + '\'' +
                ", found=" + found +
                ", frequency=" + frequency +
                ", definition='" + definition + '\'' +
                ", category='" + category + '\'' +
                ", suggestionsCount=" + suggestions.size() +
                '}';
    }

    public static class Builder {
        private final String queryWord;
        private boolean found = false;
        private long frequency = 0;
        private String definition = "";
        private List<String> synonyms = new ArrayList<>();
        private String language = "English";
        private String category = "General";
        private List<String> suggestions = new ArrayList<>();
        private List<String> nextWordPredictions = new ArrayList<>();

        public Builder(String queryWord) {
            this.queryWord = queryWord != null ? queryWord : "";
        }

        public Builder setFound(boolean found) {
            this.found = found;
            return this;
        }

        public Builder setFrequency(long frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder setDefinition(String definition) {
            this.definition = definition != null ? definition : "";
            return this;
        }

        public Builder setSynonyms(List<String> synonyms) {
            if (synonyms != null) this.synonyms = synonyms;
            return this;
        }

        public Builder setLanguage(String language) {
            if (language != null) this.language = language;
            return this;
        }

        public Builder setCategory(String category) {
            if (category != null) this.category = category;
            return this;
        }

        public Builder setSuggestions(List<String> suggestions) {
            if (suggestions != null) this.suggestions = suggestions;
            return this;
        }

        public Builder setNextWordPredictions(List<String> nextWordPredictions) {
            if (nextWordPredictions != null) this.nextWordPredictions = nextWordPredictions;
            return this;
        }

        public DictionaryResult build() {
            return new DictionaryResult(this);
        }
    }
}
