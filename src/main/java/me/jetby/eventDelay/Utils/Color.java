package me.jetby.eventDelay.Utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class Color {

    public static String replace(String text, Player player) {
        text = PlaceholderAPI.setPlaceholders(player, text);
        text = text.replace('&', '§');
        return text;
    }

}
