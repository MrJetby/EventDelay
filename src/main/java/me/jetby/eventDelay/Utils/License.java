package me.jetby.eventDelay.Utils;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static me.jetby.eventDelay.Main.*;

public class License {



    public static String getExternalIP() {
        try {
            URL url = new URL("http://checkip.amazonaws.com/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            Scanner scanner = new Scanner(url.openStream());
            String ip = scanner.nextLine(); // Получаем IP-адрес
            scanner.close();
            return ip;
        } catch (Exception e) {
            e.printStackTrace();
            return "0.0.0.0"; // Возвращаем запасной IP-адрес в случае ошибки
        }
    }


    public static boolean checkLicense() {
        try {
            // Создание JSON-запроса
            Map<String, String> payload = new HashMap<>();
            payload.put("license_key", LICENSE_KEY);
            payload.put("server_ip", SERVER_IP); // Получаем IP сервера
            payload.put("port", String.valueOf(getServerPort())); // Получаем порт сервера

            Gson gson = new Gson();
            String jsonInputString = gson.toJson(payload);

            // Отправка POST-запроса
            URL url = new URL(API_URL + "/check_license");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // Отправка JSON в запросе
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Чтение ответа
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                // Проверка ответа
                return response.toString().contains("\"valid\":true");
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Метод для получения порта сервера
    private static int getServerPort() {
        return getINSTANCE().getServer().getPort(); // Предположим, что метод getPort() возвращает текущий порт сервера
    }
}
