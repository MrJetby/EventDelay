package me.jetby.eventDelay.Configs;

import static me.jetby.eventDelay.Main.cfg;
import static me.jetby.eventDelay.Main.db;

public class DB {

    private static DB DB;

    private String nextEvent;
    private String nowEvent;
    private String OpeningTime;

    private DB() {
        load();
    }

    public static DB get() {
        if (DB == null) {
            DB = new DB();
        }
        return DB;
    }

    public void load() {

        nextEvent = db.getString("NextEvent", "none");
        nowEvent = db.getString("NowEvent", "none");
        OpeningTime = db.getString("OpeningTime", "false");

    }
    public String getNextEvent() {return nextEvent;}
    public String getNowEvent() {return nowEvent;}
    public String getOpeningTime() {return OpeningTime;}

}
