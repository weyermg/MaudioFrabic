package com.mln.client;
public class SearchResult {
    private String id;
    private String name;
    private MaudioType type;
    private String genre;

    public SearchResult(String id, String name, MaudioType type) {
        this(id, name, type, null);
    }

    public SearchResult(String id, String name, MaudioType type, String genre) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.genre = genre;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGenre() {
        return genre;
    }

    public String toString() {
        if (genre != null && !genre.isEmpty()) {
            return String.format("§e%s: §b%s §8(ID: %s) §d[%s]", type, name, id, genre);
        } else {
            return String.format("§e%s: §b%s §8(ID: %s)", type, name, id);
        }
    }
}