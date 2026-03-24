package net.tysontheember.betterharvestlevel.fabric.platform;

import net.tysontheember.betterharvestlevel.Constants;
import net.tysontheember.betterharvestlevel.config.BlockOverride;
import net.tysontheember.betterharvestlevel.config.TierDefinition;
import net.tysontheember.betterharvestlevel.config.ToolOverride;
import net.tysontheember.betterharvestlevel.platform.services.ITierService;
import net.tysontheember.betterharvestlevel.tier.TierRegistry;

import java.util.*;

public class FabricTierService implements ITierService {

    private final Map<String, String> blockTierOverrides = new HashMap<>();
    private final Map<String, String> toolTierOverrides = new HashMap<>();

    @Override
    public void registerCustomTiers(TierRegistry registry) {
        // Fabric 1.21.1 uses tag-based mining levels.
        // Custom tiers map to #fabric:needs_tool_level_N tags.
        for (TierDefinition tier : registry.getCustomTiers()) {
            Constants.LOG.info("Custom tier '{}' (level {}) will use #fabric:needs_tool_level_{} tag",
                tier.getName(), tier.getLevel(), tier.getLevel());
        }
    }

    @Override
    public void applyBlockOverrides(List<BlockOverride> overrides, TierRegistry registry) {
        blockTierOverrides.clear();
        for (BlockOverride override : overrides) {
            if ("block".equals(override.getType())) {
                blockTierOverrides.put(override.getTarget(), override.getRequiredTier());
            }
        }
        Constants.LOG.info("Applied {} block tier overrides on Fabric 1.21.1", blockTierOverrides.size());
    }

    @Override
    public void applyToolOverrides(List<ToolOverride> overrides, TierRegistry registry) {
        toolTierOverrides.clear();
        for (ToolOverride override : overrides) {
            if ("item".equals(override.getType())) {
                toolTierOverrides.put(override.getTarget(), override.getTier());
            }
        }
        Constants.LOG.info("Applied {} tool tier overrides on Fabric 1.21.1", toolTierOverrides.size());
    }

    public void setResolvedBlockOverrides(Map<String, String> resolved) {
        blockTierOverrides.clear();
        blockTierOverrides.putAll(resolved);
        Constants.LOG.info("Fabric 1.21.1: resolved {} block tier overrides", blockTierOverrides.size());
    }

    public void setResolvedToolOverrides(Map<String, String> resolved) {
        toolTierOverrides.clear();
        toolTierOverrides.putAll(resolved);
        Constants.LOG.info("Fabric 1.21.1: resolved {} tool tier overrides", toolTierOverrides.size());
    }

    @Override
    public String getBlockRequiredTier(String blockId) {
        return blockTierOverrides.get(blockId);
    }

    @Override
    public String getToolTier(String itemId) {
        return toolTierOverrides.get(itemId);
    }
}
