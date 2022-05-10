package com.thecattest.accents.Data;

import com.thecattest.accents.Managers.JSONManager;

import java.io.IOException;
import java.util.ArrayList;

public class Dictionary {

    public static final String FILENAME = "dictionary.json";
    public static final String BASE_URL = "https://raw.githubusercontent.com";
    public static final String SYNC_URL = "/thecattest/accents/dictionary/dictionary3.json";

    public ArrayList<Category> categories;
    public String version = "";

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
        jsonManager.writeObjectToFile(this, Dictionary.FILENAME);
        for (Category category : categories) {
            if (category.isSupported())
                category.syncQueue(jsonManager);
        }
    }
}
