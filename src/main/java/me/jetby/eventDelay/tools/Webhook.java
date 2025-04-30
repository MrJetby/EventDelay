package me.jetby.eventDelay.tools;


import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static org.bukkit.Bukkit.getLogger;

public class Webhook {


    public static void sendToDiscord(String webhookUrl, String username, String avatar, String color, String title, List<String> textList) {
        try {
            System.out.println("SexY");
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            int decimalColor = Integer.parseInt(color.replace("#", ""), 16);

            StringBuilder descriptionBuilder = new StringBuilder();
            for (String text : textList) {
                descriptionBuilder.append(text).append("\\n"); // экранируем перенос
            }

            String json = """
                {
                  "username": "%s",
                  "avatar_url": "%s",
                  "embeds": [{
                    "title": "%s",
                    "description": "%s",
                    "color": %d
                  }]
                }
                """.formatted(username, avatar, title, descriptionBuilder.toString(), decimalColor);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(json.getBytes());
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 204) {
                getLogger().warning("Ошибка отправки webhook: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
