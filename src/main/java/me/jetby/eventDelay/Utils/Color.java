package me.jetby.eventDelay.Utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class Color {

    public static String replace(String text, Player player) {
        text = text.replace('&', '§');
        text = PlaceholderAPI.setPlaceholders(player, text);
        return text;
    }

}
