package me.jetby.eventDelay.Configs;


import java.util.List;

import static me.jetby.eventDelay.Main.messages;

public class Messages {

    private static Messages MESSAGES;

    private List<String> time;
    private List<String> active;
    private List<String> noPlayers;
    private List<String> info;
    private String noItem;
    private String disabled;
    private String success;
    private String none;
    private String start;
    private String end;
    private String noPerm;
    private String reload;
    private List<String> usage;

    private Messages() {
        load();
    }
    public static Messages get() {
        if (MESSAGES == null) {
            MESSAGES = new Messages();
        }
        return MESSAGES;
    }
    public void load() {
        time = messages.getStringList("delay.time");
        active = messages.getStringList("delay.active");
        noPlayers = messages.getStringList("delay.noPlayers");
        info = messages.getStringList("delay.info");
        noItem = messages.getString("compass.noItem");
        disabled = messages.getString("compass.disabled");
        success = messages.getString("compass.success");
        none = messages.getString("OpeningTime.none");
        start = messages.getString("OpeningTime.start");
        end = messages.getString("OpeningTime.end");
        noPerm = messages.getString("messages.noPerm");
        reload = messages.getString("messages.reload");
        usage = messages.getStringList("messages.usage");

        for (String key : new String[]{"time", "active", "noPlayers", "info", "noItem", "disabled", "success", "none", "start", "end", "noPerm", "reload", "usage"}) {
            switch (key) {
                case "time":
                    if (time == null) {
                        time.add("&6[⌚] &fДо ивента осталось &a{time_to_start_string}.");
                    }
                    break;
                case "active":
                    if (active == null) {
                        active.add("&6[⌚] &fДо ивента осталось &a{time_to_start} секунд.");
                        active.add("&fСейчас на сервере проводится ивент {prefix}. &7(Подробнее: /event info)");
                    }
                    break;
                case "noPlayers":
                    if (noPlayers == null) {
                        noPlayers.add("&6[⌚] &cНеобходимо от {min_players} игроков для ивентов!");
                    }
                    break;
                case "info":
                    if (info == null) {
                        info.set(1, "&c[✖] &fСейчас нету активных ивентов!");
                    }
                    break;
                case "noItem":
                    if (noItem == null) {
                        noItem = ("&c[✘] &fДля этой функции на руках должен быть компас.");
                    }
                    break;
                case "disabled":
                    if (disabled == null) {
                        disabled = ("&c[✘] &fКомпас отключён для этого ивента.");
                    }
                    break;
                case "success":
                    if (success == null) {
                        success = ("&a[✔] &fТеперь компас будет направлять на ивент.");
                    }
                    break;
                case "none":
                    if (none == null) {
                        none = ("&fЖдёт активации.");
                    }
                    break;
                case "start":
                    if (start == null) {
                        start =("&fАктивация. До открытия &6{time_to_open}&f сек.");
                    }
                    break;
                case "end":
                    if (end == null) {
                        end = ("&aДоступ открыт.");
                    }
                    break;
                case "noPerm":
                    if (noPerm == null) {
                        noPerm = ("&cНедостаточно прав!");
                    }
                    break;
                case "reload":
                    if (reload == null) {
                        reload = ("&aУспешная перезагрузка!");
                    }
                    break;
                case "usage":
                    if (usage == null) {
                        usage.add("&e/event delay &7- &fВремя до ивента");
                        usage.add("&e/event info &7- &fИнформация об ивенте");
                    }
                    break;
            }
        }
    }

    public List<String> Time() { return time; }
    public List<String> Active() { return active; }
    public List<String> noPlayers() { return noPlayers; }
    public List<String> Info() { return info; }
    public List<String> Usage() { return usage; }
    public String End() { return end; }
    public String NoItem() { return noItem; }
    public String None() { return none; }
    public String NoPerm() { return noPerm; }
    public String Reload() { return reload; }
    public String Start() { return start; }
    public String Success() { return success; }
    public String Disabled() { return disabled; }
}

