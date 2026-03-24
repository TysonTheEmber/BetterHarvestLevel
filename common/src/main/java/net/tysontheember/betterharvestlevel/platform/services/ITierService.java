package net.tysontheember.betterharvestlevel.platform.services;

import net.tysontheember.betterharvestlevel.config.BlockOverride;
import net.tysontheember.betterharvestlevel.config.ToolOverride;
import net.tysontheember.betterharvestlevel.tier.TierRegistry;

import java.util.List;

public interface ITierService {

    /**
     * Register custom tiers with the platform's tier system.
     * Called during mod initialization before registries are frozen.
     */
    void registerCustomTiers(TierRegistry registry);

    /**
     * Apply block mining level overrides.
     * Called after tags/registries are loaded.
     */
    void applyBlockOverrides(List<BlockOverride> overrides, TierRegistry registry);

    /**
     * Apply tool tier overrides.
     * Called after registries are loaded.
     */
    void applyToolOverrides(List<ToolOverride> overrides, TierRegistry registry);

    /**
     * Get the display name of the tier required to mine a given block.
     * Returns null if no custom override exists.
     */
    String getBlockRequiredTier(String blockId);

    /**
     * Get the display name of a tool's current tier.
     * Returns null if no custom override exists.
     */
    String getToolTier(String itemId);
}
