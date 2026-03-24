package net.tysontheember.betterharvestlevel.tier;

import net.tysontheember.betterharvestlevel.Constants;
import net.tysontheember.betterharvestlevel.config.BlockOverride;
import net.tysontheember.betterharvestlevel.config.ToolOverride;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Resolves override targets (block/tag/mod/regex) against a set of known registry IDs.
 * This is called from platform-specific code which provides the actual registry contents.
 */
public final class OverrideResolver {

    private OverrideResolver() {}

    /**
     * Resolve block overrides against the known block IDs.
     * @param overrides the configured overrides
     * @param allBlockIds all known block resource location strings (e.g. "minecraft:stone")
     * @param tagResolver resolves a tag string (e.g. "#minecraft:needs_iron_tool") to a set of block IDs
     * @param ignoredMods mods whose blocks should be skipped
     * @return map of block ID -> required tier name
     */
    public static Map<String, String> resolveBlockOverrides(
            List<BlockOverride> overrides,
            Set<String> allBlockIds,
            Function<String, Set<String>> tagResolver,
            Set<String> ignoredMods) {

        Map<String, String> resolved = new LinkedHashMap<>();

        for (BlockOverride override : overrides) {
            String target = override.getTarget();
            String type = override.getType();
            String tier = override.getRequiredTier();

            switch (type) {
                case "block":
                    if (allBlockIds.contains(target)) {
                        putWithConflictLog(resolved, target, tier, "block");
                    }
                    break;

                case "tag":
                    if (target.startsWith("#")) {
                        Set<String> tagMembers = tagResolver.apply(target.substring(1));
                        if (tagMembers != null) {
                            for (String id : tagMembers) {
                                if (!isIgnored(id, ignoredMods)) {
                                    putWithConflictLog(resolved, id, tier, "block");
                                }
                            }
                        }
                    }
                    break;

                case "mod":
                    String namespace = target.endsWith(":*") ? target.substring(0, target.length() - 2) : target;
                    for (String id : allBlockIds) {
                        if (id.startsWith(namespace + ":") && !isIgnored(id, ignoredMods)) {
                            putWithConflictLog(resolved, id, tier, "block");
                        }
                    }
                    break;

                case "regex":
                    try {
                        Pattern pattern = Pattern.compile(target);
                        for (String id : allBlockIds) {
                            if (pattern.matcher(id).matches() && !isIgnored(id, ignoredMods)) {
                                putWithConflictLog(resolved, id, tier, "block");
                            }
                        }
                    } catch (PatternSyntaxException e) {
                        Constants.LOG.error("Invalid regex pattern in block override: '{}' - {}", target, e.getMessage());
                    }
                    break;

                default:
                    Constants.LOG.warn("Unknown block override type: '{}'", type);
            }
        }

        return resolved;
    }

    /**
     * Resolve tool overrides against the known item IDs.
     * @param overrides the configured overrides
     * @param allItemIds all known item resource location strings
     * @param tagResolver resolves a tag string to a set of item IDs
     * @param ignoredMods mods whose items should be skipped
     * @return map of item ID -> tier name
     */
    public static Map<String, String> resolveToolOverrides(
            List<ToolOverride> overrides,
            Set<String> allItemIds,
            Function<String, Set<String>> tagResolver,
            Set<String> ignoredMods) {

        Map<String, String> resolved = new LinkedHashMap<>();

        for (ToolOverride override : overrides) {
            String target = override.getTarget();
            String type = override.getType();
            String tier = override.getTier();

            switch (type) {
                case "item":
                    if (allItemIds.contains(target)) {
                        putWithConflictLog(resolved, target, tier, "tool");
                    }
                    break;

                case "tag":
                    if (target.startsWith("#")) {
                        Set<String> tagMembers = tagResolver.apply(target.substring(1));
                        if (tagMembers != null) {
                            for (String id : tagMembers) {
                                if (!isIgnored(id, ignoredMods)) {
                                    putWithConflictLog(resolved, id, tier, "tool");
                                }
                            }
                        }
                    }
                    break;

                case "mod":
                    String namespace = target.endsWith(":*") ? target.substring(0, target.length() - 2) : target;
                    for (String id : allItemIds) {
                        if (id.startsWith(namespace + ":") && !isIgnored(id, ignoredMods)) {
                            putWithConflictLog(resolved, id, tier, "tool");
                        }
                    }
                    break;

                case "regex":
                    try {
                        Pattern pattern = Pattern.compile(target);
                        for (String id : allItemIds) {
                            if (pattern.matcher(id).matches() && !isIgnored(id, ignoredMods)) {
                                putWithConflictLog(resolved, id, tier, "tool");
                            }
                        }
                    } catch (PatternSyntaxException e) {
                        Constants.LOG.error("Invalid regex pattern in tool override: '{}' - {}", target, e.getMessage());
                    }
                    break;

                default:
                    Constants.LOG.warn("Unknown tool override type: '{}'", type);
            }
        }

        return resolved;
    }

    private static void putWithConflictLog(Map<String, String> resolved, String id, String newTier, String kind) {
        String existing = resolved.put(id, newTier);
        if (existing != null && !existing.equals(newTier)) {
            Constants.LOG.warn("BHL {} override conflict: '{}' changed from tier '{}' to '{}' (later override wins)",
                kind, id, existing, newTier);
        }
    }

    private static boolean isIgnored(String id, Set<String> ignoredMods) {
        if (ignoredMods == null || ignoredMods.isEmpty()) return false;
        int colonIdx = id.indexOf(':');
        if (colonIdx < 0) return false;
        return ignoredMods.contains(id.substring(0, colonIdx));
    }
}
