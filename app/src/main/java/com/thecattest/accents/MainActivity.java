package com.thecattest.accents;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> words;
    private LinearLayout wordPlaceholder;
    private ConstraintLayout root;

    private final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JSONResourceReader reader = new JSONResourceReader(getResources(), R.raw.accents);
        words = reader.constructUsingGson();
        wordPlaceholder = findViewById(R.id.wordPlaceholder);
        root = findViewById(R.id.root);

        next();
    }

    private void next() {
        String word = words.get((int)(Math.random() * words.size()));
        if (word.contains("(")) {
            String[] split;
            split = word.split("\\(");
            word = split[0];
            String extra = split[1];
        }
        wordPlaceholder.removeAllViews();
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
            textView.setOnClickListener(v -> {
                TransitionDrawable transition = (TransitionDrawable) getResources().getDrawable(Character.isUpperCase(c) ? R.drawable.correct : R.drawable.incorrect);
                root.setBackground(transition);
                transition.startTransition(400);
                next();
            });
        } else
            textView.setTextColor(getResources().getColor(R.color.white));

        return textView;
    }


}