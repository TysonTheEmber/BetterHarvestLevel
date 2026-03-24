package net.tysontheember.betterharvestlevel.neoforge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.tysontheember.betterharvestlevel.BHLManager;
import net.tysontheember.betterharvestlevel.Constants;
import net.tysontheember.betterharvestlevel.platform.Services;
import net.tysontheember.betterharvestlevel.tier.OverrideResolver;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod(Constants.MOD_ID)
public class BetterHarvestLevelNeoForge {

    public BetterHarvestLevelNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        Constants.LOG.info("BetterHarvestLevel initializing on NeoForge 1.21.1");
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            BHLManager.init(Services.PLATFORM.getConfigDir());
        }
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            net.tysontheember.betterharvestlevel.command.BHLCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onTagsUpdated(TagsUpdatedEvent event) {
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

                if (Services.TIER_SERVICE instanceof net.tysontheember.betterharvestlevel.neoforge.platform.NeoForgeTierService neoSvc) {
                    neoSvc.setResolvedBlockOverrides(resolvedBlocks);
                    neoSvc.setResolvedToolOverrides(resolvedTools);
                }

                Constants.LOG.info("Tags updated: resolved {} block and {} tool overrides",
                    resolvedBlocks.size(), resolvedTools.size());
            } catch (IllegalStateException ignored) {}
        }
    }
}
