package com.hibiscusmc.hmccosmetics.command;

import com.hibiscusmc.hmccosmetics.gui.backpack.BackpackCategoriesGUI;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BackpackCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("backpack")) {
            return true;
        }
        
        if (!(sender instanceof Player)) {
            return true;
        }
        
        if (args.length == 0) {
            BackpackCategoriesGUI.open(((Player) sender).getUniqueId(), false);
            return true;
        }
        
        String playerName = args[0];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        
        if (!offlinePlayer.hasPlayedBefore()) {
            MessagesUtil.sendMessage(sender, "invalid-player");
            return true;
        }
        
        BackpackCategoriesGUI.open(offlinePlayer.getUniqueId(), true);
        return true;
    }
}
