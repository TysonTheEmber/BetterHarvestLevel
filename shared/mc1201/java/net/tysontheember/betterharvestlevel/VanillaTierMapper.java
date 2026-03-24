package net.tysontheember.betterharvestlevel;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

/**
 * Maps vanilla Tier objects to BHL tier names.
 * This is necessary because BHL may insert custom tiers that shift level numbers,
 * so we can't rely on vanilla Tier.getLevel() matching BHL level numbers.
 */
public final class VanillaTierMapper {

    private VanillaTierMapper() {}

    /**
     * Get the BHL tier name for a vanilla tool tier.
     * Returns null if the tier is not a recognized vanilla tier (i.e. modded).
     */
    public static String getTierName(Tier tier) {
        if (tier == Tiers.WOOD) return "wood";
        if (tier == Tiers.GOLD) return "gold";
        if (tier == Tiers.STONE) return "stone";
        if (tier == Tiers.IRON) return "iron";
        if (tier == Tiers.DIAMOND) return "diamond";
        if (tier == Tiers.NETHERITE) return "netherite";
        return null;
    }

    /**
     * For unknown modded tiers, map the vanilla level to the closest built-in BHL tier name.
     * Vanilla levels: wood/gold=0, stone=1, iron=2, diamond=3, netherite=4
     */
    public static String getTierNameByVanillaLevel(int vanillaLevel) {
        switch (vanillaLevel) {
            case 0: return "wood";
            case 1: return "stone";
            case 2: return "iron";
            case 3: return "diamond";
            case 4: return "netherite";
            default:
                if (vanillaLevel < 0) return "wood";
                return "netherite"; // Higher than netherite, treat as netherite
        }
    }
}
