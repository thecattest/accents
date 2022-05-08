package com.thecattest.accents.Data;

import androidx.annotation.Nullable;

import com.thecattest.accents.Managers.JSONManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Category {

    public static final int TASK_TYPE_ACCENTS = 1;
    public static final int TASK_TYPE_ENDINGS = 2;

    public int id;
    public String hash;
    public String title;
    public int taskType;
    public ArrayList<String> tasks;

    public int getId() { return id; }

    public String getHash() { return hash; }

    public String getTitle() { return title; }

    public int getTaskType() { return taskType; }

    public ArrayList<String> getTasks() { return tasks; }

    public String getFilename() {
        return getId() + ".json";
    }

    public String next(JSONManager jsonManager) throws IOException {
        Queue queue = loadQueue(jsonManager);
        if (queue == null)
            queue = syncQueue(jsonManager);
        return queue.next();
    }

    public void saveAnswer(String task, boolean madeMistake, JSONManager jsonManager) throws IOException {
        Queue queue = loadQueue(jsonManager);
        if (queue == null)
            queue = syncQueue(jsonManager);
        queue.saveAnswer(task, madeMistake);
        saveQueue(queue, jsonManager);
    }

    @Nullable
    public Queue loadQueue(JSONManager jsonManager) {
        Queue queue;
        try {
            queue = jsonManager.readObjectFromFile(
                    getFilename(), new Queue());
        } catch (IOException e) {
            queue = null;
        }
        return queue;
    }

    public void saveQueue(Queue queue, JSONManager jsonManager) {
        jsonManager.writeObjectToFile(queue, getFilename());
    }

    public void shuffleQueue(JSONManager jsonManager) {
        Queue oldQueue = loadQueue(jsonManager);
        if (oldQueue == null)
            return;
        Queue newQueue = new Queue();
        newQueue = newQueue.sync(this);
        newQueue.mistakes = oldQueue.mistakes;
        Collections.shuffle(newQueue.tasks);
        saveQueue(newQueue, jsonManager);
    }

    public void forgetMistakes(JSONManager jsonManager) {
        Queue queue = loadQueue(jsonManager);
        if (queue == null)
            return;
        queue.mistakes = new HashMap<>();
        saveQueue(queue, jsonManager);
    }

    public Queue syncQueue(JSONManager jsonManager) {
        Queue queue = loadQueue(jsonManager);
        if (queue == null)
            queue = new Queue();
        Queue syncedQueue = queue.sync(this);
        saveQueue(syncedQueue, jsonManager);
        return syncedQueue;
    }
}
