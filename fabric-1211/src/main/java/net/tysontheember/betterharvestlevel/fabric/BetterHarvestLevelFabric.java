package net.tysontheember.betterharvestlevel.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.tysontheember.betterharvestlevel.command.BHLCommand;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.tysontheember.betterharvestlevel.BHLManager;
import net.tysontheember.betterharvestlevel.Constants;
import net.tysontheember.betterharvestlevel.platform.Services;
import net.tysontheember.betterharvestlevel.tier.OverrideResolver;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BetterHarvestLevelFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Constants.LOG.info("BetterHarvestLevel initializing on Fabric 1.21.1");
        BHLManager.init(Services.PLATFORM.getConfigDir());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            BHLCommand.register(dispatcher));

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (!success) return;
            resolveOverrides();
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> resolveOverrides());
    }

    private static void resolveOverrides() {
        try {
            BHLManager manager = BHLManager.getInstance();
            Set<String> allBlockIds = new HashSet<>();
            BuiltInRegistries.BLOCK.keySet().forEach(rl -> allBlockIds.add(rl.toString()));

            Set<String> allItemIds = new HashSet<>();
            BuiltInRegistries.ITEM.keySet().forEach(rl -> allItemIds.add(rl.toString()));

            Set<String> ignoredMods = new HashSet<>(manager.getConfig().getIgnoredMods());

            java.util.function.Function<String, Set<String>> blockTagResolver = tagStr -> {
                ResourceLocation tagRL = ResourceLocation.parse(tagStr);
                TagKey<Block> tag = TagKey.create(BuiltInRegistries.BLOCK.key(), tagRL);
                Set<String> members = new HashSet<>();
                BuiltInRegistries.BLOCK.getTagOrEmpty(tag).forEach(holder ->
                    members.add(BuiltInRegistries.BLOCK.getKey(holder.value()).toString()));
                return members;
            };

            Map<String, String> resolvedBlocks = OverrideResolver.resolveBlockOverrides(
                manager.getConfig().getBlockOverrides(), allBlockIds, blockTagResolver, ignoredMods);

            Map<String, String> resolvedTools = OverrideResolver.resolveToolOverrides(
                manager.getConfig().getToolOverrides(), allItemIds,
                tagStr -> {
                    ResourceLocation tagRL = ResourceLocation.parse(tagStr);
                    TagKey<net.minecraft.world.item.Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(), tagRL);
                    Set<String> members = new HashSet<>();
                    BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(holder ->
                        members.add(BuiltInRegistries.ITEM.getKey(holder.value()).toString()));
                    return members;
                },
                ignoredMods);

            if (Services.TIER_SERVICE instanceof net.tysontheember.betterharvestlevel.fabric.platform.FabricTierService fabricSvc) {
                fabricSvc.setResolvedBlockOverrides(resolvedBlocks);
                fabricSvc.setResolvedToolOverrides(resolvedTools);
            }

            Constants.LOG.info("Data packs loaded: resolved {} block and {} tool overrides",
                resolvedBlocks.size(), resolvedTools.size());
        } catch (IllegalStateException ignored) {}
    }
}
