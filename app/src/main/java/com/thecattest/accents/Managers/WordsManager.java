package com.thecattest.accents.Managers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thecattest.accents.Managers.FilesManager;
import com.thecattest.accents.R;

import java.util.ArrayList;
import java.util.HashMap;

public class WordsManager {

    private final Context context;
    private final FilesManager filesManager;

    public WordsManager(Context context) {
        this.context = context;
        filesManager = new FilesManager(context);
    }

    public ArrayList<String> readWords() {
        String wordsJson = filesManager.readFromFile(FilesManager.ACCENTS_FILENAME);
        if (wordsJson.isEmpty()) {
            Toast.makeText(context, "Loaded from resources", Toast.LENGTH_SHORT).show();
            wordsJson = filesManager.readFromRawResource(R.raw.accents);
            filesManager.writeToFile(wordsJson, FilesManager.ACCENTS_FILENAME);
        }
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(wordsJson, new TypeToken<ArrayList<String>>(){}.getType());
    }

    public HashMap<String, Integer> getMistakes() {
        String jsonString = filesManager.readFromFile(FilesManager.MISTAKES_FILENAME);
        if (jsonString == null || jsonString.isEmpty())
            return new HashMap<>();
        Gson gson = new GsonBuilder().create();
        HashMap<String, Integer> mistakes = gson.fromJson(
                jsonString, new TypeToken<HashMap<String, Integer>>(){}.getType());
        Log.d("MISTAKES READ", mistakes.toString());
        return mistakes;
    }

    public void setMistakes(HashMap<String, Integer> mistakes) {
        Log.d("MISTAKES WRITE", mistakes.toString());
        Gson gson = new Gson();
        String jsonString = gson.toJson(mistakes);
        filesManager.writeToFile(jsonString, FilesManager.MISTAKES_FILENAME);
    }

    public void mistakeInc(String mistake) {
        HashMap<String, Integer> mistakes = getMistakes();
        Integer count = mistakes.get(mistake);
        count = count != null ? count : 2;
        mistakes.put(mistake, count+1);
        setMistakes(mistakes);
    }

    public void mistakeDec(String mistake) {
        HashMap<String, Integer> mistakes = getMistakes();
        Integer count = mistakes.get(mistake);
        count = count != null ? count : 0;
        if (count > 1)
            mistakes.put(mistake, count-1);
        else
            mistakes.remove(mistake);
        setMistakes(mistakes);
    }
}
