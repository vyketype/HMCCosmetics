package com.hibiscusmc.hmccosmetics.cosmetic;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;
import org.ocpsoft.prettytime.PrettyTime;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CosmeticData {
    private String uniqueCosmeticId;
    
    private long obtainedTimestamp;
    private int mintNumber;
    private int messagesChatted;
    @Getter(AccessLevel.NONE)
    private int timeWorn;
    
    // todo: color
    
    // todo: private final HashMap<Cosmetic, Color> attributesMap;
    
    public CosmeticData(int mintNumber, int messagesChatted) {
        uniqueCosmeticId = RandomStringUtils.randomAlphanumeric(16);
        obtainedTimestamp = System.currentTimeMillis();
        this.mintNumber = mintNumber;
        this.messagesChatted = messagesChatted;
        timeWorn = 0;
        // attributesMap = new HashMap<>();
    }
    
    public void addAttribute(Cosmetic cosmetic, Color color) {
        // attributesMap.put(cosmetic, color);
    }
    
    public String getWhenObtained() {
        long secondsTime = (System.currentTimeMillis() - obtainedTimestamp) / 1000;
        PrettyTime prettyTime = new PrettyTime();
        return prettyTime.format(LocalDateTime.now().minusSeconds(secondsTime));
    }
    
    public String getTimeWorn() {
        double minutes = Math.floor(timeWorn / 60.0);
        return minutes + "m " + (timeWorn - minutes * 60) + "s";
    }
    
    // Attribute Types:
    //- Solid Tab Colours
    //- Gradient Tab Colours
    //- Animated Tab Effects
    //- Tab Icons
    //- Particles and Effects
}
