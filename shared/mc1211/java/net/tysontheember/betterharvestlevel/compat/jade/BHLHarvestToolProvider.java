package net.tysontheember.betterharvestlevel.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.tysontheember.betterharvestlevel.BHLManager;
import net.tysontheember.betterharvestlevel.Constants;
import net.tysontheember.betterharvestlevel.TierLevelHelper;
import net.tysontheember.betterharvestlevel.config.TierDefinition;
import net.tysontheember.betterharvestlevel.platform.Services;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

/**
 * Renders BHL harvest tool information in the Jade tooltip.
 * Called from BHLJadePlugin's tooltip collected callback to replace
 * Jade's built-in harvest tool display for blocks with BHL tier overrides.
 */
public final class BHLHarvestToolProvider {

    private static final ResourceLocation HARVEST_TOOL_UID = ResourceLocation.parse("minecraft:harvest_tool");
    private static final Vec2 ITEM_SIZE = new Vec2(10, 0);
    private static final float ITEM_SCALE = 0.75f;
    private static final float ITEM_OFFSET_X = -1f;
    private static final float ITEM_OFFSET_Y = -3f;

    private BHLHarvestToolProvider() {}

    /**
     * Called after all Jade providers have run. If the block has a BHL tier override,
     * removes Jade's default harvest tool elements and adds our own.
     */
    public static void handleTooltipCollected(ITooltip tooltip, BlockAccessor accessor) {
        String blockId;
        try {
            blockId = BuiltInRegistries.BLOCK.getKey(accessor.getBlock()).toString();
        } catch (Exception e) {
            return;
        }

        String requiredTierName;
        try {
            requiredTierName = Services.TIER_SERVICE.getBlockRequiredTier(blockId);
        } catch (IllegalStateException e) {
            return;
        }

        // If no BHL block override, detect vanilla tier requirement from tags
        if (requiredTierName == null) {
            requiredTierName = detectVanillaTier(accessor.getBlockState());
        }

        if (requiredTierName == null) {
            return;
        }

        TierDefinition tierDef = BHLManager.getInstance().getTierRegistry().getTier(requiredTierName);
        if (tierDef == null) {
            return;
        }

        // Remove Jade's built-in harvest tool elements (auto-tagged with the provider's UID)
        tooltip.remove(HARVEST_TOOL_UID);

        ItemStack iconStack = resolveIconItem(tierDef);
        boolean canHarvest = checkCanHarvest(accessor.getPlayer(), requiredTierName);

        IElementHelper helper = IElementHelper.get();

        // Icon element (if available) — RIGHT-aligned, matching Jade's built-in positioning
        if (!iconStack.isEmpty()) {
            IElement icon = helper.item(iconStack, ITEM_SCALE)
                    .translate(new Vec2(ITEM_OFFSET_X, ITEM_OFFSET_Y))
                    .size(ITEM_SIZE)
                    .message(null)
                    .align(IElement.Align.RIGHT);
            tooltip.append(0, icon);

            IElement spacer = helper.spacer(4, 0)
                    .align(IElement.Align.RIGHT);
            tooltip.append(0, spacer);
        }

        // Harvestability indicator (checkmark or X)
        Component indicator = canHarvest
                ? Component.literal(" \u2714").withStyle(ChatFormatting.GREEN)
                : Component.literal(" \u2718").withStyle(ChatFormatting.RED);

        // Tier name in configured color
        MutableComponent tierText = Component.literal(" " + tierDef.getDisplayName());
        if (tierDef.getColor() != null) {
            TextColor color = TextColor.parseColor(tierDef.getColor()).result().orElse(null);
            if (color != null) {
                tierText.setStyle(Style.EMPTY.withColor(color));
            }
        }

        // Combine indicator and tier text
        MutableComponent statusText = Component.empty().append(indicator).append(tierText);
        IElement textElement = helper.text(statusText)
                .translate(new Vec2(0, ITEM_OFFSET_Y + 1.5f))
                .align(IElement.Align.RIGHT);

        tooltip.append(0, textElement);
    }

    private static ItemStack resolveIconItem(TierDefinition tierDef) {
        String iconItemId = tierDef.getIconItem();
        if (iconItemId == null || iconItemId.isEmpty()) {
            // Fall back to a default pickaxe based on tier name
            return getDefaultIconForTier(tierDef.getName());
        }
        try {
            ResourceLocation rl = ResourceLocation.parse(iconItemId);
            Item item = BuiltInRegistries.ITEM.get(rl);
            if (item == Items.AIR) {
                return getDefaultIconForTier(tierDef.getName());
            }
            return new ItemStack(item);
        } catch (Exception e) {
            Constants.LOG.warn("BHL Jade: invalid iconItem '{}' for tier '{}'", iconItemId, tierDef.getName(), e);
            return getDefaultIconForTier(tierDef.getName());
        }
    }

    private static ItemStack getDefaultIconForTier(String tierName) {
        return switch (tierName) {
            case "wood" -> new ItemStack(Items.WOODEN_PICKAXE);
            case "gold" -> new ItemStack(Items.GOLDEN_PICKAXE);
            case "stone" -> new ItemStack(Items.STONE_PICKAXE);
            case "iron" -> new ItemStack(Items.IRON_PICKAXE);
            case "diamond" -> new ItemStack(Items.DIAMOND_PICKAXE);
            case "netherite" -> new ItemStack(Items.NETHERITE_PICKAXE);
            default -> ItemStack.EMPTY;
        };
    }

    private static boolean checkCanHarvest(Player player, String requiredTierName) {
        if (player == null) {
            return false;
        }

        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return false;
        }

        if (held.getItem() instanceof TieredItem tieredItem) {
            String toolTierName = Services.TIER_SERVICE.getToolTier(
                    BuiltInRegistries.ITEM.getKey(held.getItem()).toString());

            if (toolTierName == null) {
                toolTierName = TierLevelHelper.getTierName(tieredItem.getTier());
                if (toolTierName == null) {
                    toolTierName = TierLevelHelper.getTierNameByVanillaLevel(
                            TierLevelHelper.getLevel(tieredItem.getTier()));
                }
            }

            return BHLManager.getInstance().getTierRegistry().canMine(toolTierName, requiredTierName);
        }

        return false;
    }

    /**
     * Detect the required tier from vanilla block tags.
     * Returns the BHL tier name if a vanilla tier requirement is found, null otherwise.
     */
    private static String detectVanillaTier(BlockState state) {
        // Check from highest to lowest — a block could theoretically be in multiple tags
        if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) return "diamond";
        if (state.is(BlockTags.NEEDS_IRON_TOOL)) return "iron";
        if (state.is(BlockTags.NEEDS_STONE_TOOL)) return "stone";
        return null;
    }
}
