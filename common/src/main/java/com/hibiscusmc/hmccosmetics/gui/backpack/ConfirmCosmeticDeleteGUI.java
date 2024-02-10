package com.hibiscusmc.hmccosmetics.gui.backpack;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticData;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ConfirmCosmeticDeleteGUI {
    public static void open(UUID uuid, CosmeticData data) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        
        ChestGui gui = new ChestGui(4, "Confirm Deletion");
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        
        StaticPane pane = new StaticPane(0, 0, 9, 4, Pane.Priority.LOWEST);
        
        GuiItem confirmButton = new GuiItem(getConfirmButton());
        confirmButton.setAction(event -> {
            CosmeticUser user = CosmeticUsers.getUser(uuid);
            user.removeCosmetic(data);
            player.closeInventory();
            // todo send chat message
        });
        pane.addItem(confirmButton, 1, 4);
        
        GuiItem closeBarrier = new GuiItem(getCloseBarrier());
        closeBarrier.setAction(event -> player.closeInventory());
        pane.addItem(closeBarrier, 3, 4);
        
        gui.addPane(pane);
        gui.show(player);
    }
    
    public static ItemStack getCloseBarrier() {
        return new ItemBuilder(new ItemStack(Material.BARRIER))
                .setDisplayName("§cClose")
                .build();
    }
    
    public static ItemStack getConfirmButton() {
        return new ItemBuilder(new ItemStack(Material.RED_TERRACOTTA))
                .setDisplayName("§c§lDelete")
                .addLoreLine("")
                .addLoreLine("§cThis action cannot be reversed!")
                .addLoreLine("")
                .addLoreLine("§eClick to delete.")
                .build();
    }
}
