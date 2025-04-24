package me.jetby.eventDelay.tools;

public class EventDelayAPI {
    private static boolean freeze;
    private static int timer;
    private static int minPlayers;
    private static String previousEvent;
    private static String nowEvent;
    private static String nextEvent;
    private static String ActivationStatus;
    private static int timerUntilNextEvent;
    private static int duration;
    private static int timeUntilDuration;
    private static int openingTimer;

    public static boolean isFreeze() { return freeze; }
    public static void setFreeze(boolean freeze) { EventDelayAPI.freeze = freeze; }

    public static int getTimer() { return timer; }
    public static void setTimer(int timer) { EventDelayAPI.timer = timer; }

    public static int getMinPlayers() { return minPlayers; }
    public static void setMinPlayers(int minPlayers) { EventDelayAPI.minPlayers = minPlayers; }

    public static String getNowEvent() { return nowEvent; }
    public static void setNowEvent(String nowEvent) { EventDelayAPI.nowEvent = nowEvent; }

    public static String getNextEvent() { return nextEvent; }
    public static void setNextEvent(String nextEvent) { EventDelayAPI.nextEvent = nextEvent; }

    public static int getTimerUntilNextEvent() { return timerUntilNextEvent; }
    public static void setTimerUntilNextEvent(int timer) { EventDelayAPI.timerUntilNextEvent = timer; }

    public static int getDuration() { return duration; }
    public static void setDuration(int duration) { EventDelayAPI.duration = duration; }

    public static int getTimeToEnd() { return timeUntilDuration; }
    public static void setTimeToEnd(int time) { EventDelayAPI.timeUntilDuration = time; }

    public static int getTimeToOpen() { return openingTimer; }
    public static void setTimeToOpen(int openingTimer) { EventDelayAPI.openingTimer = openingTimer; }

    public static String getPreviousEvent() {return previousEvent;}
    public static void setPreviousEvent(String previousEvent) {EventDelayAPI.previousEvent = previousEvent;}

    public static String getActivationStatus() {return ActivationStatus;}
    public static void setActivationStatus(String activationStatus) {ActivationStatus = activationStatus;}
}
