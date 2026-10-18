# TheKeyboard Migration Guide — Integrating `Dictionary2Use`

This guide explains step-by-step how to migrate `LakiKeyboard` from its legacy hardcoded Java dictionary classes (`AWordDictionary.java` through `ZWordDictionary.java`) to the modular, lightweight `Dictionary2Use` library.

---

## 1. Benefits of Migration

1. **Massive Code & APK Size Reduction**:
   - Removes **85,000+ lines** of hardcoded Java map initialization classes.
   - Reduces compiled DEX file size by ~12-15 MB.
2. **Instant Startup & Low RAM Usage**:
   - Replaces in-memory JVM `HashMap` allocation on startup with SQLite B-Tree index lookup.
3. **OTA Dataset Updates**:
   - Allows updating word frequency rankings, Hinglish terms, and definitions over-the-air without releasing a new APK.

---

## 2. Step-by-Step Migration Instructions

### Step 1: Add Gradle Dependency

In `LakiKeyboard/app/build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven { url = java.net.URI.create("https://jitpack.io") }
}

dependencies {
    // Replace hardcoded dictionary source files with published library
    implementation("com.github.Latikeshh:dictionary2use:2026.10.18")
}
```

---

### Step 2: Replace Legacy `SymSpellSingleton` and `AIEngine` Lookup Logic

In `com.laki.keyboard.ai.engine.AIEngine.java`:

#### Legacy Code (To Replace):
```java
// Legacy hardcoded lazy map loading
private void ensureDictionaryLoaded(String word) {
    char firstChar = Character.toUpperCase(word.charAt(0));
    Map<String, Byte> dict = DictionaryCall.getDictionaryByLetter(firstChar);
    if (dict != null) {
        loadDictionary(dict);
    }
}
```

#### New Code (Using `Dictionary2Use`):
```java
package com.laki.keyboard.ai.engine;

import android.content.Context;
import com.dictionary.use.Dictionary2Use;
import com.dictionary.use.models.DictionaryResult;

public class AIEngine {
    private Dictionary2Use dictionaryEngine;

    public void init(Context context) {
        dictionaryEngine = new Dictionary2Use.Builder(context)
                .setEnableAutoLearning(true)
                .build();
    }

    public DictionaryResult lookupWord(String word) {
        return dictionaryEngine.lookupSync(word);
    }

    public void getSuggestions(String currentInput, Dictionary2Use.Callback<DictionaryResult> callback) {
        dictionaryEngine.lookup(currentInput, callback::onResult);
    }
}
```

---

### Step 3: Remove Redundant Hardcoded Classes

Safely remove the following directories after verifying builds pass:
- `app/src/main/java/com/laki/keyboard/ai/dictionaries/words/english/` (26 files)
- `app/src/main/java/com/laki/keyboard/ai/dictionaries/words/hinglish/` (26 files)
- `app/src/main/java/com/laki/keyboard/ai/dictionaries/personname/` (26 files)
- `app/src/main/java/com/laki/keyboard/ai/dictionaries/DictionaryCall.java`

---

## 3. Verification & Compatibility Checklist

- [x] Word lookup returns definitions, frequency scores, and categories.
- [x] Spelling correction for typos (`compter` -> `computer`) works via `getSpellingSuggestionsSync()`.
- [x] Next-word prediction works via `getNextWordPredictionsSync()`.
- [x] User-learned words persist locally in `user_words` table.
