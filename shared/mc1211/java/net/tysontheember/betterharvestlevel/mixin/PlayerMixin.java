package net.tysontheember.betterharvestlevel.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.state.BlockState;
import net.tysontheember.betterharvestlevel.BHLManager;
import net.tysontheember.betterharvestlevel.TierLevelHelper;
import net.tysontheember.betterharvestlevel.platform.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept tool-correctness checks for block drops (1.21.1).
 * Uses TierLevelHelper since Tier.getLevel() was removed in 1.21.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "hasCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void betterharvestlevel$hasCorrectToolForDrops(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        try {
            BHLManager manager = BHLManager.getInstance();
            ResourceLocation blockRL = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            String blockId = blockRL.toString();

            String requiredTier = Services.TIER_SERVICE.getBlockRequiredTier(blockId);
            if (requiredTier == null) return; // No override, let vanilla handle it

            Player player = (Player) (Object) this;
            ItemStack held = player.getMainHandItem();

            if (held.isEmpty()) {
                cir.setReturnValue(false);
                return;
            }

            if (held.getItem() instanceof TieredItem tieredItem) {
                String toolTierName = Services.TIER_SERVICE.getToolTier(
                    BuiltInRegistries.ITEM.getKey(held.getItem()).toString());

                if (toolTierName == null) {
                    // No BHL override - map vanilla tier to BHL tier name
                    toolTierName = TierLevelHelper.getTierName(tieredItem.getTier());
                    if (toolTierName == null) {
                        // Unknown modded tier - map by vanilla level
                        toolTierName = TierLevelHelper.getTierNameByVanillaLevel(
                            TierLevelHelper.getLevel(tieredItem.getTier()));
                    }
                }

                cir.setReturnValue(manager.getTierRegistry().canMine(toolTierName, requiredTier));
            } else {
                cir.setReturnValue(false);
            }
        } catch (IllegalStateException ignored) {
            // BHLManager not initialized yet, skip
        }
    }
}
