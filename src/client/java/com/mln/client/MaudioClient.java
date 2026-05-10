package com.mln.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;

public class MaudioClient implements ClientModInitializer {
	public static final String MOD_ID = "maudio";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ServerConfig config;
	public static SubsonicConnection connection;

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as
		// rendering.
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("subsonic_url").executes(context -> {
				context.getSource().sendFeedback(Component.literal(config.serverUrl));
				return 1;
			}));
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("get_artists").executes(context -> {
				List<MusicFolder> folders = connection.getArtists();
				for (MusicFolder folder : folders) {
					context.getSource().sendFeedback(Component.literal(folder.getName()));
				}
				return 1;
			}));
		});
		LOGGER.info("Maudio Client Initialized!");
		LOGGER.info("Working directory: " + System.getProperty("user.dir"));

		try {
			config = parseConfig(System.getProperty("user.dir") + "/maudio.yaml");
			LOGGER.info("Subsonic URL: " + config.serverUrl);

			connection = new SubsonicConnection(config.serverUrl, config.username, config.password);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public static class ServerConfig {
        public String serverUrl;
        public String username;
        public String password;
    }


	public static ServerConfig parseConfig(String filePath) throws IOException {
		ServerConfig config = new ServerConfig();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#"))
					continue;

				int colonIndex = line.indexOf(':');
				if (colonIndex > 0) {
					String key = line.substring(0, colonIndex).trim();
					String value = line.substring(colonIndex + 1).trim();

					if ((value.startsWith("\"") && value.endsWith("\""))
							|| (value.startsWith("'") && value.endsWith("'"))) {
						value = value.substring(1, value.length() - 1);
					}

					switch (key) {
						case "server_url":
						case "serverUrl":
						case "url":
							config.serverUrl = value;
							break;
						case "username":
						case "user":
							config.username = value;
							break;
						case "password":
						case "pass":
							config.password = value;
							break;
					}
				}
			}
		}
		return config;
	}
}