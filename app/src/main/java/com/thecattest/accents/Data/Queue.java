package com.thecattest.accents.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class Queue {
    public String hash = "";
    public LinkedList<String> tasks = new LinkedList<>();
    public HashMap<String, Integer> mistakes = new HashMap<>();

    public String next() {
        return tasks.getFirst();
    }

    public void saveAnswer(String task, boolean madeMistake) {
        Object mistakesObj = mistakes.get(task);
        int mistakesCount = mistakesObj == null ? 0 : (int) mistakesObj;
        mistakesCount = mistakesCount + (madeMistake ? 2 : -1);
        int newWordIndex = (int)(Math.random() * 25) + 5;
        if (mistakesCount <= 0) {
            mistakes.remove(task);
            newWordIndex = tasks.size() - newWordIndex;
        } else {
            mistakes.put(task, mistakesCount);
        }
        tasks.remove(task);
        tasks.add(newWordIndex, task);
    }

    public Queue sync(Category category) {
        if (!category.getHash().equals(hash)) {
            Queue newQueue = new Queue();
            newQueue.hash = category.hash;
            LinkedList<String> newTasks = new LinkedList<>(category.getTasks());
            for (int i = 0; i < tasks.size(); i++) {
                String oldTask = tasks.removeLast();
                if (newTasks.contains(oldTask)) {
                    newQueue.tasks.add(0, oldTask);
                    newTasks.remove(oldTask);
                    if (mistakes.containsKey(oldTask))
                        newQueue.mistakes.put(oldTask, mistakes.get(oldTask));
                }
            }
            Collections.reverse(newTasks);
            for (String newTask : newTasks)
                newQueue.tasks.add(0, newTask);
            return newQueue;
        }
        return this;
    }
}
