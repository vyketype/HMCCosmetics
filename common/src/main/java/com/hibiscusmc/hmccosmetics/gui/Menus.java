package com.hibiscusmc.hmccosmetics.gui;

import com.hibiscusmc.hmccosmetics.SummitCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import me.lojosho.shaded.configurate.CommentedConfigurationNode;
import me.lojosho.shaded.configurate.ConfigurateException;
import me.lojosho.shaded.configurate.yaml.YamlConfigurationLoader;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;

public class Menus {

    private static final HashMap<String, Menu> MENUS = new HashMap<>();

    public static void addMenu(Menu menu) {
        MENUS.put(menu.getId().toUpperCase(), menu);
    }

    public static Menu getMenu(@NotNull String id) {
        return MENUS.get(id.toUpperCase());
    }

    @Contract(pure = true)
    @NotNull
    public static Collection<Menu> getMenu() {
        return MENUS.values();
    }

    public static boolean hasMenu(@NotNull String id) {
        return MENUS.containsKey(id.toUpperCase());
    }

    public static boolean hasMenu(Menu menu) {
        return MENUS.containsValue(menu);
    }

    public static Menu getDefaultMenu() { return Menus.getMenu(Settings.getDefaultMenu()); }

    @NotNull
    public static List<String> getMenuNames() {
        List<String> names = new ArrayList<>();

        for (Menu menu : MENUS.values()) {
            names.add(menu.getId());
        }

        return names;
    }

    public static Collection<Menu> values() {
        return MENUS.values();
    }

    public static void setup() {
        MENUS.clear();

        File cosmeticFolder = new File(SummitCosmeticsPlugin.getInstance().getDataFolder() + "/menus");
        if (!cosmeticFolder.exists()) cosmeticFolder.mkdir();

        // Recursive file lookup
        try (Stream<Path> walkStream = Files.walk(cosmeticFolder.toPath())) {
            walkStream.filter(p -> p.toFile().isFile()).forEach(child -> {
                if (child.toString().endsWith("yml") || child.toString().endsWith("yaml")) {
                    MessagesUtil.sendDebugMessages("Scanning " + child);
                    // Loads file
                    YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(child).build();
                    CommentedConfigurationNode root;
                    try {
                        root = loader.load();
                    } catch (ConfigurateException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        new Menu(FilenameUtils.removeExtension(child.getFileName().toString()), root);
                    } catch (Exception e) {
                        MessagesUtil.sendDebugMessages("Unable to create menu in " + child.getFileName().toString(), Level.WARNING);
                        if (Settings.isDebugMode()) e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
