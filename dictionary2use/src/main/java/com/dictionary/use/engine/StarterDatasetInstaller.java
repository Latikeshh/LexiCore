package com.dictionary.use.engine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Seeder that extracts and installs initial starter dictionary datasets into SQLite on first launch.
 */
public class StarterDatasetInstaller {
    private static final String TAG = "StarterDatasetInstaller";

    public static void ensureStarterDatasetInstalled(Context context, SQLiteDictionaryStore store) {
        if (store == null || !store.isReady()) return;

        SQLiteDatabase db = store.getDatabase();
        if (db == null) return;

        // Check if database already has words
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM words", null);
            if (cursor != null && cursor.moveToFirst() && cursor.getLong(0) > 0) {
                // Dataset already seeded
                return;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error checking words count: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        Log.i(TAG, "Database empty. Installing starter dataset...");

        db.beginTransaction();
        try {
            // Attempt to load from asset JSON if present
            boolean assetLoaded = loadFromAssetJson(context, db);

            if (!assetLoaded) {
                // Populate default core lexicon if asset file is missing
                populateDefaultStarterWords(db);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, "Starter dataset installation completed successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Failed installing starter dataset: " + e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
    }

    private static boolean loadFromAssetJson(Context context, SQLiteDatabase db) {
        try (InputStream is = context.getAssets().open("starter_dictionary.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject json = new JSONObject(sb.toString());

            if (json.has("words")) {
                JSONArray wordsArray = json.getJSONArray("words");
                for (int i = 0; i < wordsArray.length(); i++) {
                    JSONObject obj = wordsArray.getJSONObject(i);
                    ContentValues cv = new ContentValues();
                    cv.put("word", obj.getString("word"));
                    cv.put("frequency", obj.optLong("frequency", 100));
                    cv.put("definition", obj.optString("definition", ""));
                    cv.put("synonyms", obj.optString("synonyms", ""));
                    cv.put("category", obj.optString("category", "General"));
                    cv.put("language", obj.optString("language", "English"));
                    db.insertWithOnConflict("words", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }

            if (json.has("phrases")) {
                JSONArray phrasesArray = json.getJSONArray("phrases");
                for (int i = 0; i < phrasesArray.length(); i++) {
                    JSONObject obj = phrasesArray.getJSONObject(i);
                    ContentValues cv = new ContentValues();
                    cv.put("prev_word", obj.getString("prev_word"));
                    cv.put("next_word", obj.getString("next_word"));
                    cv.put("score", obj.optInt("score", 10));
                    db.insertWithOnConflict("phrases", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }

            if (json.has("version")) {
                ContentValues cv = new ContentValues();
                cv.put("key", "version");
                cv.put("value", json.getString("version"));
                db.insertWithOnConflict("metadata", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }

            return true;
        } catch (Exception e) {
            Log.w(TAG, "starter_dictionary.json asset not available or invalid: " + e.getMessage());
            return false;
        }
    }

    private static void populateDefaultStarterWords(SQLiteDatabase db) {
        // High frequency core English words
        String[][] defaultWords = {
                {"computer", "120", "An electronic device for storing and processing data.", "pc,laptop,workstation", "Technology", "English"},
                {"dictionary", "115", "A reference book or online resource containing words and meanings.", "lexicon,vocabulary,wordbook", "General", "English"},
                {"android", "110", "An open-source mobile operating system developed by Google.", "mobile,os", "Technology", "English"},
                {"java", "105", "A high-level, class-based, object-oriented programming language.", "code,language", "Technology", "English"},
                {"keyboard", "100", "An input device consisting of keys used to type text.", "keypad,input", "Technology", "English"},
                {"mobile", "95", "Able to move or be moved freely or easily.", "cellular,portable", "General", "English"},
                {"application", "90", "A program or piece of software designed for end-users.", "app,program,software", "Technology", "English"},
                {"library", "85", "A collection of resources or code modules.", "repository,archive", "General", "English"},
                {"developer", "80", "A person that creates software applications or systems.", "engineer,programmer", "Technology", "English"},
                {"system", "75", "A set of principles or procedures according to which something is done.", "network,structure", "General", "English"}
        };

        for (String[] entry : defaultWords) {
            ContentValues cv = new ContentValues();
            cv.put("word", entry[0]);
            cv.put("frequency", Long.parseLong(entry[1]));
            cv.put("definition", entry[2]);
            cv.put("synonyms", entry[3]);
            cv.put("category", entry[4]);
            cv.put("language", entry[5]);
            db.insertWithOnConflict("words", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }

        // Phrase predictions
        String[][] defaultPhrases = {
                {"android", "developer", "20"},
                {"android", "studio", "18"},
                {"android", "app", "15"},
                {"computer", "science", "25"},
                {"computer", "system", "20"},
                {"dictionary", "library", "15"},
                {"good", "morning", "30"},
                {"good", "night", "28"},
                {"thank", "you", "35"}
        };

        for (String[] entry : defaultPhrases) {
            ContentValues cv = new ContentValues();
            cv.put("prev_word", entry[0]);
            cv.put("next_word", entry[1]);
            cv.put("score", Integer.parseInt(entry[2]));
            db.insertWithOnConflict("phrases", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }
}
