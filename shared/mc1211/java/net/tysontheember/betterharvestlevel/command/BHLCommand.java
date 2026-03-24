package net.tysontheember.betterharvestlevel.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.tysontheember.betterharvestlevel.BHLManager;
import net.tysontheember.betterharvestlevel.config.TierDefinition;
import net.tysontheember.betterharvestlevel.platform.Services;
import net.tysontheember.betterharvestlevel.tier.TierRegistry;

import java.util.List;

public class BHLCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bhl")
            .then(Commands.literal("check")
                .then(Commands.argument("target", StringArgumentType.string())
                    .suggests((ctx, builder) -> {
                        builder.suggest("held");
                        builder.suggest("looking");
                        return builder.buildFuture();
                    })
                    .executes(BHLCommand::checkTarget))
                .executes(BHLCommand::checkLooking))
            .then(Commands.literal("info")
                .requires(src -> src.hasPermission(0))
                .executes(BHLCommand::showInfo))
            .then(Commands.literal("reload")
                .requires(src -> src.hasPermission(2))
                .executes(BHLCommand::reload))
        );
    }

    private static int checkTarget(CommandContext<CommandSourceStack> ctx) {
        String target = StringArgumentType.getString(ctx, "target");

        if ("held".equalsIgnoreCase(target)) {
            return checkHeldItem(ctx);
        } else if ("looking".equalsIgnoreCase(target)) {
            return checkLooking(ctx);
        }

        // Try as block ID
        String blockTier = Services.TIER_SERVICE.getBlockRequiredTier(target);
        if (blockTier != null) {
            TierDefinition tier = getTierDef(blockTier);
            ctx.getSource().sendSuccess(() -> Component.literal("Block ")
                .append(Component.literal(target).withStyle(s -> s.withColor(0xFFFF55)))
                .append(Component.literal(" requires tier: "))
                .append(tierComponent(blockTier, tier)), false);
            return 1;
        }

        // Try as item ID
        String toolTier = Services.TIER_SERVICE.getToolTier(target);
        if (toolTier != null) {
            TierDefinition tier = getTierDef(toolTier);
            ctx.getSource().sendSuccess(() -> Component.literal("Tool ")
                .append(Component.literal(target).withStyle(s -> s.withColor(0xFFFF55)))
                .append(Component.literal(" has tier: "))
                .append(tierComponent(toolTier, tier)), false);
            return 1;
        }

        ctx.getSource().sendFailure(Component.literal("No BHL override found for: " + target));
        return 0;
    }

    private static int checkHeldItem(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("This command requires a player"));
            return 0;
        }

        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("You are not holding an item"));
            return 0;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(held.getItem());
        String toolTier = Services.TIER_SERVICE.getToolTier(itemId.toString());
        if (toolTier != null) {
            TierDefinition tier = getTierDef(toolTier);
            ctx.getSource().sendSuccess(() -> Component.literal("Held item ")
                .append(Component.literal(itemId.toString()).withStyle(s -> s.withColor(0xFFFF55)))
                .append(Component.literal(" has tier: "))
                .append(tierComponent(toolTier, tier)), false);
            return 1;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("Held item ")
            .append(Component.literal(itemId.toString()).withStyle(s -> s.withColor(0xFFFF55)))
            .append(Component.literal(" has no BHL tier override")), false);
        return 1;
    }

    private static int checkLooking(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("This command requires a player"));
            return 0;
        }

        HitResult hit = player.pick(5.0, 0.0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() == HitResult.Type.MISS) {
            ctx.getSource().sendFailure(Component.literal("Not looking at a block"));
            return 0;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = player.level().getBlockState(pos);
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        String blockTier = Services.TIER_SERVICE.getBlockRequiredTier(blockId.toString());
        if (blockTier != null) {
            TierDefinition tier = getTierDef(blockTier);
            ctx.getSource().sendSuccess(() -> Component.literal("Block ")
                .append(Component.literal(blockId.toString()).withStyle(s -> s.withColor(0xFFFF55)))
                .append(Component.literal(" requires tier: "))
                .append(tierComponent(blockTier, tier)), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("Block ")
                .append(Component.literal(blockId.toString()).withStyle(s -> s.withColor(0xFFFF55)))
                .append(Component.literal(" has no BHL tier override")), false);
        }
        return 1;
    }

    private static int showInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            BHLManager manager = BHLManager.getInstance();
            TierRegistry registry = manager.getTierRegistry();
            List<TierDefinition> tiers = registry.getOrderedTiers();

            ctx.getSource().sendSuccess(() -> Component.literal("=== BetterHarvestLevel Info ===")
                .withStyle(s -> s.withColor(0x55FFFF)), false);

            ctx.getSource().sendSuccess(() -> Component.literal("Registered tiers: " + tiers.size()), false);

            for (TierDefinition tier : tiers) {
                String custom = tier.isBuiltIn() ? "" : " [custom]";
                int color = parseColor(tier.getColor());
                ctx.getSource().sendSuccess(() -> Component.literal("  Level " + tier.getLevel() + ": ")
                    .append(Component.literal(tier.getDisplayName())
                        .withStyle(s -> s.withColor(color)))
                    .append(Component.literal(" (" + tier.getName() + ")" + custom)), false);
            }

            int blockOverrides = manager.getConfig().getBlockOverrides().size();
            int toolOverrides = manager.getConfig().getToolOverrides().size();
            ctx.getSource().sendSuccess(() -> Component.literal("Block overrides: " + blockOverrides), false);
            ctx.getSource().sendSuccess(() -> Component.literal("Tool overrides: " + toolOverrides), false);

            return 1;
        } catch (IllegalStateException e) {
            ctx.getSource().sendFailure(Component.literal("BHLManager not initialized"));
            return 0;
        }
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        try {
            BHLManager.getInstance().reload();
            ctx.getSource().sendSuccess(() -> Component.literal("BetterHarvestLevel config reloaded!")
                .withStyle(s -> s.withColor(0x55FF55)), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed to reload config: " + e.getMessage()));
            return 0;
        }
    }

    private static TierDefinition getTierDef(String tierName) {
        try {
            return BHLManager.getInstance().getTierRegistry().getTier(tierName);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private static MutableComponent tierComponent(String tierName, TierDefinition tier) {
        if (tier != null) {
            int color = parseColor(tier.getColor());
            return Component.literal(tier.getDisplayName())
                .withStyle(s -> s.withColor(color))
                .append(Component.literal(" (" + tierName + ", level " + tier.getLevel() + ")"));
        }
        return Component.literal(tierName);
    }

    private static int parseColor(String hex) {
        if (hex == null || hex.isEmpty()) return 0xFFFFFF;
        try {
            return Integer.parseInt(hex.replace("#", ""), 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }
}
