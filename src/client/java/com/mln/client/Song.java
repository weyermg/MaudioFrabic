package com.mln.client;
public class Song {
    private String id;
    private String parent;
    private boolean isDir;
    private String title;
    private String artist;
    private String coverArt;
    private String genre;

    public Song(String id, String parent, boolean isDir, String title, String artist, String coverArt, String genre) {
        this.id = id;
        this.parent = parent;
        this.isDir = isDir;
        this.title = title;
        this.artist = artist;
        this.coverArt = coverArt;
        this.genre = genre;
    }

    public String getId() {
        return id;
    }

    public String getParent() {
        return parent;
    }

    public boolean isDir() {
        return isDir;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getCoverArt() {
        return coverArt;
    }

    public String getGenre() {
        return genre;
    }

    @Override
    public String toString() {
        if (genre != null && !genre.isEmpty()) {
            return "§b" + title + " §7by §a" + artist + " §8(ID: " + id + ") §d[" + genre + "]";
        } else {
            return "§b" + title + " §7by §a" + artist + " §8(ID: " + id + ")";
        }
    }
}
