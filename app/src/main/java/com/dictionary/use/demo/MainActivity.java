package com.dictionary.use.demo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dictionary.use.Dictionary2Use;
import com.dictionary.use.models.DictionaryResult;
import com.dictionary.use.models.DictionaryStatus;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Dictionary2Use dictionary;
    private TextView tvStatus;
    private EditText etInput;
    private TextView tvResult;
    private Button btnLookup;
    private Button btnPrefix;
    private Button btnLearn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        etInput = findViewById(R.id.etInput);
        tvResult = findViewById(R.id.tvResult);
        btnLookup = findViewById(R.id.btnLookup);
        btnPrefix = findViewById(R.id.btnPrefix);
        btnLearn = findViewById(R.id.btnLearn);

        // Initialize Dictionary2Use library instance
        dictionary = new Dictionary2Use.Builder(this)
                .setEnableAutoLearning(true)
                .build();

        updateStatusDisplay();

        btnLookup.setOnClickListener(v -> performLookup());
        btnPrefix.setOnClickListener(v -> performPrefixSearch());
        btnLearn.setOnClickListener(v -> performLearnWord());
    }

    private void updateStatusDisplay() {
        DictionaryStatus status = dictionary.getStatus();
        String text = "Installed: " + status.isInstalled() +
                " | Version: " + status.getVersion() +
                " | Total Words: " + status.getWordCount();
        tvStatus.setText(text);
    }

    private void performLookup() {
        String input = etInput.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter a word", Toast.LENGTH_SHORT).show();
            return;
        }

        dictionary.lookup(input, result -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Query Word: ").append(result.getQueryWord()).append("\n");
            sb.append("Found: ").append(result.isFound()).append("\n");

            if (result.isFound()) {
                sb.append("Category: ").append(result.getCategory()).append("\n");
                sb.append("Frequency: ").append(result.getFrequency()).append("\n");
                sb.append("Definition: ").append(result.getDefinition()).append("\n");
                if (!result.getSynonyms().isEmpty()) {
                    sb.append("Synonyms: ").append(String.join(", ", result.getSynonyms())).append("\n");
                }

                List<String> nextWords = dictionary.getNextWordPredictionsSync(result.getQueryWord(), 5);
                if (!nextWords.isEmpty()) {
                    sb.append("Next Word Predictions: ").append(String.join(", ", nextWords)).append("\n");
                }
            } else {
                sb.append("Definition: Word not found in local dictionary.\n");
                if (!result.getSuggestions().isEmpty()) {
                    sb.append("Spelling Suggestions: ").append(String.join(", ", result.getSuggestions())).append("\n");
                }
            }

            tvResult.setText(sb.toString());
            updateStatusDisplay();
        });
    }

    private void performPrefixSearch() {
        String prefix = etInput.getText().toString().trim();
        if (prefix.isEmpty()) {
            Toast.makeText(this, "Please enter a prefix to search", Toast.LENGTH_SHORT).show();
            return;
        }

        List<DictionaryResult> matches = dictionary.searchPrefixSync(prefix, 10);
        StringBuilder sb = new StringBuilder();
        sb.append("Autocompletions for '").append(prefix).append("':\n\n");

        if (matches.isEmpty()) {
            sb.append("No matching words found.");
        } else {
            for (DictionaryResult r : matches) {
                sb.append("• ").append(r.getQueryWord()).append(" (Freq: ").append(r.getFrequency()).append(")\n");
            }
        }

        tvResult.setText(sb.toString());
    }

    private void performLearnWord() {
        String word = etInput.getText().toString().trim();
        if (word.isEmpty()) {
            Toast.makeText(this, "Please enter a word to learn", Toast.LENGTH_SHORT).show();
            return;
        }

        dictionary.learnWord(word);
        Toast.makeText(this, "Learned new word: " + word, Toast.LENGTH_SHORT).show();
        updateStatusDisplay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dictionary != null) {
            dictionary.close();
        }
    }
}
