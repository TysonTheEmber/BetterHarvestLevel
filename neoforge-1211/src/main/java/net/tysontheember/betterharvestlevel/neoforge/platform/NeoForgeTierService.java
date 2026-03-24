package net.tysontheember.betterharvestlevel.neoforge.platform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.SimpleTier;
import net.tysontheember.betterharvestlevel.Constants;
import net.tysontheember.betterharvestlevel.config.BlockOverride;
import net.tysontheember.betterharvestlevel.config.TierDefinition;
import net.tysontheember.betterharvestlevel.config.ToolOverride;
import net.tysontheember.betterharvestlevel.platform.services.ITierService;
import net.tysontheember.betterharvestlevel.tier.TierRegistry;

import java.util.*;

public class NeoForgeTierService implements ITierService {

    private final Map<String, Tier> registeredTiers = new HashMap<>();
    private final Map<String, String> blockTierOverrides = new HashMap<>();
    private final Map<String, String> toolTierOverrides = new HashMap<>();

    @Override
    public void registerCustomTiers(TierRegistry registry) {
        // Map built-in names to vanilla tiers
        registeredTiers.put("wood", Tiers.WOOD);
        registeredTiers.put("stone", Tiers.STONE);
        registeredTiers.put("iron", Tiers.IRON);
        registeredTiers.put("diamond", Tiers.DIAMOND);
        registeredTiers.put("gold", Tiers.GOLD);
        registeredTiers.put("netherite", Tiers.NETHERITE);

        // NeoForge 1.21.1 uses the inverted tag system: incorrectBlocksForDrops
        // A custom tier needs a tag listing blocks that CANNOT be mined by this tier.
        // The actual tag contents will be managed via tag injection in Phase 4.
        for (TierDefinition tier : registry.getCustomTiers()) {
            TagKey<Block> incorrectTag = BlockTags.create(
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "incorrect_for_" + tier.getName() + "_tool"));

            SimpleTier neoTier = new SimpleTier(
                incorrectTag,
                0,     // durability
                0.0f,  // speed
                0.0f,  // attack damage
                0,     // enchantability
                () -> Ingredient.EMPTY
            );

            registeredTiers.put(tier.getName(), neoTier);
            Constants.LOG.info("Registered custom tier '{}' (level {}) on NeoForge", tier.getName(), tier.getLevel());
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
        Constants.LOG.info("Applied {} block tier overrides on NeoForge", blockTierOverrides.size());
    }

    @Override
    public void applyToolOverrides(List<ToolOverride> overrides, TierRegistry registry) {
        toolTierOverrides.clear();
        for (ToolOverride override : overrides) {
            if ("item".equals(override.getType())) {
                toolTierOverrides.put(override.getTarget(), override.getTier());
            }
        }
        Constants.LOG.info("Applied {} tool tier overrides on NeoForge", toolTierOverrides.size());
    }

    public void setResolvedBlockOverrides(Map<String, String> resolved) {
        blockTierOverrides.clear();
        blockTierOverrides.putAll(resolved);
        Constants.LOG.info("NeoForge: resolved {} block tier overrides", blockTierOverrides.size());
    }

    public void setResolvedToolOverrides(Map<String, String> resolved) {
        toolTierOverrides.clear();
        toolTierOverrides.putAll(resolved);
        Constants.LOG.info("NeoForge: resolved {} tool tier overrides", toolTierOverrides.size());
    }

    @Override
    public String getBlockRequiredTier(String blockId) {
        return blockTierOverrides.get(blockId);
    }

    @Override
    public String getToolTier(String itemId) {
        return toolTierOverrides.get(itemId);
    }

    public Map<String, Tier> getRegisteredTiers() {
        return Collections.unmodifiableMap(registeredTiers);
    }
}
