package com.hibiscusmc.hmccosmetics.util.packets;

import org.bukkit.Bukkit;

public class ReflectionUtil {
    private static String version;
    
    static {
        String[] versionArray = Bukkit.getServer().getClass().getName().replace('.', ',').split(",");
        if (versionArray.length >= 4) {
            version = versionArray[3];
        } else {
            version = "";
        }
    }
    
    public static Class<?> getNMSClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + version + "." + className);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("An error occurred while finding NMS class.", ex);
        }
    }
    
    public static Class<?> getOBCClass(String className) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + className);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("An error occurred while finding OBC class.", ex);
        }
    }
}
