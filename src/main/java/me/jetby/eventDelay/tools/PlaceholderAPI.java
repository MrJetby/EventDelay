package me.jetby.eventDelay.tools;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.jetby.eventDelay.Main;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.jetby.eventDelay.manager.Assistants.*;
import static me.jetby.eventDelay.tools.FormatTimer.stringFormat;

public class PlaceholderAPI extends PlaceholderExpansion {

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return Main.INSTANCE.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "eventdelay";
    }

    @Override
    public @NotNull String getVersion() {
        return Main.INSTANCE.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {


        if (identifier.equalsIgnoreCase("now")) {
            return Main.INSTANCE.getEventDelayAPI().getNowEvent();
        }


        if (identifier.equalsIgnoreCase("next")) {
            return Main.INSTANCE.getEventDelayAPI().getNextEvent();
        }
        if (identifier.equalsIgnoreCase("previous")) {
            return Main.INSTANCE.getEventDelayAPI().getPreviousEvent();
        }
        if (identifier.equalsIgnoreCase("previous_prefix")) {
            return getPreviousEventPrefix();
        }


        if (identifier.equalsIgnoreCase("time_to_start")) {
            return String.valueOf(Main.INSTANCE.getEventDelayAPI().getTimerUntilNextEvent());
        }
        if (identifier.equalsIgnoreCase("time_to_start_string")) {
            return stringFormat(Main.INSTANCE.getEventDelayAPI().getTimerUntilNextEvent());
        }


        if (identifier.equalsIgnoreCase("duration")) {
            return String.valueOf(Main.INSTANCE.getEventDelayAPI().getOpeningTimer());
        }
        if (identifier.equalsIgnoreCase("duration_string")) {
            return stringFormat(Main.INSTANCE.getEventDelayAPI().getOpeningTimer());
        }


        if (identifier.equalsIgnoreCase("prefix")) {
            return getNowEventPrefix();
        }
        if (identifier.equalsIgnoreCase("prefix_next")||identifier.equalsIgnoreCase("next_prefix")) {
            return getNextEventPrefix();
        }



        return null;
    }

}
