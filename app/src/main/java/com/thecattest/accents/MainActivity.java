package com.thecattest.accents;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> words;
    private LinearLayout wordPlaceholder;
    private TextView commentPlaceholder;
    private ConstraintLayout root;

    @SuppressWarnings("FieldCanBeLocal")
    private final String ACCENTS_FILENAME = "accents.json";
    private final String MISTAKES_FILENAME = "mistakes.json";

    private final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String wordsJson = readFromFile(ACCENTS_FILENAME);
        if (wordsJson.isEmpty()) {
            Toast.makeText(this, "Loaded from resources", Toast.LENGTH_SHORT).show();
            wordsJson = readFromRawResource(R.raw.accents);
            writeToFile(wordsJson, ACCENTS_FILENAME);
        }
        Gson gson = new GsonBuilder().create();
        words = gson.fromJson(wordsJson, new TypeToken<ArrayList<String>>(){}.getType());

        wordPlaceholder = findViewById(R.id.wordPlaceholder);
        commentPlaceholder = findViewById(R.id.extra);
        root = findViewById(R.id.root);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);

        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        next();
    }

    private void next() {
        commentPlaceholder.setText("");
        wordPlaceholder.removeAllViews();

        String fullWord = getNextWord();
        String comment = "";
        String word = fullWord;
        if (fullWord.contains("(")) {
            String[] parts = fullWord.split("\\(");
            word = parts[0];
            comment = parts[1];
            comment = comment.substring(0, comment.length() - 1);
        }
        for (int i = 0; i < word.length(); i++)
            wordPlaceholder.addView(createTextView(fullWord, word.charAt(i)), layoutParams);
        commentPlaceholder.setText(comment);
    }

    private HashMap<String, Integer> getMistakes() {
        String jsonString = readFromFile(MISTAKES_FILENAME);
        if (jsonString == null || jsonString.isEmpty())
            return new HashMap<>();
        Gson gson = new GsonBuilder().create();
        HashMap<String, Integer> mistakes = gson.fromJson(
                jsonString, new TypeToken<HashMap<String, Integer>>(){}.getType());
        Log.d("MISTAKES READ", mistakes.toString());
        return mistakes;
    }

    private void setMistakes(HashMap<String, Integer> mistakes) {
        Log.d("MISTAKES WRITE", mistakes.toString());
        Gson gson = new Gson();
        String jsonString = gson.toJson(mistakes);
        writeToFile(jsonString, MISTAKES_FILENAME);
    }

    private void mistakeInc(String mistake) {
        HashMap<String, Integer> mistakes = getMistakes();
        Integer count = mistakes.get(mistake);
        count = count != null ? count : 2;
        mistakes.put(mistake, count+1);
        setMistakes(mistakes);
    }

    private void mistakeDec(String mistake) {
        HashMap<String, Integer> mistakes = getMistakes();
        Integer count = mistakes.get(mistake);
        count = count != null ? count : 0;
        if (count > 1)
            mistakes.put(mistake, count-1);
        else
            mistakes.remove(mistake);
        setMistakes(mistakes);
    }

    private String getNextWord() {
        HashMap<String, Integer> mistakes = getMistakes();
        if (mistakes.size() != 0 && Math.random() < .1) {
            Object[] mistakesKeys = mistakes.keySet().toArray();
            String mistake = String.valueOf(mistakesKeys[(int)(Math.random() * mistakesKeys.length)]);
            mistakeDec(mistake);
            return mistake;
        }
        return words.get((int)(Math.random() * words.size()));
    }

    private TextView createTextView(String word, char c) {
        TextView textView = new TextView(MainActivity.this);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
        textView.setPadding(2, 0, 2, 0);
        textView.setText(String.valueOf(c).toLowerCase(Locale.ROOT));

        if ("аеиоуэюяы".contains(String.valueOf(c).toLowerCase(Locale.ROOT))) {
            textView.setTextColor(getResources().getColor(R.color.red));
            textView.setOnClickListener(view -> {
                animateBackground(Character.isUpperCase(c) ? R.drawable.correct : R.drawable.incorrect);
                vibrate(Character.isUpperCase(c) ? 15 : 200);
                if (Character.isUpperCase(c))
                    next();
                else
                    mistakeInc(word);
            });
        } else
            textView.setTextColor(getResources().getColor(R.color.white));

        return textView;
    }

    private void animateBackground(int colorId) {
        TransitionDrawable transition = (TransitionDrawable) getResources().getDrawable(colorId);
        root.setBackground(transition);
        transition.startTransition(400);
    }

    private void vibrate(int vibrationDuration) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(vibrationDuration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(vibrationDuration);
        }
    }

    private boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.refresh) {
            new DownloadFileFromURL().execute();
            return true;
        }
        if (itemId == R.id.forget) {
            setMistakes(new HashMap<>());
            Toast.makeText(this, "Ошибки забыты", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void writeToFile(String data, String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("writer", "File write failed: " + e);
        }
    }

    private String readFromRawResource(int resourceId) {
        try {
            InputStream inputStream = getResources().openRawResource(resourceId);
            return readFromFile(inputStream);
        } catch (FileNotFoundException e) {
            Log.e("reader", "File not found: " + e);
        } catch (IOException e) {
            Log.e("reader", "Can not read file: " + e);
        }
        return "";
    }

    private String readFromFile(String fileName) {
        try {
            InputStream inputStream = openFileInput(fileName);
            return readFromFile(inputStream);
        } catch (FileNotFoundException e) {
            Log.e("reader", "File not found: " + e);
        } catch (IOException e) {
            Log.e("reader", "Can not read file: " + e);
        }
        return "";
    }

    private String readFromFile(InputStream inputStream) throws IOException {
        String ret = "{}";

        if ( inputStream != null ) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ( (receiveString = bufferedReader.readLine()) != null ) {
                stringBuilder.append(receiveString);
            }

            inputStream.close();
            ret = stringBuilder.toString();
        }

        return ret;
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            int count;
            try {
                URL url = new URL("https://raw.githubusercontent.com/thecattest/accents/master/app/src/main/res/raw/accents.json");
                URLConnection connection = url.openConnection();
                connection.connect();

                InputStream input = new BufferedInputStream(url.openStream());

                OutputStream output = openFileOutput(ACCENTS_FILENAME, Context.MODE_PRIVATE);

                byte[] data = new byte[1024];

                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                output.flush();

                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(MainActivity.this, "Синхронизировано", Toast.LENGTH_SHORT).show();
            next();
        }
    }

}