package com.hibiscusmc.hmccosmetics.gui.backpack;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticData;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.ItemBuilder;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.ReflectionUtil;
import lombok.SneakyThrows;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

public class BackpackActionsGUI {
    @SuppressWarnings("deprecation")
    public static void open(UUID uuid, BackpackCategoriesGUI.Category category, Cosmetic cosmetic, CosmeticData data) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        
        ChestGui gui = new ChestGui(5, cosmetic.getName());
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        
        StaticPane pane = new StaticPane(0, 0, 9, 5, Pane.Priority.LOWEST);
        
        CosmeticUser user = CosmeticUsers.getUser(uuid);
        GuiItem button = getEquipOrUnequip(user, cosmetic, data, pane);
        pane.addItem(button, 4, 1);
        
        int attributesAllowed = cosmetic.getStars() - 0; // todo: - data.getAttributesMap().size();
        GuiItem apply = new GuiItem(getApplyButton(attributesAllowed));
        apply.setAction(event -> EditedBackpackGUI.open(uuid, cosmetic, data));
        pane.addItem(apply, 2, 2);
        
        GuiItem brag = new GuiItem(getBragButton());
        brag.setAction(event -> {
            TextComponent bragMessage = getBragMessage(player, cosmetic);
            player.sendMessage(bragMessage);
            player.closeInventory();
        });
        pane.addItem(brag, 4, 2);
        
        GuiItem delete = new GuiItem(getDeleteButton());
        delete.setAction(event -> ConfirmCosmeticDeleteGUI.open(uuid, data));
        pane.addItem(delete, 6, 2);
        
        GuiItem backArrow = new GuiItem(getBackArrow("Backpack - " + category.getName()));
        backArrow.setAction(event -> BackpackGUI.open(uuid, category, false));
        pane.addItem(backArrow, 4, 4);
        
        gui.addPane(pane);
        gui.show(player);
    }
    
    @SuppressWarnings("deprecation")
    @SneakyThrows
    private static TextComponent getBragMessage(Player player, Cosmetic cosmetic) {
        String itemJson = convertItemStackToJson(cosmetic.getItem());
        BaseComponent[] hoverEventComponents = new BaseComponent[]{ new TextComponent(itemJson) };
        
        HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents);
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(MessagesUtil.prefix +
                " §b" + player.getDisplayName() + " §7is showing off their §r" + cosmetic.getName() + "§7!"));
        component.setHoverEvent(event);
        
        return component;
    }
    
    public static String convertItemStackToJson(ItemStack itemStack) throws NoSuchMethodException {
        Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
        Method asNMSCopyMethod = craftItemStackClazz.getMethod("asNMSCopy", ItemStack.class);
        
        Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
        Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
        Method saveNmsItemStackMethod = nmsItemStackClazz.getMethod("save", nbtTagCompoundClazz);
        
        Object nmsNbtTagCompoundObj;
        Object nmsItemStackObj;
        Object itemAsJsonObject;
        
        try {
            nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
            nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.SEVERE, "failed to serialize itemstack to nms item", t);
            return null;
        }
        
        return itemAsJsonObject.toString();
    }
    
    @NotNull
    private static GuiItem getEquipOrUnequip(CosmeticUser user, Cosmetic cosmetic, CosmeticData data, StaticPane pane) {
        boolean isCosmeticEquipped = user.hasCosmeticInSlot(cosmetic);
        GuiItem button = new GuiItem(isCosmeticEquipped ? getEquippedButton() : getNotEquippedButton());
        Consumer<InventoryClickEvent> buttonAction = new Consumer<>() {
            @Override
            public void accept(InventoryClickEvent inventoryClickEvent) {
                // reversed button logic because it will replace the old button
                GuiItem newButton = new GuiItem(isCosmeticEquipped ? getNotEquippedButton() : getEquippedButton());
                newButton.setAction(this);
                pane.addItem(newButton, 4, 1);
                
                Player player = user.getPlayer();
                String cosmeticName = LegacyComponentSerializer.legacySection().serialize(cosmetic.getItem().displayName());
                TagResolver placeholder = TagResolver.resolver(Placeholder.parsed("cosmetic", cosmeticName));
                
                if (isCosmeticEquipped) {
                    user.removeCosmeticSlot(cosmetic);
                    MessagesUtil.sendMessage(player, "unequip-cosmetic", placeholder);
                    return;
                }
                
                user.equipCosmetic(cosmetic);
                MessagesUtil.sendMessage(player, "equip-cosmetic", placeholder);
            }
        };
        
        button.setAction(buttonAction);
        return button;
    }
    
    public static ItemStack getDeleteButton() {
        return new ItemBuilder(new ItemStack(Material.RED_DYE))
                .setDisplayName("§cRemove from Backpack")
                .addLoreLine("")
                .addLoreLine("§7Say goodbye to your")
                .addLoreLine("§7beloved cosmetic!")
                .addLoreLine("")
                .addLoreLine("§eClick to remove.")
                .build();
    }
    
    public static ItemStack getApplyButton(int attributesAllowed) {
        ItemBuilder builder = new ItemBuilder(new ItemStack(Material.DIAMOND))
                .setDisplayName("§bApply Attributes")
                .addLoreLine("")
                .addLoreLine("§7You can apply special §6attributes")
                .addLoreLine("§7on your cosmetic for a combined")
                .addLoreLine("§7effect!")
                .addLoreLine("");
        
        if (attributesAllowed > 0) {
            return builder.addLoreLine("§8" + attributesAllowed + "attribute(s) allowed")
                    .addLoreLine("")
                    .addLoreLine("§eClick to view eligible attributes.")
                    .build();
        } else {
            return builder.addLoreLine("§c0 attributes allowed")
                    .addLoreLine("")
                    .addLoreLine("§8Either you have used up all")
                    .addLoreLine("§8of your attributes or your")
                    .addLoreLine("§8cosmetic has no stars.")
                    .build();
        }
    }
    
    public static ItemStack getBragButton() {
        return new ItemBuilder(new ItemStack(Material.PAPER))
                .setDisplayName("§dBrag")
                .setGlow(true)
                .addLoreLine("")
                .addLoreLine("§7Send a chat message showing")
                .addLoreLine("§7off your cool cosmetic!")
                .addLoreLine("")
                .addLoreLine("§eClick to send message.")
                .build();
    }
    
    public static ItemStack getEquippedButton() {
        return new ItemBuilder(new ItemStack(Material.LIME_TERRACOTTA))
                .setDisplayName("§a§lEquipped")
                .addLoreLine("")
                .addLoreLine("§eClick to unequip.")
                .build();
    }
    
    public static ItemStack getNotEquippedButton() {
        return new ItemBuilder(new ItemStack(Material.RED_TERRACOTTA))
                .setDisplayName("§cNot Equipped")
                .addLoreLine("")
                .addLoreLine("§eClick to equip.")
                .build();
    }
    
    public static ItemStack getBackArrow(String text) {
        return new ItemBuilder(new ItemStack(Material.ARROW))
                .setDisplayName("§aGo Back")
                .addLoreLine("§8[" + text + "§8]")
                .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                .build();
    }
}
