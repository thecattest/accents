package com.thecattest.accents.Managers;

import android.content.Context;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thecattest.accents.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class WordsManager {

    private final FilesManager filesManager;

    public WordsManager(Context context) {
        filesManager = new FilesManager(context);
    }

    public LinkedList<Pair<String, Integer>> getWords() {
        String wordsJson = filesManager.readFromFile(FilesManager.ACCENTS_FILENAME);
        if (wordsJson.isEmpty()) {
            String wordsJsonFromResource = filesManager.readFromRawResource(R.raw.accents);
            LinkedList<Pair<String, Integer>> words = getWordsFromResourceJson(wordsJsonFromResource);
            writeWords(words);
            return words;
        }
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(wordsJson, new TypeToken<LinkedList<Pair<String, Integer>>>(){}.getType());
    }

    public LinkedList<Pair<String, Integer>> getWordsFromResourceJson(String json) {
        Gson gson = new GsonBuilder().create();
        LinkedList<String> wordsFromResource = gson.fromJson(json, new TypeToken<LinkedList<String>>(){}.getType());
        LinkedList<Pair<String, Integer>> words = new LinkedList<>();
        for (String word : wordsFromResource)
            words.add(new Pair<>(word, 0));
        Collections.shuffle(words);
        return words;
    }

    public ArrayList<String> getWordsOnly() {
        LinkedList<Pair<String, Integer>> words = getWords();
        ArrayList<String> wordsOnly = new ArrayList<>();
        for (Pair<String, Integer> pair : words)
            wordsOnly.add(pair.first);
        return wordsOnly;
    }

    public void writeWords(LinkedList<Pair<String, Integer>> words) {
        Gson gson = new GsonBuilder().create();
        filesManager.writeToFile(gson.toJson(words), FilesManager.ACCENTS_FILENAME);
    }

    public String getNextWord() {
        return getWords().getFirst().first;
    }

    public void updateQueue(String word, boolean madeMistake) {
        LinkedList<Pair<String, Integer>> words = getWords();
        int wordIndex = -1;
        for (int i = 0; i < words.size(); i++)
            if (words.get(i).first.equals(word)) {
                wordIndex = i;
                break;
            }
        if (wordIndex != -1) {
            Pair<String, Integer> wordMistakesPair = words.remove(wordIndex);
            int mistakes = wordMistakesPair.second + (madeMistake ? 2 : -1);
            int newWordIndex = (int)(Math.random() * 25) + 5;
            if (mistakes <= 0) {
                mistakes = 0;
                newWordIndex = words.size() - newWordIndex;
            }
            words.add(newWordIndex, new Pair<>(word, mistakes));
            writeWords(words);
        }
    }
}
