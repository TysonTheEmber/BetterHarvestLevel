package net.tysontheember.betterharvestlevel.forge.platform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import net.tysontheember.betterharvestlevel.Constants;
import net.tysontheember.betterharvestlevel.config.BlockOverride;
import net.tysontheember.betterharvestlevel.config.TierDefinition;
import net.tysontheember.betterharvestlevel.config.ToolOverride;
import net.tysontheember.betterharvestlevel.platform.services.ITierService;
import net.tysontheember.betterharvestlevel.tier.TierRegistry;

import java.util.*;

public class ForgeTierService implements ITierService {

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

        for (TierDefinition tier : registry.getCustomTiers()) {
            TagKey<Block> tierTag = BlockTags.create(
                new ResourceLocation(Constants.MOD_ID, "needs_" + tier.getName() + "_tool"));

            // Resolve "after" and "before" references
            List<Object> after = new ArrayList<>();
            List<Object> before = new ArrayList<>();

            if (tier.getAfter() != null) {
                Tier afterTier = registeredTiers.get(tier.getAfter());
                if (afterTier != null) after.add(afterTier);
            }
            if (tier.getBefore() != null) {
                Tier beforeTier = registeredTiers.get(tier.getBefore());
                if (beforeTier != null) before.add(beforeTier);
            }

            ForgeTier forgeTier = new ForgeTier(
                tier.getLevel(),
                0,     // durability (not used for mining level checks)
                0.0f,  // speed
                0.0f,  // attack damage
                0,     // enchantability
                tierTag,
                () -> Ingredient.EMPTY
            );

            ResourceLocation tierRL = new ResourceLocation(Constants.MOD_ID, tier.getName());
            TierSortingRegistry.registerTier(forgeTier, tierRL, after, before);
            registeredTiers.put(tier.getName(), forgeTier);

            Constants.LOG.info("Registered custom tier '{}' (level {}) on Forge", tier.getName(), tier.getLevel());
        }
    }

    @Override
    public void applyBlockOverrides(List<BlockOverride> overrides, TierRegistry registry) {
        // Direct block overrides are stored immediately; tag/mod/regex resolved later via setResolvedBlockOverrides
        for (BlockOverride override : overrides) {
            if ("block".equals(override.getType())) {
                blockTierOverrides.put(override.getTarget(), override.getRequiredTier());
            }
        }
    }

    @Override
    public void applyToolOverrides(List<ToolOverride> overrides, TierRegistry registry) {
        for (ToolOverride override : overrides) {
            if ("item".equals(override.getType())) {
                toolTierOverrides.put(override.getTarget(), override.getTier());
            }
        }
    }

    /** Called after tags are loaded to set fully resolved overrides (including tag/mod/regex targets). */
    public void setResolvedBlockOverrides(Map<String, String> resolved) {
        blockTierOverrides.clear();
        blockTierOverrides.putAll(resolved);
        Constants.LOG.info("Forge: resolved {} block tier overrides", blockTierOverrides.size());
    }

    /** Called after tags are loaded to set fully resolved overrides. */
    public void setResolvedToolOverrides(Map<String, String> resolved) {
        toolTierOverrides.clear();
        toolTierOverrides.putAll(resolved);
        Constants.LOG.info("Forge: resolved {} tool tier overrides", toolTierOverrides.size());
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
