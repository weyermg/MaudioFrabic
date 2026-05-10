package com.mln.client;
public class SearchResult {
    private String id;
    private String name;
    private MaudioType type;

    public SearchResult(String id, String name, MaudioType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return String.format("%s: %s (id: %s)", type, name, id);
    }
}