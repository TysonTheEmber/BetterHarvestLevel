package net.tysontheember.betterharvestlevel.forge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.tysontheember.betterharvestlevel.BHLManager;
import net.tysontheember.betterharvestlevel.Constants;
import net.tysontheember.betterharvestlevel.platform.Services;
import net.tysontheember.betterharvestlevel.tier.OverrideResolver;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod(Constants.MOD_ID)
public class BetterHarvestLevelForge {

    public BetterHarvestLevelForge() {
        Constants.LOG.info("BetterHarvestLevel initializing on Forge 1.20.1");
    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            BHLManager.init(Services.PLATFORM.getConfigDir());
        }
    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            net.tysontheember.betterharvestlevel.command.BHLCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onTagsUpdated(TagsUpdatedEvent event) {
            try {
                BHLManager manager = BHLManager.getInstance();

                // Collect all block IDs
                Set<String> allBlockIds = new HashSet<>();
                BuiltInRegistries.BLOCK.keySet().forEach(rl -> allBlockIds.add(rl.toString()));

                // Collect all item IDs
                Set<String> allItemIds = new HashSet<>();
                BuiltInRegistries.ITEM.keySet().forEach(rl -> allItemIds.add(rl.toString()));

                Set<String> ignoredMods = new HashSet<>(manager.getConfig().getIgnoredMods());

                // Tag resolver for blocks
                java.util.function.Function<String, Set<String>> blockTagResolver = tagStr -> {
                    ResourceLocation tagRL = new ResourceLocation(tagStr);
                    TagKey<Block> tag = TagKey.create(BuiltInRegistries.BLOCK.key(), tagRL);
                    Set<String> members = new HashSet<>();
                    BuiltInRegistries.BLOCK.getTagOrEmpty(tag).forEach(holder ->
                        members.add(BuiltInRegistries.BLOCK.getKey(holder.value()).toString()));
                    return members;
                };

                // Resolve and apply block overrides
                Map<String, String> resolvedBlocks = OverrideResolver.resolveBlockOverrides(
                    manager.getConfig().getBlockOverrides(), allBlockIds, blockTagResolver, ignoredMods);

                // Resolve and apply tool overrides
                Map<String, String> resolvedTools = OverrideResolver.resolveToolOverrides(
                    manager.getConfig().getToolOverrides(), allItemIds,
                    tagStr -> {
                        ResourceLocation tagRL = new ResourceLocation(tagStr);
                        TagKey<net.minecraft.world.item.Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(), tagRL);
                        Set<String> members = new HashSet<>();
                        BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(holder ->
                            members.add(BuiltInRegistries.ITEM.getKey(holder.value()).toString()));
                        return members;
                    },
                    ignoredMods);

                // Update the tier service with resolved overrides
                Services.TIER_SERVICE.applyBlockOverrides(
                    manager.getConfig().getBlockOverrides(), manager.getTierRegistry());
                Services.TIER_SERVICE.applyToolOverrides(
                    manager.getConfig().getToolOverrides(), manager.getTierRegistry());

                // Also store the fully resolved maps
                if (Services.TIER_SERVICE instanceof net.tysontheember.betterharvestlevel.forge.platform.ForgeTierService forgeSvc) {
                    forgeSvc.setResolvedBlockOverrides(resolvedBlocks);
                    forgeSvc.setResolvedToolOverrides(resolvedTools);
                }

                Constants.LOG.info("Tags updated: resolved {} block and {} tool overrides",
                    resolvedBlocks.size(), resolvedTools.size());

            } catch (IllegalStateException ignored) {
                // BHLManager not initialized yet
            }
        }
    }
}
