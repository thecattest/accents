package com.thecattest.accents.Data;

import com.thecattest.accents.Managers.JSONManager;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Category {

    public static final int TASK_TYPE_ACCENTS = 1;
    public static final int TASK_TYPE_ENDINGS = 2;

    private int id;
    private String hash;
    private String title;
    private int taskType;
    private ArrayList<String> tasks;

    private JSONManager jsonManager = null;

    public int getId() { return id; }

    public String getHash() { return hash; }

    public String getTitle() { return title; }

    public int getTaskType() { return taskType; }

    public ArrayList<String> getTasks() { return tasks; }

    public void setJsonManager(JSONManager jsonManager) {
        this.jsonManager = jsonManager;
    }

    public String getFilename() {
        return getId() + ".json";
    }

    public String next() {
        return loadQueue().next();
    }

    public void saveAnswer(String task, boolean madeMistake) {
        Queue queue = loadQueue();
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
        saveQueue(queue);
    }

    public Queue loadQueue() {
        if (jsonManager == null)
            throw new NullPointerException("set json manager to continue");
        Queue queue;
        try {
            queue = jsonManager.readObjectFromFile(
                    getFilename(), new Queue());
        } catch (FileNotFoundException e) {
            queue = new Queue();
            queue.sync(this);
            saveQueue(queue);
        }
        return queue;
    }

    public void saveQueue(Queue queue) {
        if (jsonManager == null)
            throw new NullPointerException("set json manager to continue");
        jsonManager.writeObjectToFile(queue, getFilename());
    }

    public void syncQueue() {
        Queue queue = loadQueue();
        Queue newQueue = queue.sync(this);
        saveQueue(newQueue);
    }
}
