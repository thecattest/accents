package com.thecattest.accents.Data;

import com.thecattest.accents.Managers.JSONManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

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
        return queue.next();
    }

    public void saveAnswer(String task, boolean madeMistake, JSONManager jsonManager) throws IOException {
        Queue queue = loadQueue(jsonManager);
        Object mistakesObj = queue.mistakes.get(task);
        int mistakes = mistakesObj == null ? 0 : (int) mistakesObj;
        mistakes = mistakes + (madeMistake ? 2 : -1);
        int newWordIndex = (int)(Math.random() * 25) + 5;
        if (mistakes <= 0) {
            queue.mistakes.remove(task);
            newWordIndex = queue.tasks.size() - newWordIndex;
        } else {
            queue.mistakes.put(task, mistakes);
        }
        queue.tasks.remove(task);
        queue.tasks.add(newWordIndex, task);
        saveQueue(queue, jsonManager);
    }

    public Queue loadQueue(JSONManager jsonManager) throws IOException {
        Queue queue;
        try {
            queue = jsonManager.readObjectFromFile(
                    getFilename(), new Queue());
            queue = syncQueue(queue, jsonManager);
        } catch (FileNotFoundException e) {
            queue = new Queue().sync(this);
            saveQueue(queue, jsonManager);
        }
        return queue;
    }

    public void saveQueue(Queue queue, JSONManager jsonManager) {
        jsonManager.writeObjectToFile(queue, getFilename());
    }

    public Queue syncQueue(Queue queue, JSONManager jsonManager) {
        if (queue.hash.isEmpty() || !queue.hash.equals(hash)) {
            Queue newQueue = queue.sync(this);
            saveQueue(newQueue, jsonManager);
            return newQueue;
        }
        return queue;
    }

    public void syncQueue(JSONManager jsonManager) throws IOException {
        Queue queue = loadQueue(jsonManager);
        syncQueue(queue, jsonManager);
    }
}
