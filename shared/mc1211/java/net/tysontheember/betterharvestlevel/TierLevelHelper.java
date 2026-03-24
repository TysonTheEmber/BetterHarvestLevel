package net.tysontheember.betterharvestlevel;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

/**
 * Maps vanilla Tier enum values to BHL tier names and level numbers for 1.21.1+
 * where Tier.getLevel() no longer exists.
 */
public final class TierLevelHelper {

    private TierLevelHelper() {}

    public static int getLevel(Tier tier) {
        if (tier == Tiers.WOOD) return 0;
        if (tier == Tiers.GOLD) return 0;
        if (tier == Tiers.STONE) return 1;
        if (tier == Tiers.IRON) return 2;
        if (tier == Tiers.DIAMOND) return 3;
        if (tier == Tiers.NETHERITE) return 4;
        // Unknown tier - try to estimate from speed (rough heuristic)
        return (int) (tier.getSpeed() / 2.0f);
    }

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
                return "netherite";
        }
    }
}
