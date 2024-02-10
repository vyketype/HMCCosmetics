package com.hibiscusmc.hmccosmetics.gui.panes;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticData;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticItem;
import com.hibiscusmc.hmccosmetics.gui.backpack.BackpackActionsGUI;
import com.hibiscusmc.hmccosmetics.gui.backpack.BackpackCategoriesGUI;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackpackPane extends StaticPane {
    public static final int NUMBER_OF_ITEMS = 35;
    
    @NotNull
    private final Map<Map.Entry<Integer, Integer>, GuiItem> items;
    
    public BackpackPane(int x, int y, int length, int height) {
        super(x, y, length, height);
        items = new HashMap<>(length * height);
    }
    
    public void loadPage(CosmeticUser user, BackpackCategoriesGUI.Category category, int page) {
        fillWith(getEmptyItem());
        
        Map<CosmeticData, Cosmetic> cosmeticsMap = user.getCosmeticsMap();
        int endIndex = NUMBER_OF_ITEMS * (page - 1);
        
        // Get cosmetics based on page number
        List<CosmeticData> data = Arrays.asList(cosmeticsMap.keySet().toArray(new CosmeticData[0]));
        List<CosmeticData> dataToRemove = data.subList(0, endIndex);
        cosmeticsMap.keySet().removeAll(dataToRemove);
        
        // Display cosmetics
        int index = 0;
        for (Map.Entry<CosmeticData, Cosmetic> entry : cosmeticsMap.entrySet()) {
            if (index == 35)
                break;
            
            GuiItem item = new GuiItem(CosmeticItem.get(entry.getValue(), entry.getKey()));
            item.setAction(event -> BackpackActionsGUI.open(uuid, category, entry.getValue(), entry.getKey()));
            addItem(item, Slot.fromIndex(index));
            index++;
        }
    }
    
    private ItemStack getEmptyItem() {
        return new ItemBuilder(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE))
                .setDisplayName("")
                .build();
    }
}
