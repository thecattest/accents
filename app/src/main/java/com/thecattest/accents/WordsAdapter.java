package com.thecattest.accents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class WordsAdapter extends ArrayAdapter<String> {

    public WordsAdapter(Context context, ArrayList<String> words) {
        super(context, android.R.layout.simple_list_item_1, words);
    }

//    @NonNull
//    @Override
//    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
//        final String word = getItem(position);
//        if (convertView == null) {
//            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
//        }
//
//        ((TextView) convertView.findViewById(android.R.id.text1)).setText(word);
//
//        return convertView;
//    }

}
