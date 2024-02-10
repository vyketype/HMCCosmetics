package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import lombok.Getter;
import me.lojosho.shaded.configurate.ConfigurationNode;
import me.lojosho.shaded.configurate.serialize.SerializationException;

import java.util.List;

@Getter
public class CosmeticMiscType extends Cosmetic {
    
    final List<String> colors;
    
    public CosmeticMiscType(String id, ConfigurationNode config) throws SerializationException {
        super(id, config);
        colors = config.node("colors").getList(String.class);
    }
    
    @Override
    public void update(CosmeticUser user) {
        // todo
    }
}
