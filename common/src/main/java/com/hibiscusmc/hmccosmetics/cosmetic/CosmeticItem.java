package com.hibiscusmc.hmccosmetics.cosmetic;

import com.hibiscusmc.hmccosmetics.util.ItemBuilder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CosmeticItem {
    public static ItemStack get(Cosmetic cosmetic, CosmeticData data) {
        // lore
        List<String> loreList = new ArrayList<>();
        cosmetic.getItem().lore().forEach(line -> {
            loreList.add(LegacyComponentSerializer.legacySection().serialize(line));
        });
        String[] lore = new String[loreList.size()];
        loreList.toArray(lore);
        
        ItemBuilder builder = new ItemBuilder(cosmetic.getItem().getType());
        builder.setDisplayName(cosmetic.getName())
                .setCustomModelData(cosmetic.getItem().getItemMeta().getCustomModelData())
                .addLoreArray(lore)
                .addLoreLine("")
                .addLoreLine("§7Obtained: §d" + data.getWhenObtained())
                .addLoreLine("§7Time Worn: §b" + data.getTimeWorn());
        
        if (data.getMessagesChatted() != 0) {
            builder.addLoreLine("§7Messages Chatted: §6" + data.getMessagesChatted());
        }
        
        builder.addLoreLine("");
        
        int minted = cosmetic.getAmountMinted();
        if (minted != -1) {
            builder.addLoreLine("§7Mint: §8#§a" + data.getMintNumber() + " §8of §c" + minted);
            builder.addLoreLine("");
        }
        
        String stars = cosmetic.getStars() == 0 ? "" : " §8— §6" + cosmetic.getStars() + " ★";
        builder.addLoreLine(cosmetic.getRarity().getName() + stars);
        // todo: attributes
        builder.addLoreLine("");
        
        return builder.build();
    }
}
