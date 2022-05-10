package com.thecattest.accents.Activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.thecattest.accents.Data.Category;
import com.thecattest.accents.Data.Dictionary;
import com.thecattest.accents.Managers.JSONManager;
import com.thecattest.accents.Managers.TasksManager;
import com.thecattest.accents.R;
import com.thecattest.accents.WordsAdapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class WordsListActivity extends AppCompatActivity {

    private ListView wordsListView;
    private ArrayList<String> words;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words_list);

        MaterialToolbar toolbar = findViewById(R.id.top_app_bar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        wordsListView = findViewById(R.id.wordsListView);

        JSONManager jsonManager = new JSONManager(this);
        Dictionary dictionary;
        try {
            dictionary = jsonManager.readObjectFromFile(Dictionary.FILENAME, new Dictionary());
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "dictionary not found", Toast.LENGTH_SHORT).show();
            dictionary = jsonManager.gson.fromJson(
                    jsonManager.filesManager.readFromRawResource(R.raw.dictionary),
                    Dictionary.class);
        } catch (IOException e) {
            Toast.makeText(this, "dictionary io exception", Toast.LENGTH_SHORT).show();
            dictionary = jsonManager.gson.fromJson(
                    jsonManager.filesManager.readFromRawResource(R.raw.dictionary),
                    Dictionary.class);
        }
        String categoryTitle = getIntent().getStringExtra(MainActivity.CATEGORY_TITLE);
        if (categoryTitle.isEmpty())
            categoryTitle = dictionary.getCategoriesTitles().get(0);
        Category category = dictionary.getCategory(categoryTitle);
        words = TasksManager.getDictionaryRepresentation(category);
        Collections.sort(words, (s1, s2) -> s1.toLowerCase(Locale.ROOT).compareTo(s2.toLowerCase(Locale.ROOT)));
        updateWords(words);

        TextInputEditText searchBar = findViewById(R.id.wordsSearch);
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