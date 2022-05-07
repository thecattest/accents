package com.thecattest.accents.Managers;

import android.util.TypedValue;
import android.widget.TextView;

import com.thecattest.accents.Activities.MainActivity;
import com.thecattest.accents.Data.Category;
import com.thecattest.accents.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

public class TasksManager {

    public static String getComment(String task, int taskType) {
        String comment = "";
        if (taskType == Category.TASK_TYPE_ACCENTS) {
            if (task.contains("(")) {
                comment = task.split("\\(")[1];
                comment = comment.substring(0, comment.length() - 1);
            }
        }
        return comment;
    }

    public static ArrayList<TextView> getTextViews(MainActivity mainActivity,
                                                   String task, int taskType) {
        ArrayList<TextView> textViews = new ArrayList<>();
        if (taskType == Category.TASK_TYPE_ACCENTS) {
            String word = task;
            if (task.contains("("))
                word = task.split("\\(")[0];
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                String lower = String.valueOf(c).toLowerCase(Locale.ROOT);
                boolean clickable = "аеиоуэюяы".contains(lower);
                boolean correct = Character.isUpperCase(c) && clickable;
                textViews.add(
                        createTextView(mainActivity, lower,
                                task, clickable, correct)
                );
            }
        } else if (taskType == Category.TASK_TYPE_ENDINGS) {
            String[] parts = task.substring(0, task.length() - 1).split("\\(");
            textViews.add(createTextView(mainActivity, parts[0] + "("));
            String[] optionsArray = parts[1].split("/");
            ArrayList<String> options = new ArrayList<>(Arrays.asList(optionsArray));
            Collections.shuffle(options);
            for (int i = 0; i < options.size(); i++) {
                String word = options.get(i);
                textViews.add(
                        createTextView(mainActivity, word, task, true,
                                optionsArray[0].equals(word))
                );
                if (i != options.size() - 1)
                    textViews.add(createTextView(mainActivity, "/"));
            }
            textViews.add(createTextView(mainActivity, ")"));
        }
        return textViews;
    }

    private static TextView createTextView(MainActivity mainActivity, String part) {
        return createTextView(mainActivity, part, null, false, false);
    }

    private static TextView createTextView(MainActivity mainActivity, String part, String task,
                                    boolean clickable, boolean correct) {
        TextView textView = new TextView(mainActivity);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 42);
        textView.setPadding(2, 0, 2, 0);
        textView.setText(part);

        if (clickable) {
            textView.setTextColor(mainActivity.getResources().getColor(R.color.red));
            textView.setOnClickListener(mainActivity.getTextViewClickListener(task, correct));
        }
        return textView;
    }

    public static ArrayList<String> getDictionaryRepresentation(Category category) {
        ArrayList<String> tasks = category.getTasks();
        ArrayList<String> dictionaryRepresentation = new ArrayList<>();
        for (String task : tasks) {
            if (category.getTaskType() == Category.TASK_TYPE_ACCENTS)
                dictionaryRepresentation.add(task);
            else if (category.getTaskType() == Category.TASK_TYPE_ENDINGS) {
                String[] parts = task.split("\\(");
                String end = parts[1].split("/")[0];
                dictionaryRepresentation.add(parts[0] + end);
            }
        }
        return dictionaryRepresentation;
    }
}
