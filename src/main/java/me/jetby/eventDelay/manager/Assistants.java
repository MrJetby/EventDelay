package me.jetby.eventDelay.manager;


import me.jetby.eventDelay.tools.EventDelayAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static me.jetby.eventDelay.configurations.Config.CFG;

public class Assistants {





    public static String getRandomEvent() {
        List<String> events = CFG().getConfigurationSection("Events").getKeys(false).stream().toList();
        Map<String, Integer> eventChances = new HashMap<>();
        int totalWeight = 0;

        for (String event : events) {
            int chance = CFG().getInt("Events." + event + ".Chance", 100);
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
        return !EventDelayAPI.getNowEvent().equalsIgnoreCase("none");
    }

    public static String getPreviousEventPrefix() {
        if (!EventDelayAPI.getPreviousEvent().equalsIgnoreCase("none")) {
            return CFG().getString("Events." + EventDelayAPI.getPreviousEvent() + ".Prefix");
        }
        return "none";
    }
    public static String getNowEventPrefix() {
        if (!EventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
            return CFG().getString("Events." + EventDelayAPI.getNowEvent() + ".Prefix");
        }
        return "none";
    }
    public static String getNextEventPrefix() {
        if (!EventDelayAPI.getNextEvent().equalsIgnoreCase("none")) {
            return CFG().getString("Events." + EventDelayAPI.getNextEvent() + ".Prefix");
        }
        return "none";
    }


    public static boolean isCompass() {
        if (!EventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
            return CFG().getBoolean("Events." + EventDelayAPI.getNowEvent() + ".compass", false);
        }
        return false;
    }


}
