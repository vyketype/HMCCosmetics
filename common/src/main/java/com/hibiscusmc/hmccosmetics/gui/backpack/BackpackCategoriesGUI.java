package com.hibiscusmc.hmccosmetics.gui.backpack;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.hibiscusmc.hmccosmetics.util.ItemBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class BackpackCategoriesGUI {
    public static void open(UUID uuid, boolean guestViewer) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        
        ChestGui gui = new ChestGui(6, "\uD83D\uDF0F");
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        
        StaticPane pane = new StaticPane(0, 0, 9, 6, Pane.Priority.LOWEST);
        
        for (Category category : Category.values()) {
            ItemStack itemStack = new ItemBuilder(Material.PAPER)
                    .setDisplayName("§" + category.getColor() + category.getName())
                    .build();
            GuiItem item = new GuiItem(itemStack);
            item.setAction(event -> BackpackGUI.open(uuid, category, guestViewer));
            for (int slot : category.getSlots()) {
                pane.addItem(item, Slot.fromIndex(slot));
            }
        }
        
        gui.addPane(pane);
        gui.show(player);
    }
    
    @Getter
    @AllArgsConstructor
    public enum Category {
        HATS('a', new int[] {0, 1, 2, 9, 10, 11}),
        OFFHAND('c', new int[] {3, 4, 5, 12, 13, 14}),
        BACKWEAR('b', new int[] {6, 7, 8, 15, 16, 17}),
        EMOTES('6', new int[] {18, 19, 20, 27, 28, 29}),
        BUDDIES('5', new int[] {21, 22, 23, 30, 31, 32}),
        TOOLS('3', new int[] {24, 25, 26, 33, 34, 35}),
        MISC('9', new int[] {36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53});
        
        private final char color;
        private final int[] slots;
        
        public String getName() {
            return StringUtils.capitalize(name().toLowerCase());
        }
    }
}
