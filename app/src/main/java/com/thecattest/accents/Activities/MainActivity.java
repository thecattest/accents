package com.thecattest.accents.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.thecattest.accents.Managers.FilesManager;
import com.thecattest.accents.Managers.WordsManager;
import com.thecattest.accents.R;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String SHARED_PREF_KEY = "ACCENTS_SHARED_PREF";
    public static final String THEME = "ACCENTS_SHARED_PREF_THEME";
    public static final int THEME_DARK = 1;
    public static final int THEME_LIGHT = 2;
    public static final String TASK_TYPE = "TASK_TYPE";

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
        TabLayout taskTypeNavigation = findViewById(R.id.taskTypeNavigation);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);

        findViewById(R.id.author).setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.author_url)));
            startActivity(browserIntent);
        });
        findViewById(R.id.dictionary).setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.dictionary_url)));
            startActivity(browserIntent);
        });

        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
        taskTypeNavigation.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    wordsManager.setTaskType(WordsManager.TASK_TYPE_ACCENTS);
                } else if (tab.getPosition() == 1) {
                    wordsManager.setTaskType(WordsManager.TASK_TYPE_ENDINGS);
                }
                next();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
        int theme = sharedPref.getInt(THEME, THEME_LIGHT);
        if(theme == THEME_DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            toolbar.getMenu().getItem(2).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_light, null));
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            toolbar.getMenu().getItem(2).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_dark, null));
        }

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
        int taskType = wordsManager.getTaskType();
        if (taskType == WordsManager.TASK_TYPE_ACCENTS) {
            String comment = "";
            String word = fullWord;
            if (fullWord.contains("(")) {
                String[] parts = fullWord.split("\\(");
                word = parts[0];
                comment = parts[1];
                comment = comment.substring(0, comment.length() - 1);
            }
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                String lower = String.valueOf(c).toLowerCase(Locale.ROOT);
                boolean clickable = "аеиоуэюяы".contains(lower);
                boolean correct = Character.isUpperCase(c) && clickable;
                wordPlaceholder.addView(createTextView(lower, fullWord, clickable, correct), layoutParams);
            }
            commentPlaceholder.setText(comment);
        } else if (taskType == WordsManager.TASK_TYPE_ENDINGS) {
            String[] parts = fullWord.substring(0, fullWord.length() - 1).split("\\(");
            wordPlaceholder.addView(createTextView(parts[0] + "("), layoutParams);
            String[] options = parts[1].split("/");
            for (int i = 0; i < options.length; i++) {
                wordPlaceholder.addView(createTextView(options[i], fullWord, true, i == 0), layoutParams);
                if (i != options.length - 1)
                    wordPlaceholder.addView(createTextView("/"), layoutParams);
            }
            wordPlaceholder.addView(createTextView(")"), layoutParams);
        }
    }

    private TextView createTextView(String part) {
        return createTextView(part, "", false, false);
    }

    private TextView createTextView(String part, String word, boolean clickable, boolean correct) {
        TextView textView = new TextView(MainActivity.this);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 42);
        textView.setPadding(2, 0, 2, 0);
        textView.setText(part);

        if (clickable) {
            textView.setTextColor(getResources().getColor(R.color.red));
            textView.setOnClickListener(view -> {
                //animateBackground(Character.isUpperCase(c) ? R.drawable.correct : R.drawable.incorrect);
                vibrate(correct ? 15 : 200);
                if (correct) {
                    wordsManager.updateQueue(word, madeMistake);
                    next();
                }
                else
                    madeMistake = true;
            });
        }
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
        if (itemId == R.id.theme) {
            SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putInt(THEME, THEME_LIGHT);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putInt(THEME, THEME_DARK);
            }
            editor.apply();
        }
        if (itemId == R.id.words) {
            Intent i = new Intent(MainActivity.this, WordsListActivity.class);
            i.putExtra(TASK_TYPE, wordsManager.getTaskType());
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
                URL url = new URL(wordsManager.getSyncURL());
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