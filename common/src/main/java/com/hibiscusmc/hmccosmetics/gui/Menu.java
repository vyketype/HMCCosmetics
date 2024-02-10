package com.hibiscusmc.hmccosmetics.gui;

import com.hibiscusmc.hmccosmetics.SummitCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.events.PlayerMenuOpenEvent;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.gui.type.Type;
import com.hibiscusmc.hmccosmetics.gui.type.Types;
import com.hibiscusmc.hmccosmetics.gui.type.types.TypeCosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import lombok.Getter;
import me.lojosho.hibiscuscommons.config.serializer.ItemSerializer;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import me.lojosho.hibiscuscommons.util.AdventureUtils;
import me.lojosho.hibiscuscommons.util.StringUtils;
import me.lojosho.shaded.configurate.ConfigurationNode;
import me.lojosho.shaded.configurate.serialize.SerializationException;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Menu {

    @Getter
    private final String id;
    @Getter
    private final String title;
    @Getter
    private final int rows;
    @Getter
    private final ConfigurationNode config;
    @Getter
    private final String permissionNode;
    private final HashMap<Integer, List<MenuItem>> items;
    @Getter
    private final int refreshRate;
    @Getter
    private final boolean shading;

    public Menu(String id, @NotNull ConfigurationNode config) {
        this.id = id;
        this.config = config;

        title = config.node("title").getString("chest");
        rows = config.node("rows").getInt(1);
        permissionNode = config.node("permission").getString("");
        refreshRate = config.node("refresh-rate").getInt(-1);
        shading = config.node("shading").getBoolean(Settings.isDefaultShading());

        items = new HashMap<>();
        setupItems();

        Menus.addMenu(this);
    }

    private void setupItems() {
        for (ConfigurationNode config : config.node("items").childrenMap().values()) {

            List<String> slotString;
            try {
                slotString = config.node("slots").getList(String.class);
            } catch (SerializationException e) {
                continue;
            }
            if (slotString == null) {
                MessagesUtil.sendDebugMessages("Unable to get valid slot for " + config.key().toString());
                continue;
            }

            List<Integer> slots = getSlots(slotString);

            if (slots.isEmpty()) {
                MessagesUtil.sendDebugMessages("Slot is empty for " + config.key().toString());
                continue;
            }

            ItemStack item;
            try {
                item = ItemSerializer.INSTANCE.deserialize(ItemStack.class, config.node("item"));
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }

            if (item == null) {
                MessagesUtil.sendDebugMessages("Something went wrong with the item creation for " + config.key().toString());
                continue;
            }

            int priority = config.node("priority").getInt(1);

            Type type = null;

            if (!config.node("type").virtual()) {
                String typeId = config.node("type").getString("");
                if (Types.isType(typeId)) type = Types.getType(typeId);
            }

            for (Integer slot : slots) {
                MenuItem menuItem = new MenuItem(slots, item, type, priority, config);
                if (items.containsKey(slot)) {
                    List<MenuItem> menuItems = items.get(slot);
                    menuItems.add(menuItem);
                    menuItems.sort(priorityCompare);
                    items.put(slot, menuItems);
                } else {
                    items.put(slot, new ArrayList<>(Arrays.asList(menuItem)));
                }
            }
        }
    }

    public void openMenu(CosmeticUser user) {
        openMenu(user, false);
    }

    public void openMenu(@NotNull CosmeticUser user, boolean ignorePermission) {
        Player player = user.getPlayer();
        if (player == null) return;
        if (!ignorePermission && !permissionNode.isEmpty()) {
            if (!player.hasPermission(permissionNode) && !player.isOp()) {
                MessagesUtil.sendMessage(player, "no-permission");
                return;
            }
        }
        final Component component = AdventureUtils.MINI_MESSAGE.deserialize(Hooks.processPlaceholders(player, this.title));
        Gui gui = Gui.gui()
                .title(component)
                .rows(this.rows)
                .create();

        gui.setDefaultClickAction(event -> event.setCancelled(true));

        AtomicInteger taskid = new AtomicInteger(-1);
        gui.setOpenGuiAction(event -> {
            Runnable run = () -> {
                if (gui.getInventory().getViewers().isEmpty() && taskid.get() != -1) {
                    Bukkit.getScheduler().cancelTask(taskid.get());
                }

                updateMenu(user, gui);
            };

            if (refreshRate != -1) {
                taskid.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(SummitCosmeticsPlugin.getInstance(), run, 0, refreshRate));
            } else {
                run.run();
            }
        });

        gui.setCloseGuiAction(event -> {
            if (taskid.get() != -1) Bukkit.getScheduler().cancelTask(taskid.get());
        });

        // API
        PlayerMenuOpenEvent event = new PlayerMenuOpenEvent(user, this);
        Bukkit.getScheduler().runTask(SummitCosmeticsPlugin.getInstance(), () -> Bukkit.getPluginManager().callEvent(event));
        if (event.isCancelled()) return;
        // Internal

        Bukkit.getScheduler().runTask(SummitCosmeticsPlugin.getInstance(), () -> {
            gui.open(player);
            updateMenu(user, gui); // fixes shading? I know I do this twice but it's easier than writing a whole new class to deal with this shit
        });
    }

    private void updateMenu(CosmeticUser user, Gui gui) {
        StringBuilder title = new StringBuilder(this.title);

        int row = 0;
        if (shading) {
            for (int i = 0; i < gui.getInventory().getSize(); i++) {
                // Handles the title
                if (i % 9 == 0) {
                    if (row == 0) {
                        title.append(Settings.getFirstRowShift()); // Goes back to the start of the gui
                    } else {
                        title.append(Settings.getSequentRowShift());
                    }
                    row += 1;
                } else {
                    title.append(Settings.getIndividualColumnShift()); // Goes to the next slot
                }

                boolean occupied = false;

                if (items.containsKey(i)) {
                    // Handles the items
                    List<MenuItem> menuItems = items.get(i);
                    MenuItem item = menuItems.get(0);
                    updateItem(user, gui, i);

                    if (item.type() instanceof TypeCosmetic) {
                        Cosmetic cosmetic = Cosmetics.getCosmetic(item.itemConfig().node("cosmetic").getString(""));
                        if (cosmetic == null) continue;
                        if (user.hasCosmeticInSlot(cosmetic)) {
                            title.append(Settings.getEquippedCosmeticColor());
                        } else {
                            if (user.canEquipCosmetic(cosmetic, true)) {
                                title.append(Settings.getEquipableCosmeticColor());
                            } else {
                                title.append(Settings.getLockedCosmeticColor());
                            }
                        }
                        occupied = true;
                    }
                }
                if (occupied) {
                    title.append(Settings.getBackground().replaceAll("<row>", String.valueOf(row)));
                } else {
                    title.append(Settings.getClearBackground().replaceAll("<row>", String.valueOf(row)));
                }
            }
            MessagesUtil.sendDebugMessages("Updated menu with title " + title);
            gui.updateTitle(StringUtils.parseStringToString(Hooks.processPlaceholders(user.getPlayer(), title.toString())));
        } else {
            for (int i = 0; i < gui.getInventory().getSize(); i++) {
                if (items.containsKey(i)) {
                    updateItem(user, gui, i);
                }
            }
        }
    }

    private void updateItem(CosmeticUser user, Gui gui, int slot) {
        if (!items.containsKey(slot)) return;
        List<MenuItem> menuItems = items.get(slot);
        if (menuItems.isEmpty()) return;

        for (MenuItem item : menuItems) {
            Type type = item.type();
            ItemStack modifiedItem = getMenuItem(user, type, item.itemConfig(), item.item().clone(), slot);
            if (modifiedItem.getType().isAir()) continue;
            GuiItem guiItem = ItemBuilder.from(modifiedItem).asGuiItem();
            guiItem.setAction(event -> {
                MessagesUtil.sendDebugMessages("Selected slot " + slot);
                final ClickType clickType = event.getClick();
                if (type != null) type.run(user, item.itemConfig(), clickType);
                updateMenu(user, gui);
            });

            MessagesUtil.sendDebugMessages("Added " + slot + " as " + guiItem + " in the menu");
            gui.updateItem(slot, guiItem);
            break;
        }
    }

    @NotNull
    private List<Integer> getSlots(@NotNull List<String> slotString) {
        List<Integer> slots = new ArrayList<>();

        for (String a : slotString) {
            if (a.contains("-")) {
                String[] split = a.split("-");
                int min = Integer.parseInt(split[0]);
                int max = Integer.parseInt(split[1]);
                slots.addAll(getSlots(min, max));
            } else {
                slots.add(Integer.valueOf(a));
            }
        }

        return slots;
    }

    @NotNull
    private List<Integer> getSlots(int small, int max) {
        List<Integer> slots = new ArrayList<>();

        for (int i = small; i <= max; i++) slots.add(i);
        return slots;
    }

    @Contract("_, _, _, _, _ -> param2")
    @NotNull
    private ItemStack getMenuItem(CosmeticUser user, Type type, ConfigurationNode config, ItemStack itemStack, int slot) {
        if (!itemStack.hasItemMeta()) return itemStack;
        return type.setItem(user, config, itemStack, slot);
    }

    public boolean canOpen(Player player) {
        if (permissionNode.isEmpty()) return true;
        return player.isOp() || player.hasPermission(permissionNode);
    }
    
    public static ItemStack getBlackPane() {
        return new com.hibiscusmc.hmccosmetics.util.ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName("")
                .build();
    }

    public static Comparator<MenuItem> priorityCompare = Comparator.comparing(MenuItem::priority).reversed();
}
