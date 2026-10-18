# Dictionary2Use — Comprehensive Dictionary Audit Report

**Date**: October 2026  
**Audited Project**: `LakiKeyboard` (`C:\Users\Latikesh\Desktop\LakiKeyboard1\LakiKeyboard`)  
**Target Architecture**: `Dictionary2Use` (Modular Android Library) & `Dictionary2Use-Words` (Dataset Repository)

---

## 1. Executive Summary

This audit inspects the complete codebase and dataset assets of the `LakiKeyboard` application to identify, categorize, and decouple dictionary lookup, spell-checking, phrase prediction, word frequency, and NLP engines from keyboard UI components.

The current implementation mixes hardcoded Java dictionary files, asset CSV text files, SQLite databases, and Android framework services directly inside the keyboard application package (`com.laki.keyboard.ai`).

---

## 2. Inventory of Discovered Files

### A. Hardcoded Java Word Dictionaries (78 Files, ~85,000+ Lines of Java)
- **English Word Dictionaries** (`com/laki/keyboard/ai/dictionaries/words/english/`):
  `AWordDictionary.java` through `ZWordDictionary.java` (26 files).
  - Example: `AWordDictionary.java` (5,438 lines), `SWordDictionary.java` (9,101 lines), `PWordDictionary.java` (6,521 lines).
  - Populates `Map<String, Byte>` maps hardcoded in Java methods.
- **Hinglish Word Dictionaries** (`com/laki/keyboard/ai/dictionaries/words/hinglish/`):
  `AHinglishWord.java` through `ZHinglishWord.java` (26 files).
- **Person Name Dictionaries** (`com/laki/keyboard/ai/dictionaries/personname/`):
  `APersonNameDictionary.java` through `ZPersonNameDictionary.java` (26 files).

### B. Dictionary Aggregators & Dispatchers
- `com/laki/keyboard/ai/dictionaries/DictionaryCall.java`: Aggregates English, Hinglish, and Person Name dictionaries dynamically per character ('a'..'z').
- `com/laki/keyboard/ai/dictionaries/DictionaryStats.java`: Utility that calculates total word count across letter dictionaries.
- `com/laki/keyboard/ai/tools/DictionaryBuilder.java`: Generator tool that packages extracted word maps into chunked `CommunityDictionaryX.java` files.

### C. N-Gram & Next-Word Prediction Engine
- `com/laki/keyboard/ai/dictionaries/ngram/PhraseDictionary.java`: Multilingual n-gram phrase map for English, Hinglish, and Marathi.
- `com/laki/keyboard/ai/dictionaries/ngram/PhraseEngine.java`: Orchestrates context-aware phrase lookups.
- `com/laki/keyboard/ai/dictionaries/ngram/BasePhraseData.java`, `IndicPhraseData.java`, `EnglishPhraseData.java`, `PhraseUtils.java`.

### D. SymSpell & Spell-Checking System
- `com/laki/keyboard/ai/spellcheck/SymSpell.java`: Public interface for SymSpell spellchecker.
- `com/laki/keyboard/ai/spellcheck/SymSpellImpl.java`: Implementation of symmetric delete spelling correction algorithm.
- `com/laki/keyboard/ai/spellcheck/SymSpellBuilder.java`: Builder class for initializing SymSpell lexicons.
- `com/laki/keyboard/ai/spellcheck/SymSpellSetupK.java`: Asset parser loading `words*.txt` from Android assets into unigrams and bigrams.
- `com/laki/keyboard/ai/spellcheck/apis/`: `Bigram`, `SuggestItem`, `DamerauLevenshteinOSA`, `StringDistance`, `CharComparator`, `DefaultCharComparator`, `JSymSpellException`.

### E. Raw Asset Files
- `app/src/main/assets/words.txt` (2,566 lines): CSV dataset mapping words to integer frequencies (`word,frequency`).
- `app/src/main/assets/names` (14 lines): Starter list of custom names.
- `app/src/main/assets/laki.class`: Binary resource file.

### F. Loaders & Extensible Data Sources
- `com/laki/keyboard/ai/loaders/ContactDictionaryLoader.java`: Reads device contacts via `ContactsContract`.
- `com/laki/keyboard/ai/loaders/CommunityDictionaryLoader.java`: Reflection-based loader for community-contributed dictionary classes.
- `com/laki/keyboard/ai/loaders/DictionarySource.java`, `CommunityDictionarySource.java`.

### G. Orchestration & Core AI Components
- `com/laki/keyboard/ai/engine/AIEngine.java`: Central singleton orchestrating SymSpell, phrase lookups, lazy dictionary preloading, and user word learning.
- `com/laki/keyboard/ai/engine/JustGuesser.java`, `KeyboardProximity.java`.
- `com/laki/keyboard/ai/suggestions/`: `Suggestion`, `SuggestionScorer`, `filter/SuggestionFilterPipeline`, `FirstCharCompatibilityRule`, `LengthDeltaRule`, `NoisePatternRule`, `WordShapeRule`.
- `com/laki/keyboard/ai/correction/`: `CorrectionEngine`, `CorrectionLearner`, `smart/AutoCorrectDecisionEngine`, `AutoCorrectScorer`, `AutoCorrectPolicy`, `WordShapeAnalyzer`.
- `com/laki/keyboard/ai/safety/`: `WordSafetyGate`, `StrictOfflineLearningGuard`, `rules/`.
- `com/laki/keyboard/ai/languages/`: `Sanscript`, `HindiRules`, `MarathiRules`, `HinglishToDevanagariEngine`.

---

## 3. Critical Architectural Issues in Current Project

1. **Massive Compilation & DEX Overhead**: Storing 85,000+ lines of hardcoded Java map initialization (`words.put("...", (byte)...)`) in DEX bytecode causes huge APK bloat (~15MB+), long compilation times, and excessive JVM heap usage on startup.
2. **Coupling with IME Lifecycle**: `AIEngine` and `SymSpellSingleton` are tied to IME context, `UserDictionaryDatabase` SQLite instance, and specific UI views.
3. **Lack of Versioning & Remote Updates**: Dictionary datasets cannot be updated remotely or downloaded on demand without recompiling and redeploying the APK.
4. **Scattered Datasets**: Words are split across 78 Java classes, `words.txt`, `names`, `PhraseDictionary`, and `IndicPhraseData`.

---

## 4. Recommended Target Architecture

### Repository 1: `Dictionary2Use` (`dictionary2use`)
- Lightweight Android Library (`.aar`).
- Contains Java API, binary/SQLite lookup engine, SymSpell correction, phrase prediction, caching, and OTA update client.
- Zero hardcoded word lists in Java source code.
- Dependencies: Standard Android SDK, OkHttp / HttpURLConnection (for dataset updates), SQLite.

### Repository 2: `Dictionary2Use-Words` (`dictionary2use-words`)
- Structured dataset repository containing raw and optimized datasets:
  - English unigrams & frequencies (`english_words.json`, `english_words.db`)
  - Hinglish unigrams & frequencies (`hinglish_words.json`, `hinglish_words.db`)
  - Person names (`person_names.json`)
  - Phrases & N-Grams (`phrases.json`)
  - Language metadata (`metadata.json`) with versioning & SHA-256 checksums.

---

## 5. Summary Matrix of File Dispositions

| File / Component | Lines / Size | Current Function | Target Action in `Dictionary2Use` |
|---|---|---|---|
| `*WordDictionary.java` (78 files) | 85,000+ lines | Hardcoded Java word maps | Export data to `dictionary2use-words` JSON/SQLite; replace in library with binary SQLite database |
| `words.txt` | 2,566 lines | Asset CSV file | Convert to structured JSON & SQLite starter dataset |
| `PhraseDictionary.java` | 531 lines | Hardcoded N-gram predictions | Modularize into `PhraseEngine` with downloadable/configurable phrase provider |
| `SymSpellImpl.java` | 474 lines | Symmetric Delete spellchecker | Integrate into `com.dictionary.use.engine.SymSpellEngine` |
| `AIEngine.java` | 561 lines | AI orchestrator | Extract into clean `Dictionary2Use` client class with fluent `Builder` API |
| `UserDictionaryDatabase.java` | ~200 lines | SQLite user dictionary | Refactor into `UserDictionaryStore` for dynamic local learning & caching |

