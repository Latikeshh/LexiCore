# Dictionary2Use-Words (`dictionary2use-words`)

Official dataset repository for the [Dictionary2Use](https://github.com/Latikeshh/dictionary2use) Android library.

## 📦 Contents

- **`manifest.json`**: Dataset metadata, versioning, SHA-256 checksums, and update notes.
- **`datasets/english_words.json`**: English words with frequency rankings and concise definitions.
- **`datasets/hinglish_words.json`**: Transliterated Hinglish and Indic vocabulary.
- **`datasets/person_names.json`**: Indian and international given names dataset.
- **`datasets/phrases.json`**: Bi-gram and tri-gram next-word prediction dataset.
- **`tools/build_sqlite.py`**: Python compiler tool to package JSON datasets into an indexed SQLite `.db` artifact.

## 🚀 Building SQLite Dataset

Run the provided Python compilation script to build `dictionary2use.db`:

```bash
python tools/build_sqlite.py
```

## 📜 Dataset Provenance & Licenses

This dataset combines cleaned English unigrams, transliterated Hinglish terms, and N-gram phrases. Distributed under the Apache-2.0 License.
