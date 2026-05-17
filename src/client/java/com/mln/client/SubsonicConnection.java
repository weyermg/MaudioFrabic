package com.mln.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.mln.client.MaudioClient.MOD_ID;

public class SubsonicConnection {
    private String baseUrl;
    private String username;
    private String password;

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public SubsonicConnection(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<SearchResult> search(String query) {
        try {
            String requestUrl = baseUrl + "/rest/search3.view?u=" + username + "&p=" + password
                    + "&v=1.16.1&c=maudio&f=json&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            String response = getSubsonicJSON(requestUrl);

            JSONObject json = new JSONObject(response);
            JSONObject sresponse = json.getJSONObject("subsonic-response");
            if(sresponse.has("error")) {
                LOGGER.error("Subsonic error: " + sresponse.getJSONObject("error").getString("message"));
                return new ArrayList<>();
            }
            List<SearchResult> results = new ArrayList<>();
            if (sresponse.has("searchResult3")) {
                JSONObject searchResult3 = sresponse.getJSONObject("searchResult3");
                if (searchResult3.has("artist")) {
                    JSONArray artists = searchResult3.getJSONArray("artist");
                    for (int i = 0; i < artists.length(); i++) {
                        JSONObject artist = artists.getJSONObject(i);
                        results.add(new SearchResult(artist.getString("id"), artist.getString("name"), MaudioType.ARTIST));
                    }
                } if (searchResult3.has("album")) {
                    JSONArray albums = searchResult3.getJSONArray("album");
                    for (int i = 0; i < albums.length(); i++) {
                        JSONObject album = albums.getJSONObject(i);
                        results.add(new SearchResult(album.getString("id"), album.getString("name"), MaudioType.ALBUM));
                    }
                } if (searchResult3.has("song")) {
                    JSONArray songs = searchResult3.getJSONArray("song");
                    for (int i = 0; i < songs.length(); i++) {
                        JSONObject song = songs.getJSONObject(i);
                        results.add(new SearchResult(song.getString("id"), song.getString("title"), MaudioType.SONG));
                    }
                }
            }
            return results;
        } catch (Exception e) {
            System.out.println("Failed to fetch search results: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public List<SearchResult> getArtists() {
        try {
            String requestUrl = baseUrl + "/rest/getArtists.view?u=" + username + "&p=" + password
                    + "&v=1.16.1&c=maudio&f=json";
            String response = getSubsonicJSON(requestUrl);


            //Parse the JSON response and create MusicFolder objects
            JSONObject json = new JSONObject(response);
            JSONObject sresponse = json.getJSONObject("subsonic-response");
            if(sresponse.has("error")) {
                LOGGER.error("Subsonic error: " + sresponse.getJSONObject("error").getString("message"));
                return new ArrayList<>();
            }
            JSONArray folders = json.getJSONObject("subsonic-response").getJSONObject("artists").getJSONArray("index");
            List<SearchResult> musicFolders = new ArrayList<>();
            for (int i = 0; i < folders.length(); i++) {
                JSONArray artistFolders = folders.getJSONObject(i).getJSONArray("artist");
                for (int j = 0; j < artistFolders.length(); j++) {
                    JSONObject folder = artistFolders.getJSONObject(j);
                    SearchResult musicFolder = new SearchResult(folder.getString("id"), folder.getString("name"), MaudioType.ARTIST);
                    musicFolders.add(musicFolder);
                }
            }

            return musicFolders;
        } catch (Exception e) {
            System.out.println("Failed to fetch artists: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public void handleError(JSONObject error) {
        LOGGER.error("Subsonic error: " + error.getString("message"));
        System.out.println("Subsonic error " + error.getInt("code") + ": " + error.getString("message"));

    }

    public String cleanSubsonicJSON(String response) {
        // Remove everything before the first '{' character to get the JSON response
        response = response.substring(response.indexOf("{"));

        // Remove everything after the last '}' character to get the JSON response
        response = response.substring(0, response.lastIndexOf("}") + 1);

        // Strip newlines and tabs from the response
        response = response.replaceAll("[\\n\\t]", "");

        return response;
    }

    public String getSubsonicJSON(String requestUrl) throws Exception {
        String response = HttpUtils.sendGetRequest(requestUrl);

        response = cleanSubsonicJSON(response);

        LOGGER.info("Subsonic response: " + response);
        return response;
    }

    public String postSubsonicJSON(String requestUrl, String payload) throws Exception {
        String response = HttpUtils.sendPostRequest(requestUrl, payload);

        response = cleanSubsonicJSON(response);

        LOGGER.info("Subsonic response: " + response);
        return response;
    }

    public Song getSong(String id) {
        try {
            String requestUrl = baseUrl + "/rest/getSong.view?u=" + username + "&p=" + password
                    + "&v=1.16.1&c=maudio&f=json&id=" + java.net.URLEncoder.encode(id, "UTF-8");
            String response = getSubsonicJSON(requestUrl);

            JSONObject json = new JSONObject(response);
            JSONObject sresponse = json.getJSONObject("subsonic-response");
            if(sresponse.has("error")) {
                LOGGER.error("Subsonic error: " + sresponse.getJSONObject("error").getString("message"));
                return null;
            }
            if (sresponse.has("song")) {
                JSONObject songJson = sresponse.getJSONObject("song");
                String songId = songJson.optString("id");
                String parent = songJson.optString("parent");
                boolean isDir = songJson.optBoolean("isDir", false);
                String title = songJson.optString("title");
                String artist = songJson.optString("artist");
                String coverArt = songJson.optString("coverArt");
                
                String genre = null;
                if (songJson.has("genres")) {
                    JSONArray genresArray = songJson.getJSONArray("genres");
                    List<String> genreList = new ArrayList<>();
                    for (int j = 0; j < genresArray.length(); j++) {
                        genreList.add(genresArray.getJSONObject(j).getString("name"));
                    }
                    genre = String.join(", ", genreList);
                } else if (songJson.has("genre")) {
                    genre = songJson.optString("genre");
                }

                return new Song(songId, parent, isDir, title, artist, coverArt, genre);
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch song: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public InputStream getStream(String id) throws Exception {
        String streamUrl = baseUrl + "/rest/stream.view?u=" + username + "&p=" + password
                + "&v=1.16.1&c=maudio&id=" + java.net.URLEncoder.encode(id, "UTF-8") + "&format=ogg";
        LOGGER.info("Stream URL for song " + id + ": " + streamUrl);
        return HttpUtils.getBinaryStream(streamUrl);
    }

    // TODO: implement this
    public List<Song> getSongs(SearchResult folder) {
        return new ArrayList<>();
    }
}
