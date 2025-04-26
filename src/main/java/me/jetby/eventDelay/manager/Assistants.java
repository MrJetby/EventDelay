package me.jetby.eventDelay.manager;


import me.jetby.eventDelay.Main;
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
        return !Main.INSTANCE.getEventDelayAPI().getNowEvent().equalsIgnoreCase("none");
    }

    public static String getPreviousEventPrefix() {
        if (!Main.INSTANCE.getEventDelayAPI().getPreviousEvent().equalsIgnoreCase("none")) {
            return CFG().getString("Events." + Main.INSTANCE.getEventDelayAPI().getPreviousEvent() + ".Prefix");
        }
        return "none";
    }
    public static String getNowEventPrefix() {
        if (!Main.INSTANCE.getEventDelayAPI().getNowEvent().equalsIgnoreCase("none")) {
            return CFG().getString("Events." + Main.INSTANCE.getEventDelayAPI().getNowEvent() + ".Prefix");
        }
        return "none";
    }
    public static String getNextEventPrefix() {
        if (!Main.INSTANCE.getEventDelayAPI().getNextEvent().equalsIgnoreCase("none")) {
            return CFG().getString("Events." + Main.INSTANCE.getEventDelayAPI().getNextEvent() + ".Prefix");
        }
        return "none";
    }


    public static boolean isCompass() {
        if (!Main.INSTANCE.getEventDelayAPI().getNowEvent().equalsIgnoreCase("none")) {
            return CFG().getBoolean("Events." + Main.INSTANCE.getEventDelayAPI().getNowEvent() + ".compass", false);
        }
        return false;
    }


}
