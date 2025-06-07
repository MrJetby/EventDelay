package me.jetby.eventDelay.tools;

import lombok.Getter;
import me.jetby.eventDelay.Main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static me.jetby.eventDelay.configurations.Config.CFG;

public class License {

    @Getter
    private final String license;
    private final Main plugin;
    private final boolean debug = CFG().getBoolean("debug", false);

    @Getter
    private boolean valid = false;
    private ReturnType returnType;
    @Getter
    private String generatedBy;
    @Getter
    private String licensedTo;
    @Getter
    private String generatedIn;


    public License(String license, Main plugin) {
        this.license = license;
        this.plugin = plugin;
    }

    public String getRaw(String string) {
        try {
            URL url = new URL(string);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder builder = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                builder.append(inputLine);
            }
            in.close();
            return builder.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void request() {
        try {

            String serverUrl = "https://pastebin.com/raw/W33iCnTM";
            String server = getRaw(serverUrl);

            String keyUrl = "https://pastebin.com/raw/SYHyarp0";
            String requestKey = getRaw(keyUrl);

            URL url = new URL(server);
            URLConnection connection = url.openConnection();
            if (debug) System.out.println("[DEBUG] Попытка подключится к сайту: " + server);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

            connection.setRequestProperty("License-Key", license);
            connection.setRequestProperty("Plugin-Name", plugin.getDescription().getName());
            connection.setRequestProperty("Request-Key", requestKey);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            if (debug) System.out.println("[DEBUG] Чтение ответа");
            if (debug) System.out.println("[DEBUG] Преобразование в строку");
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            String response = builder.toString();
            if (debug) System.out.println("[DEBUG] Конвертировано");

            String[] responseSplited = response.split(";");
            if (responseSplited[0].equals("VALID")) {
                if (debug) System.out.println("[DEBUG] ЛИЦЕНЗИЯ ДЕЙСТВИТЕЛЬНА");
                valid = true;
                returnType = ReturnType.valueOf(responseSplited[0]);

                generatedBy = responseSplited[2];
                generatedIn = responseSplited[3];
                licensedTo = responseSplited[1];
            } else {
                if (debug) System.out.println("[DEBUG] ЛИЦЕНЗИЯ НЕ ДЕЙСТВИТЕЛЬНА");
                valid = false;
                returnType = ReturnType.valueOf(responseSplited[0]);

                if (debug) System.out.println("[DEBUG] НЕУДАЧНО С РЕЗУЛЬТАТОМ: " + returnType);
            }
        } catch (Exception ex) {
            if (debug) {
                ex.printStackTrace();
            }
        }
    }

    public String getReturn() {
        if (returnType == null) {
            return "Подключение было безуспешным.";
        }
        if (returnType.equals(ReturnType.VALID)) {
            return "Лицензия действительна!";
        }
        if (returnType.equals(ReturnType.INVALID_LICENSE)) {
            return "Лицензия не существует!";
        }
        if (returnType.equals(ReturnType.LICENSE_NOT_FOUND)) {
            return "Лицензия не найдена!";
        }
        if (returnType.equals(ReturnType.PLUGIN_NAME_NOT_FOUND)) {
            return "Такого плагина не существует!";
        }
        if (returnType.equals(ReturnType.TOO_MANY_IPS)) {
            return "Лимит IP с этой лицензией превышен!";
        }
        return returnType.toString();
    }
    public enum ReturnType {
        LICENSE_NOT_FOUND, PLUGIN_NAME_NOT_FOUND, REQUEST_KEY_NOT_FOUND, INVALID_REQUEST_KEY, INVALID_LICENSE, TOO_MANY_IPS, VALID;
    }

}
