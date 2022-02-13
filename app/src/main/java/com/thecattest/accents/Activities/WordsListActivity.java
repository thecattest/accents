package com.thecattest.accents.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.thecattest.accents.Managers.WordsManager;
import com.thecattest.accents.R;
import com.thecattest.accents.WordsAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class WordsListActivity extends AppCompatActivity {

    private TextInputEditText searchBar;
    private ListView wordsListView;

    private ArrayList<String> words;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words_list);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        wordsListView = findViewById(R.id.wordsListView);
        WordsManager wordsManager = new WordsManager(getApplicationContext());
        words = wordsManager.readWords();
        Collections.sort(words, (s1, s2) -> s1.toLowerCase(Locale.ROOT).compareTo(s2.toLowerCase(Locale.ROOT)));
        updateWords(words);

        searchBar = findViewById(R.id.wordsSearch);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    updateWords(words);
                    return;
                }
                ArrayList<String> filteredWords = new ArrayList<>();
                for (int ind = 0; ind < words.size(); ind++) {
                    String word = words.get(ind);
                    if (word.toLowerCase(Locale.ROOT).contains(charSequence.toString().toLowerCase(Locale.ROOT)))
                        filteredWords.add(word);
                }
                updateWords(filteredWords);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void updateWords(ArrayList<String> words) {
        WordsAdapter wordsAdapter = new WordsAdapter(getApplicationContext(), words);
        wordsListView.setAdapter(wordsAdapter);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}