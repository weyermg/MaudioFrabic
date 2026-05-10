package com.mln.client;

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

    public String getSubsonicJSON(String requestUrl) throws Exception {
        String response = HttpUtils.sendGetRequest(requestUrl);

        // Remove everything before the first '{' character to get the JSON response
        response = response.substring(response.indexOf("{"));

        // Remove everything after the last '}' character to get the JSON response
        response = response.substring(0, response.lastIndexOf("}") + 1);

        // Strip newlines and tabs from the response
        response = response.replaceAll("[\\n\\t]", "");

        LOGGER.info("Subsonic response: " + response);
        return response;
    }

    // TODO: implement this
    public List<Song> getSongs(SearchResult folder) {
        return new ArrayList<>();
    }
}
