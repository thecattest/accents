package com.thecattest.accents.Managers;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;

public class JSONManager {
    public final Gson gson;
    public final FilesManager filesManager;

    public JSONManager(Context context) {
        gson = new GsonBuilder().create();
        filesManager = new FilesManager(context);
    }

    public <T> void writeObjectToFile(T object, String filename) {
        filesManager.writeToFile(gson.toJson(object), filename);
    }

    public <T> T readObjectFromFile(String filename, T objectClass) throws FileNotFoundException, IOException {
        String json = filesManager.readFromFile(filename);
        return gson.fromJson(json, (Type) objectClass.getClass());
    }

}
