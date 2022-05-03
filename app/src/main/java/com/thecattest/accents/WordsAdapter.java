package com.thecattest.accents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;

public class WordsAdapter extends ArrayAdapter<String> {

    public WordsAdapter(Context context, ArrayList<String> words) {
        super(context, R.layout.list_item, words);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final String word = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.text1)).setText(word);
        int textColor = MaterialColors.getColor(
                parent,
                com.google.android.material.R.attr.colorOnSecondary,
                getContext().getResources().getColor(R.color.light_gray));
        ((TextView) convertView.findViewById(R.id.text1)).setTextColor(textColor);

        return convertView;
    }

}
