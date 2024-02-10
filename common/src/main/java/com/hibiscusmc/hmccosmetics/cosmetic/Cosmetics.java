package com.hibiscusmc.hmccosmetics.cosmetic;

import com.google.common.collect.HashBiMap;
import com.hibiscusmc.hmccosmetics.SummitCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.types.*;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import me.lojosho.shaded.configurate.CommentedConfigurationNode;
import me.lojosho.shaded.configurate.ConfigurateException;
import me.lojosho.shaded.configurate.ConfigurationNode;
import me.lojosho.shaded.configurate.yaml.YamlConfigurationLoader;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

public class Cosmetics {
    
    private static final String[] FILENAMES = {
            "defaultcosmetics.yml",
            "hats.yml",
            "backwear.yml",
            "offhand.yml",
            "buddies.yml",
            "emotes.yml",
            "tools.yml",
            "miscellaneous.yml"
    };
    
    private static final HashBiMap<String, Cosmetic> COSMETICS = HashBiMap.create();

    public static void addCosmetic(Cosmetic cosmetic) {
        COSMETICS.put(cosmetic.getId(), cosmetic);
    }

    public static void removeCosmetic(String id) {
        COSMETICS.remove(id);
    }

    public static void removeCosmetic(Cosmetic cosmetic) {
        COSMETICS.remove(cosmetic);
    }

    @Nullable
    public static Cosmetic getCosmetic(String id) {
        return COSMETICS.get(id);
    }

    @Contract(pure = true)
    @NotNull
    public static Set<Cosmetic> values() {
        return COSMETICS.values();
    }

    @Contract(pure = true)
    @NotNull
    public static Set<String> keys() {
        return COSMETICS.keySet();
    }

    public static boolean hasCosmetic(String id) {
        return COSMETICS.containsKey(id);
    }

    public static boolean hasCosmetic(Cosmetic cosmetic) {
        return COSMETICS.containsValue(cosmetic);
    }

    public static void setup() {
        COSMETICS.clear();

        File cosmeticFolder = new File(SummitCosmeticsPlugin.getInstance().getDataFolder() + "/cosmetics");
        if (!cosmeticFolder.exists()) cosmeticFolder.mkdir();

        File[] directoryListing = cosmeticFolder.listFiles();
        if (directoryListing == null) return;
        
        for (String filename : FILENAMES) {
            String path = SummitCosmeticsPlugin.getInstance().getDataFolder() + "/cosmetics/" + filename;
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(Paths.get(path)).build();
            CommentedConfigurationNode root;
            try {
                root = loader.load();
            } catch (ConfigurateException e) {
                throw new RuntimeException(e);
            }
            setupCosmetics(root);
        }
    }

    private static void setupCosmetics(@NotNull CommentedConfigurationNode config) {
        for (ConfigurationNode cosmeticConfig : config.childrenMap().values()) {
            try {
                String id = cosmeticConfig.key().toString();
                MessagesUtil.sendDebugMessages("Attempting to add " + id);
                ConfigurationNode slotNode = cosmeticConfig.node("slot");
                if (slotNode.virtual()) {
                    MessagesUtil.sendDebugMessages("Unable to create " + id + " because there is no slot defined!", Level.WARNING);
                    continue;
                }
                if (!EnumUtils.isValidEnum(CosmeticSlot.class, slotNode.getString())) {
                    MessagesUtil.sendDebugMessages("Unable to create " + id + " because " + slotNode.getString() + " is not a valid slot!", Level.WARNING);
                    continue;
                }
                CosmeticSlot slot = CosmeticSlot.valueOf(cosmeticConfig.node("slot").getString());
                if (cosmeticConfig.node("assigned_ids").getList(String.class) != null) {
                    // go to another file
                    // find cosmetic id node
                    // search for all variants and run through this method (recursive)
                }
                switch (slot) {
                    case MISC -> new CosmeticMiscType(id, cosmeticConfig);
                    case BALLOON -> new CosmeticBalloonType(id, cosmeticConfig);
                    case BACKPACK -> new CosmeticBackpackType(id, cosmeticConfig);
                    case MAINHAND -> new CosmeticMainhandType(id, cosmeticConfig);
                    case EMOTE -> new CosmeticEmoteType(id, cosmeticConfig);
                    default -> new CosmeticArmorType(id, cosmeticConfig);
                }
            } catch (Exception e) {
                if (Settings.isDebugMode()) e.printStackTrace();
            }
        }
    }
}