package me.jetby.eventDelay.Manager;

import org.bukkit.Bukkit;

import java.util.List;

import static me.jetby.eventDelay.Main.*;
import static me.jetby.eventDelay.Main.duration;
import static me.jetby.eventDelay.Manager.Assistants.getRandomEvent;
import static me.jetby.eventDelay.Manager.Timer.startDuration;

public class Triggers {
    public static void nextRandomEvent() {
        if (nextEvent.equalsIgnoreCase("none")) {
            nextEvent = getRandomEvent();
        }
    }
    // Запуск случайного ивента и установка следующего
    public static void startRandomEvent() {
        if (nextEvent.equalsIgnoreCase("none")) {
            nextEvent = getRandomEvent();
        }

        nowEvent = nextEvent;
        nextEvent = getRandomEvent(); // Устанавливаем следующий случайный ивент
        triggerEvent(nowEvent);
    }


    public static void triggerEvent(String eventName) {
        duration = cfg.getInt("Events." + eventName + ".Duration");
        List<String> commands = cfg.getStringList("Events." + eventName + ".start-commands");

        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        // Запускаем обратный отсчет времени до завершения ивента
        startDuration(nowEvent, duration);
    }
}
