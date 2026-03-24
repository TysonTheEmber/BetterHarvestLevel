package net.tysontheember.betterharvestlevel.config;

import java.util.ArrayList;
import java.util.List;

public class TiersConfig {
    private int configVersion = 1;
    private List<TierDefinition> tiers = new ArrayList<>();

    public int getConfigVersion() { return configVersion; }
    public List<TierDefinition> getTiers() { return tiers; }

    public void setConfigVersion(int configVersion) { this.configVersion = configVersion; }
    public void setTiers(List<TierDefinition> tiers) { this.tiers = tiers; }

    public static TiersConfig createDefault() {
        TiersConfig config = new TiersConfig();
        config.tiers.add(new TierDefinition("wood", 0, "Wooden", "#8B6914", true, "minecraft:wooden_pickaxe"));
        config.tiers.add(new TierDefinition("gold", 0, "Golden", "#FFAA00", true, "minecraft:golden_pickaxe"));
        config.tiers.add(new TierDefinition("stone", 1, "Stone", "#888888", true, "minecraft:stone_pickaxe"));
        config.tiers.add(new TierDefinition("iron", 2, "Iron", "#C8C8C8", true, "minecraft:iron_pickaxe"));
        config.tiers.add(new TierDefinition("diamond", 3, "Diamond", "#55FFFF", true, "minecraft:diamond_pickaxe"));
        config.tiers.add(new TierDefinition("netherite", 4, "Netherite", "#4D3B3B", true, "minecraft:netherite_pickaxe"));
        return config;
    }
}
