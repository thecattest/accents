package com.thecattest.accents;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> words;
    private LinearLayout wordPlaceholder;
    private TextView commentPlaceholder;
    private ConstraintLayout root;

    private final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JSONResourceReader reader = new JSONResourceReader(getResources(), R.raw.accents);
        words = reader.constructUsingGson();

        wordPlaceholder = findViewById(R.id.wordPlaceholder);
        commentPlaceholder = findViewById(R.id.extra);
        root = findViewById(R.id.root);

        next();
    }

    private void next() {
        commentPlaceholder.setText("");
        wordPlaceholder.removeAllViews();
        String word = words.get((int)(Math.random() * words.size()));
        if (word.contains("(")) {
            String[] parts = word.split("\\(");
            word = parts[0];
            String comment = parts[1];
            comment = comment.substring(0, comment.length() - 1);
            commentPlaceholder.setText(comment);
        }
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            wordPlaceholder.addView(createTextView(c), layoutParams);
        }
    }

    private TextView createTextView(char c) {
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


}