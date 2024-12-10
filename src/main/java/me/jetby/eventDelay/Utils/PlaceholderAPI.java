package me.jetby.eventDelay.Utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import static me.jetby.eventDelay.Main.getINSTANCE;
import static me.jetby.eventDelay.Manager.Assistants.*;
import static me.jetby.eventDelay.Utils.FormatTimer.stringFormat;

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
    public String getAuthor() {
        return getINSTANCE().getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return "eventdelay";
    }

    @Override
    public String getVersion() {
        return getINSTANCE().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {


        if (identifier.equalsIgnoreCase("now")) {
            return getNowEvent();
        }


        if (identifier.equalsIgnoreCase("next")) {
            return getNextEvent();
        }


        if (identifier.equalsIgnoreCase("time_to_start")) {
            return String.valueOf(getTime());
        }
        if (identifier.equalsIgnoreCase("time_to_start_string")) {
            return stringFormat(getTime());
        }


        if (identifier.equalsIgnoreCase("duration")) {
            return String.valueOf(getTimeToEnd());
        }
        if (identifier.equalsIgnoreCase("duration_string")) {
            return stringFormat(getTimeToEnd());
        }


        if (identifier.equalsIgnoreCase("prefix")) {
            return getActiveEventPrefix();
        }
        if (identifier.equalsIgnoreCase("prefix_next")) {
            return getActiveNextEventPrefix();
        }


        return null;
    }

}
