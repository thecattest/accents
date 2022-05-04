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

    public static final int TASK_TYPE_ACCENTS = 1;
    public static final int TASK_TYPE_ENDINGS = 2;

    private final FilesManager filesManager;
    private int taskType = TASK_TYPE_ACCENTS;

    public WordsManager(Context context) {
        filesManager = new FilesManager(context);
    }

    public void setTaskType(int newTaskType) {
        if (newTaskType == TASK_TYPE_ACCENTS)
            taskType = TASK_TYPE_ACCENTS;
        else if (newTaskType == TASK_TYPE_ENDINGS)
            taskType = TASK_TYPE_ENDINGS;
        else
            throw new IllegalArgumentException("Wrong task type");
    }

    public int getTaskType() {
        return taskType;
    }

    private String getFilename() {
        if (taskType == TASK_TYPE_ACCENTS)
            return FilesManager.ACCENTS_FILENAME;
        else
            return FilesManager.ENDINGS_FILENAME;
    }

    public String getSyncURL() {
        if (taskType == TASK_TYPE_ACCENTS)
            return FilesManager.ACCENTS_URL;
        else
            return FilesManager.ENDINGS_URL;
    }

    private int getRawId() {
        if (taskType == TASK_TYPE_ACCENTS)
            return R.raw.accents;
        else
            return R.raw.endings;
    }

    public LinkedList<Pair<String, Integer>> getWords() {
        String wordsJson = filesManager.readFromFile(getFilename());
        if (wordsJson.isEmpty()) {
            String wordsJsonFromResource = filesManager.readFromRawResource(getRawId());
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
        for (Pair<String, Integer> pair : words) {
            if (taskType == TASK_TYPE_ACCENTS)
                wordsOnly.add(pair.first);
            else {
                String[] parts = pair.first.split("\\(");
                String end = parts[1].split("/")[0];
                wordsOnly.add(parts[0] + end);
            }
        }
        return wordsOnly;
    }

    public void writeWords(LinkedList<Pair<String, Integer>> words) {
        Gson gson = new GsonBuilder().create();
        filesManager.writeToFile(gson.toJson(words), getFilename());
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
