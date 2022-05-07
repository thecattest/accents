package com.thecattest.accents.Data;

import java.util.HashMap;
import java.util.LinkedList;

public class Queue {
    public String hash;
    public LinkedList<String> tasks = new LinkedList<>();
    public HashMap<String, Integer> mistakes = new HashMap<>();

    public String next() {
        return tasks.getFirst();
    }

    public Queue sync(Category category) {
        if (!category.getHash().equals(hash)) {
            Queue newQueue = new Queue();
            LinkedList<String> newTasks = new LinkedList<>(category.getTasks());
            for (int i = 0; i < tasks.size(); i++) {
                String oldTask = tasks.removeFirst();
                if (newTasks.contains(oldTask)) {
                    newQueue.tasks.add(oldTask);
                    newTasks.removeFirst();
                    if (mistakes.containsKey(oldTask))
                        newQueue.mistakes.put(oldTask, mistakes.get(oldTask));
                }
            }
            for (String newTask : newTasks)
                newQueue.tasks.add(0, newTask);
            return newQueue;
        }
        return this;
    }
}
