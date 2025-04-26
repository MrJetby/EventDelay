package me.jetby.eventDelay.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class LicenseCMD implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        //
        // /tlc find <license>
        //  - Если найдено то у игрока появляется буттон в чате (кнопка) если кликнет то всё загрузится.
        //
        // /tlc update <license>
        //
        // /tlc key <license>
        //
        if (args[0].equalsIgnoreCase("set")) {

            return true;
        }
        if (args[0].equalsIgnoreCase("update")) {

            return true;
        }

        return false;
    }
}
