import json
import sqlite3
import os

def build_database(json_dir, output_db_path):
    if os.path.exists(output_db_path):
        os.remove(output_db_path)

    conn = sqlite3.connect(output_db_path)
    cur = conn.cursor()

    cur.execute('''
    CREATE TABLE words (
        word TEXT PRIMARY KEY,
        frequency INTEGER DEFAULT 1,
        definition TEXT,
        synonyms TEXT,
        category TEXT,
        language TEXT
    )''')

    cur.execute('CREATE INDEX idx_words_word ON words(word)')
    cur.execute('CREATE INDEX idx_words_prefix ON words(word, frequency DESC)')

    cur.execute('''
    CREATE TABLE phrases (
        prev_word TEXT,
        next_word TEXT,
        score INTEGER DEFAULT 1,
        PRIMARY KEY (prev_word, next_word)
    )''')

    cur.execute('CREATE INDEX idx_phrases_prev ON phrases(prev_word, score DESC)')

    cur.execute('''
    CREATE TABLE user_words (
        word TEXT PRIMARY KEY,
        frequency INTEGER DEFAULT 1,
        last_used INTEGER
    )''')

    cur.execute('''
    CREATE TABLE metadata (
        key TEXT PRIMARY KEY,
        value TEXT
    )''')

    cur.execute("INSERT INTO metadata (key, value) VALUES ('version', '2026.10.18')")

    # Populate words
    for fname in ['english_words.json', 'hinglish_words.json', 'person_names.json']:
        fpath = os.path.join(json_dir, fname)
        if os.path.exists(fpath):
            with open(fpath, 'r', encoding='utf-8') as f:
                items = json.load(f)
                for item in items:
                    cur.execute(
                        "INSERT OR REPLACE INTO words (word, frequency, definition, synonyms, category, language) VALUES (?, ?, ?, ?, ?, ?)",
                        (
                            item.get('word', '').lower().strip(),
                            item.get('frequency', 1),
                            item.get('definition', ''),
                            item.get('synonyms', ''),
                            item.get('category', 'General'),
                            item.get('language', 'English')
                        )
                    )

    # Populate phrases
    phrases_path = os.path.join(json_dir, 'phrases.json')
    if os.path.exists(phrases_path):
        with open(phrases_path, 'r', encoding='utf-8') as f:
            phrases = json.load(f)
            for item in phrases:
                cur.execute(
                    "INSERT OR REPLACE INTO phrases (prev_word, next_word, score) VALUES (?, ?, ?)",
                    (
                        item.get('prev_word', '').lower().strip(),
                        item.get('next_word', '').lower().strip(),
                        item.get('score', 1)
                    )
                )

    conn.commit()
    conn.close()
    print(f"Database successfully generated at {output_db_path}")

if __name__ == '__main__':
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    json_dir = os.path.join(base_dir, 'datasets')
    out_path = os.path.join(base_dir, 'dictionary2use.db')
    build_database(json_dir, out_path)
