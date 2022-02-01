package com.thecattest.accents;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;

import android.content.res.Resources;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


/**
 * An object for reading from a JSON resource file and constructing an object from that resource file using Gson.
 */
public class JSONResourceReader {

    // === [ Private Data Members ] ============================================

    // Our JSON, in string form.
    private final String jsonString;
    private static final String LOGTAG = JSONResourceReader.class.getSimpleName();

    // === [ Public API ] ======================================================

    /**
     * Read from a resources file and create a {@link JSONResourceReader} object that will allow the creation of other
     * objects from this resource.
     *
     * @param resources An application {@link Resources} object.
     * @param id The id for the resource to load, typically held in the raw/ folder.
     */
    public JSONResourceReader(Resources resources, int id) {
        InputStream resourceReader = resources.openRawResource(id);
        Writer writer = new StringWriter();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceReader, StandardCharsets.UTF_8));
            String line = reader.readLine();
            while (line != null) {
                writer.write(line);
                line = reader.readLine();
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Unhandled exception while using JSONResourceReader", e);
        } finally {
            try {
                resourceReader.close();
            } catch (Exception e) {
                Log.e(LOGTAG, "Unhandled exception while using JSONResourceReader", e);
            }
        }

        jsonString = writer.toString();
    }

    /**
     * Build an object from the specified JSON resource using Gson.
     *
     * @return An object of type T, with member fields populated using Gson.
     */
    public ArrayList<String> constructUsingGson() {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonString, new TypeToken<ArrayList<String>>(){}.getType());
    }
}