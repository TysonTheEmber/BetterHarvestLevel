package net.tysontheember.betterharvestlevel.forge.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import net.tysontheember.betterharvestlevel.BHLManager;
import net.tysontheember.betterharvestlevel.Constants;
import net.tysontheember.betterharvestlevel.config.BlockOverride;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

/**
 * Injects BHL block overrides into the mining level tags at tag load time.
 * This ensures Jade and other mods see the correct tier requirements.
 */
@Mixin(TagLoader.class)
public class TagLoaderMixin {

    @Shadow
    private String directory;

    private static final Map<String, ResourceLocation> TIER_TO_TAG;
    private static final Set<ResourceLocation> ALL_NEEDS_TAGS;

    static {
        TIER_TO_TAG = new HashMap<>();
        TIER_TO_TAG.put("stone", new ResourceLocation("minecraft", "needs_stone_tool"));
        TIER_TO_TAG.put("iron", new ResourceLocation("minecraft", "needs_iron_tool"));
        TIER_TO_TAG.put("diamond", new ResourceLocation("minecraft", "needs_diamond_tool"));
        TIER_TO_TAG.put("netherite", new ResourceLocation("forge", "needs_netherite_tool"));

        ALL_NEEDS_TAGS = new HashSet<>(TIER_TO_TAG.values());
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void betterharvestlevel$injectBlockTags(
            ResourceManager resourceManager,
            CallbackInfoReturnable<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> cir) {

        if (!"tags/blocks".equals(this.directory)) return;

        BHLManager manager;
        try {
            manager = BHLManager.getInstance();
        } catch (IllegalStateException e) {
            return;
        }

        Map<ResourceLocation, List<TagLoader.EntryWithSource>> map = cir.getReturnValue();
        int injected = 0;

        for (BlockOverride override : manager.getConfig().getBlockOverrides()) {
            if (!"block".equals(override.getType())) continue;

            ResourceLocation blockRL = new ResourceLocation(override.getTarget());
            String requiredTier = override.getRequiredTier();

            // Remove this block from ALL needs_*_tool tags (Forge supports remove=true)
            for (ResourceLocation tagId : ALL_NEEDS_TAGS) {
                map.computeIfAbsent(tagId, k -> new ArrayList<>())
                    .add(new TagLoader.EntryWithSource(
                        TagEntry.element(blockRL), Constants.MOD_ID, true));
            }

            // Add to the correct tag for the required tier
            ResourceLocation targetTag = TIER_TO_TAG.get(requiredTier);
            if (targetTag != null) {
                map.computeIfAbsent(targetTag, k -> new ArrayList<>())
                    .add(new TagLoader.EntryWithSource(
                        TagEntry.element(blockRL), Constants.MOD_ID));
                injected++;
            } else if ("wood".equals(requiredTier) || "gold".equals(requiredTier)) {
                // Wood/gold tier = no tag needed, just remove from all (already done above)
                injected++;
            } else {
                Constants.LOG.debug("No tag mapping for tier '{}', block '{}' will use mixin-only enforcement",
                    requiredTier, override.getTarget());
            }
        }

        if (injected > 0) {
            Constants.LOG.info("Injected {} block tier overrides into mining level tags", injected);
        }
    }
}
