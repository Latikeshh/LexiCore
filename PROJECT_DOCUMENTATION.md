# Dictionary2Use — Comprehensive Source Code & System Documentation

**Project Name**: Dictionary2Use  
**Published Package**: `com.github.Latikeshh:dictionary2use:2026.10.18`  
**Target Environment**: Android SDK 24 to 35+ (Java 11)  
**Root Path**: `C:\Users\Latikesh\AndroidStudioProjects\Dictionary2Use\`  

---

## 1. System Overview & Architecture

`Dictionary2Use` is a lightweight, offline-first, high-performance Android dictionary and spellchecking engine written in clean, thread-safe Java.

The project is structured into a multi-module architecture:
1. **`:dictionary2use`** (Android Library Module): Standalone library engine (`com.dictionary.use`) containing public APIs, SQLite storage, extracted SymSpell spellchecking (`com.dictionary.use.spellcheck`), phrase prediction, asset installers, and OTA update managers.
2. **`:app`** (Demo Android Application): Sample application (`com.dictionary.use.demo`) demonstrating real-world usage of `Dictionary2Use`.
3. **`dictionary2use-words`** (Dataset Storage Repository): Separate repository hosting raw JSON datasets (`english_words.json`, `hinglish_words.json`, `person_names.json`, `phrases.json`), manifests, and a Python SQLite build tool.

---

## 2. Detailed File-by-File Documentation

### A. Extracted SymSpell Engine (`com.dictionary.use.spellcheck`)

#### 1. [com/dictionary/use/spellcheck/SymSpell.java](file:///C:/Users/Latikesh/AndroidStudioProjects/Dictionary2Use/dictionary2use/src/main/java/com/dictionary/use/spellcheck/SymSpell.java)
- **Purpose**: Core SymSpell interface extracted from `LakiKeyboard`.
- **Responsibilities**: Contract for `lookup`, `lookupCompound`, `addWord`, and lexicon access.

#### 2. [com/dictionary/use/spellcheck/SymSpellImpl.java](file:///C:/Users/Latikesh/AndroidStudioProjects/Dictionary2Use/dictionary2use/src/main/java/com/dictionary/use/spellcheck/SymSpellImpl.java)
- **Purpose**: Implementation of Symmetric Delete spelling correction algorithm.
- **Responsibilities**: Generates edit distance deletes, manages unigram/bigram lexicons, calculates candidate costs, and ranks spelling suggestions.

#### 3. [com/dictionary/use/spellcheck/SymSpellBuilder.java](file:///C:/Users/Latikesh/AndroidStudioProjects/Dictionary2Use/dictionary2use/src/main/java/com/dictionary/use/spellcheck/SymSpellBuilder.java)
- **Purpose**: Builder pattern implementation for instantiating `SymSpellImpl`.

#### 4. [com/dictionary/use/spellcheck/Verbosity.java](file:///C:/Users/Latikesh/AndroidStudioProjects/Dictionary2Use/dictionary2use/src/main/java/com/dictionary/use/spellcheck/Verbosity.java)
- **Purpose**: Candidate verbosity filter enum (`TOP`, `CLOSEST`, `ALL`).

#### 5. [com/dictionary/use/spellcheck/api/DamerauLevenshteinOSA.java](file:///C:/Users/Latikesh/AndroidStudioProjects/Dictionary2Use/dictionary2use/src/main/java/com/dictionary/use/spellcheck/api/DamerauLevenshteinOSA.java)
- **Purpose**: Optimal String Alignment Damerau-Levenshtein distance algorithm with early stop optimization.

#### 6. [com/dictionary/use/spellcheck/api/SuggestItem.java](file:///C:/Users/Latikesh/AndroidStudioProjects/Dictionary2Use/dictionary2use/src/main/java/com/dictionary/use/spellcheck/api/SuggestItem.java)
- **Purpose**: Immutable model for spelling suggestions implementing `Comparable<SuggestItem>`.

---

### B. Core Library Facade & Engine (`com.dictionary.use`)

#### 7. [dictionary2use/src/main/java/com/dictionary/use/Dictionary2Use.java](file:///C:/Users/Latikesh/AndroidStudioProjects/Dictionary2Use/dictionary2use/src/main/java/com/dictionary/use/Dictionary2Use.java)
- **Purpose**: Primary public entry point and facade for external Android applications.
- **Responsibilities**:
  - Provides a fluent `Builder(Context)` constructor.
  - Manages background `ExecutorService` thread pools and UI `Handler` thread dispatchers.
  - Exposes `lookupSync(word)` and `lookup(word, callback)`.
  - Exposes `searchPrefixSync(prefix, limit)` for autocompletions.
  - Exposes `getNextWordPredictionsSync(previousWord, limit)` for n-gram predictions.
  - Exposes `getSpellingSuggestionsSync(word, maxEditDistance)` for typo corrections.
  - Exposes `learnWord(word)` for dynamic user word learning.
  - Exposes `checkForUpdates()` and `downloadUpdate()` for OTA dataset updates.

#### 8. [dictionary2use/src/main/java/com/dictionary/use/engine/SQLiteDictionaryStore.java](file:///C:/Users/Latikesh/AndroidStudioProjects/Dictionary2Use/dictionary2use/src/main/java/com/dictionary/use/engine/SQLiteDictionaryStore.java)
- **Purpose**: Primary SQLite B-Tree indexed database provider.
- **Responsibilities**:
  - Manages `dictionary2use.db` in `context.getDatabasePath()`.
  - Creates tables `words`, `phrases`, `user_words`, and `metadata`.
  - Executes B-Tree indexed queries for words, prefixes, and phrases.

#### 9. [dictionary2use/src/main/java/com/dictionary/use/engine/StarterDatasetInstaller.java](file:///C:/Users/Latikesh/AndroidStudioProjects/Dictionary2Use/dictionary2use/src/main/java/com/dictionary/use/engine/StarterDatasetInstaller.java)
- **Purpose**: Asset installer that seeds SQLite database on first launch.

#### 10. [dictionary2use/src/main/java/com/dictionary/use/engine/OnlineUpdateManager.java](file:///C:/Users/Latikesh/AndroidStudioProjects/Dictionary2Use/dictionary2use/src/main/java/com/dictionary/use/engine/OnlineUpdateManager.java)
- **Purpose**: Secure background OTA dataset updater supporting HTTP download, SHA-256 validation, and atomic database swapping.

---

## 3. Component Dependency Map & Execution Diagram

```
[External Android Application]
              │
              ▼
    [com.dictionary.use.Dictionary2Use]
         │           │             │
         │           │             ▼
         │           │    [SymSpellImpl / SymSpellEngine]
         │           │    (Extracted Symmetric Delete Engine)
         │           ▼
         │    [OnlineUpdateManager]
         │    (HTTP OTA Updates & SHA-256)
         ▼
[IDictionaryProvider]
         │
         ▼
[SQLiteDictionaryStore] ◄──── [StarterDatasetInstaller]
         │                     (Asset Seeder)
         ▼
[dictionary2use.db] (SQLite Disk B-Tree)
  ├── words (Indexed)
  ├── phrases (Indexed)
  ├── user_words (Indexed)
  └── metadata
```

---

## 4. Verification Guide in Android Studio

### Test 1: Verify All Unit Tests (15 Passed)
- **Action**: In Android Studio, right click `dictionary2use/src/test/java` and select **Run 'Tests in 'com.dictionary.use''** (or run `./gradlew :dictionary2use:test`).
- **Expected Result**: **15 unit tests pass** across `Dictionary2UseUnitTest` (8 tests) and `SymSpellUnitTest` (7 tests).

### Test 2: Verify SymSpell Spelling Corrections in Demo App
- **Action**: Run `:app` configuration on an emulator or Android device. Type `compter` and tap **Lookup**.
- **Expected Result**: Displays `Found: false`, `Spelling Suggestions: computer`.

### Test 3: Verify Compound Phrase Spelling Corrections
- **Action**: In `SymSpellUnitTest`, `lookupCompound("compter teh", 2, true)` returns `["computer", "the"]`.
