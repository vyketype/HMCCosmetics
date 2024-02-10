package com.hibiscusmc.hmccosmetics.gui.backpack;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.panes.BackpackPane;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackpackGUI {
    private static final Map<UUID, Integer> PAGE_VIEWING = new HashMap<>();
    
    public static void open(UUID uuid, BackpackCategoriesGUI.Category category, boolean guestViewer) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        CosmeticUser user = CosmeticUsers.getUser(uuid);
        
        int maxPage = (int) Math.ceil(user.getCosmeticsMap().size() / 35.0);
        
        PAGE_VIEWING.put(uuid, 1);
        
        ChestGui gui = new ChestGui(6, "Backpack - " + category.getName());
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        
        StaticPane background = new StaticPane(0, 0, 9, 6, Pane.Priority.LOWEST);
        background.fillWith(Menu.getBlackPane());
        
        BackpackPane backpackPane = new BackpackPane(1, 0, 7, 5);
        backpackPane.loadPage(user, category, PAGE_VIEWING.get(uuid));
        
        StaticPane bottomPane = new StaticPane(0, 5, 9, 1);
        
        GuiItem previousPage = new GuiItem(getPreviousPageArrow());
        previousPage.setAction(event -> {
            int currentPage = PAGE_VIEWING.get(uuid);
            PAGE_VIEWING.put(uuid, currentPage - 1);
            backpackPane.loadPage(user, category, PAGE_VIEWING.get(uuid));
            gui.update();
        });
        
        GuiItem nextPage = new GuiItem(getNextPageArrow());
        nextPage.setAction(event -> {
            int currentPage = PAGE_VIEWING.get(uuid);
            PAGE_VIEWING.put(uuid, currentPage + 1);
            backpackPane.loadPage(user, category, PAGE_VIEWING.get(uuid));
            gui.update();
        });
        
        bottomPane = addArrows(bottomPane, uuid, previousPage, nextPage); // todo resolve this
        
        GuiItem closeBarrier = new GuiItem(getCloseBarrier());
        closeBarrier.setAction(event -> player.closeInventory());
        bottomPane.addItem(closeBarrier, 4, 0);
        
        // todo sorting button
        
        gui.addPane(backpackPane);
        gui.addPane(backpackPane);
        gui.addPane(bottomPane);
        
        gui.show(player);
    }
    
    private static StaticPane addArrows(StaticPane bottomPane, UUID uuid, GuiItem previous, GuiItem next) {
        CosmeticUser user = CosmeticUsers.getUser(uuid);
        int maxPage = (int) Math.ceil(user.getCosmeticsMap().size() / 35.0);
        
        bottomPane.addItem(new GuiItem(Menu.getBlackPane()), 1, 0);
        bottomPane.addItem(new GuiItem(Menu.getBlackPane()), 7, 0);
        if (PAGE_VIEWING.get(uuid) > 1) {
            bottomPane.addItem(previous, 1, 0);
        }
        if (PAGE_VIEWING.get(uuid) == maxPage) {
            bottomPane.addItem(next, 7, 0);
        }
        
        return bottomPane;
    }
    
    public static ItemStack getNextPageArrow() {
        return new ItemBuilder(new ItemStack(Material.ARROW))
                .setDisplayName("§aNext Page")
                .build();
    }
    
    public static ItemStack getPreviousPageArrow() {
        return new ItemBuilder(new ItemStack(Material.ARROW))
                .setDisplayName("§aPrevious Page")
                .build();
    }
    
    public static ItemStack getCloseBarrier() {
        return new ItemBuilder(new ItemStack(Material.BARRIER))
                .setDisplayName("§cClose")
                .build();
    }
}
