package me.jetby.eventDelay.Configs;

import java.util.List;

import static me.jetby.eventDelay.Main.*;

public class Config {

    private static Config CONFIG;

    private String license;
    private int Timer;
    private boolean Freeze;
    private int MinPlayers;
    private boolean Debug;
    private int WebhookUrl;

    private Config() {
        load();
    }
    public static Config get() {
        if (CONFIG == null) {
            CONFIG = new Config();
        }
        return CONFIG;
    }
    public void load() {

        license = cfg.getString("license", "ЛИЦЕНЗИЯ");
        Timer = cfg.getInt("Timer", 1800);
        Debug = cfg.getBoolean("debug", false);
        Freeze = cfg.getBoolean("Freeze", false);
        MinPlayers = cfg.getInt("MinPlayers", 1);

    }

    public String getLicense() {return license;}
    public int getTimer() {return Timer;}
    public int getMinPlayers() {return MinPlayers;}
    public boolean getFreeze() {return Freeze;}
    public boolean getDebug() {return Debug;}

}