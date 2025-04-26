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
    private final String server;
    private final Main plugin;
    // This MUST be the same as the REQUEST_KEY defined in config.php
    private final String requestKey = "vmLAyzmppLLDgvqMPFyHLSkWdyHYqRImNueC1OLK";
    private boolean debug = CFG().getBoolean("debug", false);

    private boolean valid = false;
    private ReturnType returnType;
    @Getter
    private String generatedBy;
    @Getter
    private String licensedTo;
    @Getter
    private String generatedIn;



    public License(String license, String server, Main plugin) {
        this.license = license;
        this.server = server;
        this.plugin = plugin;
    }

    public void debug() {
        debug = true;
    }

    public void request() {
        try {
            URL url = new URL(server + "/request.php");
            URLConnection connection = url.openConnection();
            if (debug) System.out.println("[DEBUG] Попытка подключится к сайту: " + server);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            
            connection.setRequestProperty("License-Key", license);
            connection.setRequestProperty("Plugin-Name", plugin.getDescription().getName());
            connection.setRequestProperty("Request-Key", requestKey);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            if (debug) System.out.println("[DEBUG] Reading response");
            if (debug) System.out.println("[DEBUG] Converting to string");
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

                if (debug) System.out.println("[DEBUG] FAILED WITH RESULT: " + returnType);
            }
        } catch (Exception ex) {
            if (debug) {
                ex.printStackTrace();
            }
        }
    }

    public boolean isValid() {
        if (valid) {



        }
        return valid;
    }

    public ReturnType getReturn() {
        return returnType;
    }

    public enum ReturnType {
        LICENSE_NOT_FOUND, PLUGIN_NAME_NOT_FOUND, REQUEST_KEY_NOT_FOUND, INVALID_REQUEST_KEY, INVALID_LICENSE, TOO_MANY_IPS, VALID;
    }

}
