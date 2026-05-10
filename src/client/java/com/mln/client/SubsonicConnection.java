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

    // TODO: implement this
    public List<MusicFolder> getArtists() {
        try {
            String requestUrl = baseUrl + "/rest/getArtists.view?u=" + username + "&p=" + password
                    + "&v=1.16.1&c=maudio&f=json";
            String response = HttpUtils.sendGetRequest(requestUrl);

            // Remove everything before the first '{' character to get the JSON response
            response = response.substring(response.indexOf("{"));

            // Remove everything after the last '}' character to get the JSON response
            response = response.substring(0, response.lastIndexOf("}") + 1);

            // Strip newlines and tabs from the response
            response = response.replaceAll("[\\n\\t]", "");

            LOGGER.info("Subsonic response: " + response);


            //Parse the JSON response and create MusicFolder objects
            JSONObject json = new JSONObject(response);
            JSONObject sresponse = json.getJSONObject("subsonic-response");
            if(sresponse.has("error")) {
                LOGGER.error("Subsonic error: " + sresponse.getJSONObject("error").getString("message"));
                return new ArrayList<>();
            }
            JSONArray folders = json.getJSONObject("subsonic-response").getJSONObject("artists").getJSONArray("index");
            List<MusicFolder> musicFolders = new ArrayList<>();
            for (int i = 0; i < folders.length(); i++) {
                JSONArray artistFolders = folders.getJSONObject(i).getJSONArray("artist");
                for (int j = 0; j < artistFolders.length(); j++) {
                    JSONObject folder = artistFolders.getJSONObject(j);
                    MusicFolder musicFolder = new MusicFolder(folder.getString("id"), folder.getString("name"));
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

    // TODO: implement this
    public List<Song> getSongs(MusicFolder folder) {
        return new ArrayList<>();
    }
}
