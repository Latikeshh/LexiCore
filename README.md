# Dictionary2Use (`dictionary2use`)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Version](https://img.shields.io/badge/Release-2026.10.18-green.svg)](https://github.com/Latikeshh/dictionary2use/releases/tag/v2026.10.18)

**Dictionary2Use** is a lightweight, offline-first, high-performance Android dictionary and spellchecking library written in Java.

---

## 🌟 Key Features

- ⚡ **Offline-First B-Tree Indexed Lookup**: Fast local lookups with zero network requests using SQLite.
- 🔍 **Prefix Autocompletion & Next-Word Prediction**: Instant phrase predictions for keyboard search and input methods.
- 🔤 **Symmetric Delete Spelling Correction**: Symmetric Delete algorithm for fast, low-memory typo correction.
- ☁️ **Secure Over-The-Air (OTA) Updates**: Secure background dataset downloads with SHA-256 checksum verification and atomic database swapping.
- 📦 **Zero Hardcoded Word Bloat**: Keeps code clean and lightweight.

---

## 🚀 Installation & Gradle Setup

Add JitPack repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI.create("https://jitpack.io") }
    }
}
```

Add the dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.Latikeshh:dictionary2use:2026.10.18")
}
```

---

## 💻 Public Java API Usage

### 1. Basic Synchronous & Asynchronous Lookup

```java
Dictionary2Use dictionary = new Dictionary2Use.Builder(context)
        .setEnableAutoLearning(true)
        .build();

// Asynchronous Lookup
dictionary.lookup("computer", result -> {
    if (result.isFound()) {
        String definition = result.getDefinition();
        long frequency = result.getFrequency();
    } else {
        List<String> suggestions = result.getSuggestions(); // e.g. "computer" for "compter"
    }
});

// Synchronous Safe Lookup
DictionaryResult syncResult = dictionary.lookupSync("android");
```

### 2. Prefix Search (Autocompletion)

```java
List<DictionaryResult> matches = dictionary.searchPrefixSync("comp", 5);
for (DictionaryResult res : matches) {
    Log.i("Demo", "Matching word: " + res.getQueryWord());
}
```

### 3. Next-Word Phrase Predictions

```java
List<String> nextWords = dictionary.getNextWordPredictionsSync("good", 3);
// Returns: ["morning", "night", "luck"]
```

### 4. Online OTA Dataset Updates

```java
dictionary.checkForUpdates(updateInfo -> {
    if (updateInfo.isUpdateAvailable()) {
        dictionary.downloadUpdate(updateInfo, new UpdateProgressCallback() {
            @Override
            public void onProgress(int percent, long bytesDownloaded, long totalBytes) {
                Log.i("Update", "Progress: " + percent + "%");
            }

            @Override
            public void onSuccess(String installedVersion) {
                Log.i("Update", "Successfully installed version: " + installedVersion);
            }

            @Override
            public void onError(Exception exception) {
                Log.e("Update", "Failed: " + exception.getMessage());
            }
        });
    }
});
```

---

## 🛠️ GitHub Publishing & Release Instructions

To publish new releases to JitPack:

1. Commit all files to Git:
   ```bash
   git add .
   git commit -m "Release version 2026.10.18"
   ```
2. Tag release version:
   ```bash
   git tag -a v2026.10.18 -m "Release v2026.10.18"
   ```
3. Push to GitHub repository `Latikeshh/dictionary2use`:
   ```bash
   git push origin main --tags
   ```

JitPack will automatically build the AAR release artifact at:  
`com.github.Latikeshh:dictionary2use:2026.10.18`

---

## 📄 License

Licensed under the Apache License, Version 2.0. See `LICENSE` for details.
