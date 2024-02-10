package com.hibiscusmc.hmccosmetics.cosmetic;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Rarity {
    COMMON('f'),
    RARE('9'),
    EPIC('5'),
    LEGENDARY('6'),
    MYTHICAL('d'),
    LIMITED_EDITION('c'),
    EXCLUSIVE('c');
    
    private final char color;
    
    public String getName() {
        return "§" + color + "§l" + name().toUpperCase();
    }
}
