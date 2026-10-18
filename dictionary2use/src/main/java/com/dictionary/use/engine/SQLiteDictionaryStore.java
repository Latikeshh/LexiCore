package com.dictionary.use.engine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dictionary.use.models.DictionaryResult;
import com.dictionary.use.models.DictionaryStatus;
import com.dictionary.use.providers.IDictionaryProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * High-performance SQLite-backed dictionary provider.
 * Implements B-Tree indexed local word lookup, prefix search, phrase predictions, and dynamic word learning.
 */
public class SQLiteDictionaryStore implements IDictionaryProvider {
    private static final String TAG = "SQLiteDictionaryStore";
    private static final String DB_NAME = "dictionary2use.db";
    private static final int DB_VERSION = 1;

    private final Context context;
    private final Helper dbHelper;
    private SQLiteDatabase db;

    public SQLiteDictionaryStore(Context context) {
        this(context, DB_NAME);
    }

    public SQLiteDictionaryStore(Context context, String dbName) {
        this.context = context.getApplicationContext();
        this.dbHelper = new Helper(this.context, dbName);
        try {
            this.db = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            Log.e(TAG, "Failed to open SQLite database: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized DictionaryResult lookup(String word) {
        if (!isReady() || word == null || word.trim().isEmpty()) {
            return DictionaryResult.notFound(word, new ArrayList<>());
        }

        String cleaned = word.trim().toLowerCase(Locale.ROOT);

        // 1. Check user-learned words first
        Cursor userCursor = null;
        try {
            userCursor = db.rawQuery("SELECT word, frequency FROM user_words WHERE word = ? LIMIT 1", new String[]{cleaned});
            if (userCursor != null && userCursor.moveToFirst()) {
                long freq = userCursor.getLong(1);
                return new DictionaryResult.Builder(word)
                        .setFound(true)
                        .setFrequency(freq)
                        .setCategory("UserLearned")
                        .setDefinition("User custom word")
                        .build();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error querying user_words: " + e.getMessage());
        } finally {
            if (userCursor != null) userCursor.close();
        }

        // 2. Query main dictionary table
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT word, frequency, definition, synonyms, category, language FROM words WHERE word = ? LIMIT 1",
                    new String[]{cleaned}
            );

            if (cursor != null && cursor.moveToFirst()) {
                String foundWord = cursor.getString(0);
                long frequency = cursor.getLong(1);
                String definition = cursor.getString(2);
                String synonymsRaw = cursor.getString(3);
                String category = cursor.getString(4);
                String language = cursor.getString(5);

                List<String> synonymsList = synonymsRaw != null && !synonymsRaw.isEmpty()
                        ? Arrays.asList(synonymsRaw.split(","))
                        : new ArrayList<>();

                return new DictionaryResult.Builder(foundWord)
                        .setFound(true)
                        .setFrequency(frequency)
                        .setDefinition(definition)
                        .setSynonyms(synonymsList)
                        .setCategory(category)
                        .setLanguage(language)
                        .build();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error looking up word in SQLite: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return DictionaryResult.notFound(word, new ArrayList<>());
    }

    @Override
    public synchronized List<DictionaryResult> searchPrefix(String prefix, int limit) {
        List<DictionaryResult> results = new ArrayList<>();
        if (!isReady() || prefix == null || prefix.trim().isEmpty()) {
            return results;
        }

        String cleaned = prefix.trim().toLowerCase(Locale.ROOT);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT word, frequency, category, language FROM words WHERE word LIKE ? || '%' ORDER BY frequency DESC LIMIT ?",
                    new String[]{cleaned, String.valueOf(limit)}
            );

            while (cursor != null && cursor.moveToNext()) {
                String w = cursor.getString(0);
                long freq = cursor.getLong(1);
                String cat = cursor.getString(2);
                String lang = cursor.getString(3);

                results.add(new DictionaryResult.Builder(w)
                        .setFound(true)
                        .setFrequency(freq)
                        .setCategory(cat)
                        .setLanguage(lang)
                        .build());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching prefix in SQLite: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return results;
    }

    @Override
    public synchronized List<String> getPredictions(String previousWord, int limit) {
        List<String> predictions = new ArrayList<>();
        if (!isReady() || previousWord == null || previousWord.trim().isEmpty()) {
            return predictions;
        }

        String cleaned = previousWord.trim().toLowerCase(Locale.ROOT);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT next_word FROM phrases WHERE prev_word = ? ORDER BY score DESC LIMIT ?",
                    new String[]{cleaned, String.valueOf(limit)}
            );

            while (cursor != null && cursor.moveToNext()) {
                predictions.add(cursor.getString(0));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying phrase predictions: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return predictions;
    }

    @Override
    public synchronized void learnWord(String word, long frequency) {
        if (!isReady() || word == null || word.trim().isEmpty()) return;

        String cleaned = word.trim().toLowerCase(Locale.ROOT);
        try {
            ContentValues cv = new ContentValues();
            cv.put("word", cleaned);
            cv.put("frequency", frequency);
            cv.put("last_used", System.currentTimeMillis());

            db.insertWithOnConflict("user_words", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            Log.e(TAG, "Error learning user word: " + e.getMessage());
        }
    }

    @Override
    public synchronized boolean isReady() {
        return db != null && db.isOpen();
    }

    @Override
    public synchronized DictionaryStatus getStatus() {
        boolean ready = isReady();
        long wordCount = 0;
        long phraseCount = 0;
        String version = "1.0.0";
        String dbPath = "";

        if (ready) {
            File dbFile = context.getDatabasePath(dbHelper.getDatabaseName());
            if (dbFile.exists()) {
                dbPath = dbFile.getAbsolutePath();
            }

            Cursor c1 = null;
            try {
                c1 = db.rawQuery("SELECT COUNT(*) FROM words", null);
                if (c1 != null && c1.moveToFirst()) wordCount = c1.getLong(0);
            } catch (Exception ignored) {
            } finally {
                if (c1 != null) c1.close();
            }

            Cursor c2 = null;
            try {
                c2 = db.rawQuery("SELECT COUNT(*) FROM phrases", null);
                if (c2 != null && c2.moveToFirst()) phraseCount = c2.getLong(0);
            } catch (Exception ignored) {
            } finally {
                if (c2 != null) c2.close();
            }

            Cursor c3 = null;
            try {
                c3 = db.rawQuery("SELECT value FROM metadata WHERE key = 'version' LIMIT 1", null);
                if (c3 != null && c3.moveToFirst()) version = c3.getString(0);
            } catch (Exception ignored) {
            } finally {
                if (c3 != null) c3.close();
            }
        }

        return new DictionaryStatus(ready, version, wordCount, phraseCount, System.currentTimeMillis(), dbPath);
    }

    @Override
    public synchronized void close() {
        if (db != null && db.isOpen()) {
            try {
                db.close();
            } catch (Exception ignored) {
            }
        }
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }

    private static class Helper extends SQLiteOpenHelper {

        Helper(Context context, String dbName) {
            super(context, dbName, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS words (" +
                    "word TEXT PRIMARY KEY, " +
                    "frequency INTEGER DEFAULT 1, " +
                    "definition TEXT, " +
                    "synonyms TEXT, " +
                    "category TEXT, " +
                    "language TEXT)");

            db.execSQL("CREATE INDEX IF NOT EXISTS idx_words_word ON words(word)");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_words_prefix ON words(word, frequency DESC)");

            db.execSQL("CREATE TABLE IF NOT EXISTS phrases (" +
                    "prev_word TEXT, " +
                    "next_word TEXT, " +
                    "score INTEGER DEFAULT 1, " +
                    "PRIMARY KEY (prev_word, next_word))");

            db.execSQL("CREATE INDEX IF NOT EXISTS idx_phrases_prev ON phrases(prev_word, score DESC)");

            db.execSQL("CREATE TABLE IF NOT EXISTS user_words (" +
                    "word TEXT PRIMARY KEY, " +
                    "frequency INTEGER DEFAULT 1, " +
                    "last_used INTEGER)");

            db.execSQL("CREATE TABLE IF NOT EXISTS metadata (" +
                    "key TEXT PRIMARY KEY, " +
                    "value TEXT)");

            db.execSQL("INSERT OR REPLACE INTO metadata (key, value) VALUES ('version', '2026.10.18')");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS words");
            db.execSQL("DROP TABLE IF EXISTS phrases");
            db.execSQL("DROP TABLE IF EXISTS user_words");
            db.execSQL("DROP TABLE IF EXISTS metadata");
            onCreate(db);
        }
    }
}
