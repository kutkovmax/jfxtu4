package ru.kutkovmax.jfxtu4.model;

import java.util.HashMap;
import java.util.Map;

public class StateTable {
    private final Map<String, Integer> nameToId = new HashMap<>();
    private final Map<Integer, String> idToName = new HashMap<>();
    private int nextId = 0;

    public int getId(String name) {
        return nameToId.computeIfAbsent(name, k -> {
            int id = nextId++;
            idToName.put(id, name);
            return id;
        });
    }

    public String getName(int id) {
        String name = idToName.get(id);
        if (name == null) {
            throw new IllegalStateException("state id " + id + " not found");
        }
        return name;
    }
}