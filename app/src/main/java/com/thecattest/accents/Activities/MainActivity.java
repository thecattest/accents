package com.thecattest.accents.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.thecattest.accents.Managers.FilesManager;
import com.thecattest.accents.Managers.WordsManager;
import com.thecattest.accents.R;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private long lastTimeBackPressed = 0;

    private LinearLayout wordPlaceholder;
    private TextView commentPlaceholder;
    private ConstraintLayout root;
    private boolean madeMistake;

    private WordsManager wordsManager;

    private final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wordsManager = new WordsManager(getApplicationContext());

        wordPlaceholder = findViewById(R.id.wordPlaceholder);
        commentPlaceholder = findViewById(R.id.extra);
        root = findViewById(R.id.root);
        findViewById(R.id.author).setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.author_url)));
            startActivity(browserIntent);
        });
        findViewById(R.id.dictionary).setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.dictionary_url)));
            startActivity(browserIntent);
        });
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);

        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        next();
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis() / 1000L;
        if (currentTime - lastTimeBackPressed <= 3) {
            super.onBackPressed();
        } else {
            lastTimeBackPressed = currentTime;
            Toast.makeText(this, R.string.back_again_to_exit, Toast.LENGTH_SHORT).show();
        }
    }

    private void next() {
        commentPlaceholder.setText("");
        wordPlaceholder.removeAllViews();
        madeMistake = false;

        String fullWord = wordsManager.getNextWord();
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

    private TextView createTextView(String word, char c) {
        TextView textView = new TextView(MainActivity.this);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 42);
        textView.setPadding(2, 0, 2, 0);
        textView.setText(String.valueOf(c).toLowerCase(Locale.ROOT));

        if ("аеиоуэюяы".contains(String.valueOf(c).toLowerCase(Locale.ROOT))) {
            textView.setTextColor(getResources().getColor(R.color.red));
            textView.setOnClickListener(view -> {
                animateBackground(Character.isUpperCase(c) ? R.drawable.correct : R.drawable.incorrect);
                vibrate(Character.isUpperCase(c) ? 15 : 200);
                if (Character.isUpperCase(c)) {
                    wordsManager.updateQueue(word, madeMistake);
                    next();
                }
                else
                    madeMistake = true;
            });
        } else
            textView.setTextColor(getResources().getColor(R.color.white));

        return textView;
    }

    private void animateBackground(int colorId) {
        TransitionDrawable transition = (TransitionDrawable) ResourcesCompat.getDrawable(getResources(), colorId, null);
        if (transition != null) {
            root.setBackground(transition);
            transition.startTransition(200);
        }
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
//        if (itemId == R.id.forget) {
//            wordsManager.setMistakes(new HashMap<>());
//            Toast.makeText(this, "Ошибки забыты", Toast.LENGTH_SHORT).show();
//            return true;
//        }
        if (itemId == R.id.words) {
            Intent i = new Intent(MainActivity.this, WordsListActivity.class);
            startActivity(i);
            return true;
        }
        return false;
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

                ByteArrayOutputStream output = new ByteArrayOutputStream();
                // OutputStream output = openFileOutput(FilesManager.ACCENTS_FILENAME, Context.MODE_PRIVATE);

                byte[] data = new byte[1024];

                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                output.flush();
                String wordsJson = output.toString();
                wordsManager.writeWords(wordsManager.getWordsFromResourceJson(wordsJson));

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