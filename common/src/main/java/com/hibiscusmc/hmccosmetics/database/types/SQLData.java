package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.SummitCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticData;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.Bukkit;
import org.bukkit.Color;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class SQLData extends Data {
    @Override
    @SuppressWarnings({"resource"}) // Duplicate is from deprecated InternalData
    public CosmeticUser get(UUID uniqueId) {
        CosmeticUser user = new CosmeticUser(uniqueId);

        Bukkit.getScheduler().runTaskAsynchronously(SummitCosmeticsPlugin.getInstance(), () -> {
            try {
                PreparedStatement preparedStatement = preparedStatement("SELECT * FROM COSMETICDATABASE WHERE UUID = ?;");
                preparedStatement.setString(1, uniqueId.toString());
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    String rawData = rs.getString("COSMETICS");
                    
                    String[] array = rawData.split(",");
                    HashMap<CosmeticData, Cosmetic> cosmeticsMap = deserializeMap(array[0]);
                    user.setCosmeticsMap(cosmeticsMap);
                    
                    String s1 = rawData.substring(rawData.indexOf(",") + 1);
                    Map<CosmeticSlot, Map<Cosmetic, Color>> equippedCosmetics = deserializeData(user, s1);
                    
                    for (Map<Cosmetic, Color> cosmeticColors : equippedCosmetics.values()) {
                        for (Cosmetic cosmetic : cosmeticColors.keySet()) {
                            Bukkit.getScheduler().runTask(SummitCosmeticsPlugin.getInstance(), () -> {
                                // This can not be async.
                                user.equipCosmetic(cosmetic, cosmeticColors.get(cosmetic));
                            });
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return user;
    }

    @Override
    @SuppressWarnings("resource")
    public void save(CosmeticUser user) {
        Runnable run = () -> {
            try {
                PreparedStatement preparedSt = preparedStatement("REPLACE INTO COSMETICDATABASE(UUID,COSMETICS) VALUES(?,?);");
                preparedSt.setString(1, user.getUniqueId().toString());
                preparedSt.setString(2, serializeData(user));
                preparedSt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        if (!SummitCosmeticsPlugin.getInstance().isDisabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(SummitCosmeticsPlugin.getInstance(), run);
        } else {
            run.run();
        }
    }

    public abstract PreparedStatement preparedStatement(String query);
}
