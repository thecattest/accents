package com.thecattest.accents.Data;

import com.thecattest.accents.Managers.JSONManager;

import java.io.IOException;
import java.util.ArrayList;

public class Dictionary {

    public static final String FILENAME = "dictionary.json";
    public static final String SYNC_URL = "https://raw.githubusercontent.com/thecattest/accents/master/app/src/main/res/raw/accents.json";

    public ArrayList<Category> categories;

    public Category getCategory(String title) {
        for (Category category : categories)
            if (category.getTitle().equals(title))
                return category;
        throw new StringIndexOutOfBoundsException("no category with such name");
    }

    public ArrayList<String> getCategoriesTitles() {
        ArrayList<String> titles = new ArrayList<>();
        for (Category category : categories)
            titles.add(category.getTitle());
        return titles;
    }

    public void sync(JSONManager jsonManager) throws IOException {
        for (Category category : categories)
            category.syncQueue(jsonManager);
    }
}
