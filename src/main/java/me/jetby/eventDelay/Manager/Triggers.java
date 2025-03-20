package me.jetby.eventDelay.Manager;

import org.bukkit.Bukkit;

import java.util.List;
import java.util.Set;

import static me.jetby.eventDelay.Main.*;
import static me.jetby.eventDelay.Main.duration;
import static me.jetby.eventDelay.Manager.Assistants.getNowEvent;
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

    public static void stopEvent(String eventName) {
        if (getNowEvent().equalsIgnoreCase("none")) {
            return;
        }
        triggerStop(nowEvent);
    }

    private static void triggerStop(String eventName) {
        duration = cfg.getInt("Events." + eventName + ".Duration");
        List<String> commands = cfg.getStringList("Events." + eventName + ".end-commands");

        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
        nowEvent = "none";
        nextEvent = getRandomEvent(); // Устанавливаем следующий случайный ивент

    }
    public static void triggerEvent(String eventName) {
        duration = cfg.getInt("Events." + eventName + ".Duration");

        // Проверяем наличие секций start-commands-random
        if (cfg.contains("Events." + eventName + ".start-commands-random")) {
            // Получаем все секции
            Set<String> sections = cfg.getConfigurationSection("Events." + eventName + ".start-commands-random").getKeys(false);
            if (!sections.isEmpty()) {
                // Выбираем случайную секцию
                String randomSection = sections.stream()
                        .skip((int) (Math.random() * sections.size()))
                        .findFirst()
                        .orElse(null);

                if (randomSection != null) {
                    // Выполняем все команды из выбранной секции
                    List<String> randomCommands = cfg.getStringList("Events." + eventName + ".start-commands-random." + randomSection);
                    for (String command : randomCommands) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    }
                }
            }
        }

        // Выполняем команды из start-commands, если start-commands-random не найдено или пусто
        List<String> commands = cfg.getStringList("Events." + eventName + ".start-commands");
        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        // Запускаем обратный отсчет времени до завершения ивента
        startDuration(nowEvent, duration);
    }

}
