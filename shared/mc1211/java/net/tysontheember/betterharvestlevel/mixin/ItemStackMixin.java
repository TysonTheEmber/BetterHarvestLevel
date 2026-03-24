package net.tysontheember.betterharvestlevel.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.state.BlockState;
import net.tysontheember.betterharvestlevel.BHLManager;
import net.tysontheember.betterharvestlevel.TierLevelHelper;
import net.tysontheember.betterharvestlevel.platform.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to override ItemStack-level mining checks with BHL's tier system.
 * This affects both mining speed and the isCorrectToolForDrops check
 * (which Jade and other mods use to determine harvestability).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void betterharvestlevel$getDestroySpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        try {
            String requiredTier = betterharvestlevel$getRequiredTier(state);
            if (requiredTier == null) return;

            ItemStack self = (ItemStack) (Object) this;
            if (self.isEmpty() || !(self.getItem() instanceof TieredItem tieredItem)) {
                cir.setReturnValue(cir.getReturnValue() * 0.1f);
                return;
            }

            if (!betterharvestlevel$canToolMine(self, tieredItem, requiredTier)) {
                cir.setReturnValue(cir.getReturnValue() * 0.1f);
            }
        } catch (IllegalStateException ignored) {
            // BHLManager not initialized yet
        }
    }

    @Inject(method = "isCorrectToolForDrops", at = @At("RETURN"), cancellable = true)
    private void betterharvestlevel$isCorrectToolForDrops(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        try {
            String requiredTier = betterharvestlevel$getRequiredTier(state);
            if (requiredTier == null) return;

            ItemStack self = (ItemStack) (Object) this;
            if (self.isEmpty() || !(self.getItem() instanceof TieredItem tieredItem)) {
                cir.setReturnValue(false);
                return;
            }

            cir.setReturnValue(betterharvestlevel$canToolMine(self, tieredItem, requiredTier));
        } catch (IllegalStateException ignored) {
            // BHLManager not initialized yet
        }
    }

    @Unique
    private static String betterharvestlevel$getRequiredTier(BlockState state) {
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        String requiredTier = Services.TIER_SERVICE.getBlockRequiredTier(blockId);
        if (requiredTier != null) return requiredTier;

        if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) return "diamond";
        if (state.is(BlockTags.NEEDS_IRON_TOOL)) return "iron";
        if (state.is(BlockTags.NEEDS_STONE_TOOL)) return "stone";
        return null;
    }

    @Unique
    private static boolean betterharvestlevel$canToolMine(ItemStack stack, TieredItem tieredItem, String requiredTier) {
        String toolTierName = Services.TIER_SERVICE.getToolTier(
            BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());

        if (toolTierName == null) {
            toolTierName = TierLevelHelper.getTierName(tieredItem.getTier());
            if (toolTierName == null) {
                toolTierName = TierLevelHelper.getTierNameByVanillaLevel(
                    TierLevelHelper.getLevel(tieredItem.getTier()));
            }
        }

        return BHLManager.getInstance().getTierRegistry().canMine(toolTierName, requiredTier);
    }
}
