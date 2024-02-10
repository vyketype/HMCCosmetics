package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.SummitCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import me.lojosho.hibiscuscommons.hooks.Hook;
import me.lojosho.hibiscuscommons.hooks.HookFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A hook that integrates the plugin {@link SummitCosmeticsPlugin HMCCosmetics} to provide cosmetic items
 */
public class HookHMCCosmetics extends Hook {
    public HookHMCCosmetics() {
        super("HMCCosmetics", HookFlag.ITEM_SUPPORT);
    }

    /**
     * Gets a cosmetic {@link ItemStack} that is associated with the provided id from the plugin {@link SummitCosmeticsPlugin HMCCosmetics}
     */
    @Override
    public ItemStack getItem(@NotNull String itemId) {
        Cosmetic cosmetic = Cosmetics.getCosmetic(itemId);
        if (cosmetic == null) return null;
        return cosmetic.getItem();
    }
}

