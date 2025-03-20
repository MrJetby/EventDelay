package me.jetby.eventDelay.Manager;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

import static me.jetby.eventDelay.Main.*;
import static me.jetby.eventDelay.Main.freeze;
import static me.jetby.eventDelay.Manager.Assistants.*;
import static me.jetby.eventDelay.Manager.Triggers.triggerEvent;

public class Timer {



    public static void startTimer() {

        TimerUntilNextEvent = timer;

        BukkitRunnable task = new BukkitRunnable() {

            @Override
            public void run() {
                if (TimerUntilNextEvent > 0) {

                    if (freeze) {
                        if (Bukkit.getOnlinePlayers().size() >= minPlayers) {
                            TimerUntilNextEvent--;
                        }
                    } else {
                        TimerUntilNextEvent--;
                    }

                    if (!getNextEvent().equalsIgnoreCase("none")) {
                        List<Integer> warnTimes = cfg.getIntegerList("Events." + getNextEvent() + ".warns.time");
                        List<String> warnMessages = cfg.getStringList("Events." + getNextEvent() + ".warns.msg");

                        if (warnTimes.contains(TimerUntilNextEvent)) {
                            for (String msg : warnMessages) {
                                String warnMessage = msg
                                        .replace('&', '§')
                                        .replace("{prefix}", getActiveNextEventPrefix())
                                        .replace("{time_to_start}", String.valueOf(TimerUntilNextEvent));
                                Bukkit.broadcastMessage(warnMessage);
                            }
                        }
                    }

                } else {
                        if (getNextEvent().equalsIgnoreCase("none")) {
                            setNextEvent(getRandomEvent());
                            db.set("NextEvent", getNextEvent());
                            getINSTANCE().cfgSave();
                        }

                        nowEvent = getNextEvent();
                        setNextEvent("none");
                        db.set("NowEvent", nowEvent);
                        db.set("NextEvent", "none");
                        db.set("OpeningTime", "false");
                        getINSTANCE().cfgSave();

                        triggerEvent(getNowEvent());

                        TimerUntilNextEvent = timer;

                    }
                }

        };


        task.runTaskTimer(getINSTANCE(), 0, 20);
    }

    public static void startDuration(String eventName, int duration) {
        TimeUntilDuration = duration;

        BukkitRunnable task = new BukkitRunnable() {

            @Override
            public void run() {
                if (TimeUntilDuration > 0) {

                    TimeUntilDuration--;
                } else {
                    List<String> commands = db.getStringList("Events." + eventName + ".end-commands");

                    for (String command : commands) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    }
                    setNowEvent("none");
                    db.set("NowEvent", "none");
                    getINSTANCE().cfgSave();
                    cancel();
                    }

                }
        };task.runTaskTimer(getINSTANCE(), 0, 20);  // Таймер на каждую секунду
    }


    public static void Activate() {
        OpeningTimer = cfg.getInt("Events." + getNowEvent() + ".OpeningTime", 120);  // Устанавливаем начальное время для ивента
        db.set("OpeningTime", "true");

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (OpeningTimer > 0) {

                    OpeningTimer--;
                } else {

                    db.set("OpeningTime", "opened");
                    getINSTANCE().dbSave();
                    cancel();
                }
            }
        };
        task.runTaskTimer(getINSTANCE(), 0, 20);  // Таймер на каждую секунду


    }


}
