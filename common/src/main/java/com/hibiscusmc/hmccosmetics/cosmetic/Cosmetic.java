package com.hibiscusmc.hmccosmetics.cosmetic;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import lombok.Getter;
import lombok.Setter;
import me.lojosho.hibiscuscommons.config.serializer.ItemSerializer;
import me.lojosho.shaded.configurate.ConfigurationNode;
import me.lojosho.shaded.configurate.serialize.SerializationException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

public abstract class Cosmetic {

    @Getter @Setter
    private String id;
    @Getter @Setter
    private String permission;
    private ItemStack item;
    @Getter @Setter
    private String material;
    @Getter @Setter
    private CosmeticSlot slot;
    @Getter @Setter
    private boolean dyable;

    @Getter @Setter
    private int stars;
    @Getter @Setter
    private Rarity rarity;
    @Getter @Setter
    private int amountMinted;

    protected Cosmetic(String id, @NotNull ConfigurationNode config) {
        this.id = id;

        if (!config.node("permission").virtual()) {
            this.permission = config.node("permission").getString();
        } else {
            this.permission = null;
        }

        if (!config.node("item").virtual()) {
            this.material = config.node("item", "material").getString();
            this.item = generateItemStack(config.node("item"));
        }

        MessagesUtil.sendDebugMessages("Slot: " + config.node("slot").getString());

        setSlot(CosmeticSlot.valueOf(config.node("slot").getString()));
        setDyable(config.node("dyeable").getBoolean(false));
        setRarity(Rarity.valueOf(config.node("rarity").getString("COMMON").toUpperCase()));
        setAmountMinted(config.node("amountMinted").getInt(-1));
        setStars(config.node("stars").getInt(0));

        MessagesUtil.sendDebugMessages("Dyeable " + dyable);
        Cosmetics.addCosmetic(this);
    }
    
    public String getName() {
        final Component itemNameComponent = item.displayName();
        return LegacyComponentSerializer.legacySection().serialize(itemNameComponent);
    }

    public boolean requiresPermission() {
        return permission != null;
    }

    public abstract void update(CosmeticUser user);

    @Nullable
    public ItemStack getItem() {
        if (item == null) return null;
        return item.clone();
    }

    protected ItemStack generateItemStack(ConfigurationNode config) {
        try {
            ItemStack item = ItemSerializer.INSTANCE.deserialize(ItemStack.class, config);
            if (item == null) {
                MessagesUtil.sendDebugMessages("Unable to create item for " + getId(), Level.SEVERE);
                return new ItemStack(Material.AIR);
            }
            return item;
        } catch (SerializationException e) {
            MessagesUtil.sendDebugMessages("Fatal error encountered for " + getId() + " regarding Serialization of item", Level.SEVERE);
            throw new RuntimeException(e);
        }
    }
}
