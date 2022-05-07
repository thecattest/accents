package com.thecattest.accents.Data;

import java.util.ArrayList;

public class Dictionary {
    private ArrayList<Category> categories;

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

    public void sync() {
        for (Category category : categories)
            category.syncQueue();
    }
}
