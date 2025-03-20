package me.jetby.eventDelay.Manager;


import me.jetby.eventDelay.Main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static me.jetby.eventDelay.Main.*;

public class Assistants {


    public static String getNowEvent() {
        if (nowEvent==null){
            return "none";
        } else {
            return nowEvent;
        }
    }
    public static void setNowEvent(String nowEvent) {
        Main.nowEvent = nowEvent;
    }

    public static String getNextEvent() {
        return nextEvent;
    }

    public static int getTime() {
        return TimerUntilNextEvent;
    }

    public static String getRandomEvent() {
        List<String> events = cfg.getConfigurationSection("Events").getKeys(false).stream().toList();
        Map<String, Integer> eventChances = new HashMap<>();
        int totalWeight = 0;

        for (String event : events) {
            int chance = cfg.getInt("Events." + event + ".Chance", 100);
            eventChances.put(event, chance);
            totalWeight += chance;
        }


        int randomValue = new Random().nextInt(totalWeight) + 1;



        int currentWeight = 0;
        for (Map.Entry<String, Integer> entry : eventChances.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue <= currentWeight) {
                return entry.getKey();
            }
        }

        return events.get(new Random().nextInt(events.size()));
    }


    public static boolean isEventActive() {
        return !nowEvent.equalsIgnoreCase("none");
    }

    public static String getActiveEventPrefix() {
        if (!nowEvent.equalsIgnoreCase("none")) {
            return cfg.getString("Events." + nowEvent + ".Prefix");
        }
        return "none";
    }
    public static String getActiveNextEventPrefix() {
        if (!nextEvent.equalsIgnoreCase("none")) {
            return cfg.getString("Events." + nextEvent + ".Prefix");
        }
        return "none";
    }

    public static void setNextEvent(String eventName) {
        nextEvent = eventName;
    }

    public static int getTimeToOpen() {
        return OpeningTimer;
    }

    public static int getTimeToEnd() {
        return TimeUntilDuration;
    }



}
