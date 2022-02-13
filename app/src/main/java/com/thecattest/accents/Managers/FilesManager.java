package com.thecattest.accents.Managers;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FilesManager {
    private final Context context;

    @SuppressWarnings("FieldCanBeLocal")
    public static final String ACCENTS_FILENAME = "accents.json";
    public static final String MISTAKES_FILENAME = "mistakes.json";

    public FilesManager(Context context){
        this.context = context;
    }

    public void writeToFile(String data, String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("writer", "File write failed: " + e);
        }
    }

    public String readFromRawResource(int resourceId) {
        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            return readFromFile(inputStream);
        } catch (FileNotFoundException e) {
            Log.e("reader", "File not found: " + e);
        } catch (IOException e) {
            Log.e("reader", "Can not read file: " + e);
        }
        return "";
    }

    public String readFromFile(String fileName) {
        try {
            InputStream inputStream = context.openFileInput(fileName);
            return readFromFile(inputStream);
        } catch (FileNotFoundException e) {
            Log.e("reader", "File not found: " + e);
        } catch (IOException e) {
            Log.e("reader", "Can not read file: " + e);
        }
        return "";
    }

    public String readFromFile(InputStream inputStream) throws IOException {
        String ret = "{}";

        if ( inputStream != null ) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ( (receiveString = bufferedReader.readLine()) != null ) {
                stringBuilder.append(receiveString);
            }

            inputStream.close();
            ret = stringBuilder.toString();
        }

        return ret;
    }
}
