package net.tysontheember.betterharvestlevel.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.tysontheember.betterharvestlevel.Constants;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public final class ConfigIO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigIO() {}

    public static BHLConfig load(Path configDir) {
        Path folder = configDir.resolve(Constants.CONFIG_FOLDER_NAME);
        BHLConfig config = new BHLConfig();

        if (!Files.exists(folder)) {
            save(configDir, config);
            return config;
        }

        // Load tiers
        Path tiersFile = folder.resolve("tiers.json");
        if (Files.exists(tiersFile)) {
            TiersConfig tiers = readJson(tiersFile, TiersConfig.class);
            if (tiers != null) {
                config.setTiersConfig(tiers);
            }
        }

        // Load block overrides (supports layering: blocks.json, blocks_create.json, etc.)
        List<BlockOverride> allBlockOverrides = new ArrayList<>();
        for (Path file : findLayeredFiles(folder, "blocks")) {
            OverridesConfig<BlockOverride> overrides = readJson(file,
                new TypeToken<OverridesConfig<BlockOverride>>() {}.getType());
            if (overrides != null && overrides.getOverrides() != null) {
                allBlockOverrides.addAll(overrides.getOverrides());
            }
        }
        config.setBlockOverrides(allBlockOverrides);

        // Load tool overrides (supports layering: tools.json, tools_create.json, etc.)
        List<ToolOverride> allToolOverrides = new ArrayList<>();
        for (Path file : findLayeredFiles(folder, "tools")) {
            OverridesConfig<ToolOverride> overrides = readJson(file,
                new TypeToken<OverridesConfig<ToolOverride>>() {}.getType());
            if (overrides != null && overrides.getOverrides() != null) {
                allToolOverrides.addAll(overrides.getOverrides());
            }
        }
        config.setToolOverrides(allToolOverrides);

        // Load equivalences
        Path equivFile = folder.resolve("equivalences.json");
        if (Files.exists(equivFile)) {
            EquivalencesConfig equiv = readJson(equivFile, EquivalencesConfig.class);
            if (equiv != null) {
                config.setEquivalencesConfig(equiv);
            }
        }

        return config;
    }

    public static void save(Path configDir, BHLConfig config) {
        try {
            Path folder = configDir.resolve(Constants.CONFIG_FOLDER_NAME);
            Files.createDirectories(folder);

            writeJson(folder.resolve("tiers.json"), config.getTiersConfig());

            // Only write blocks.json and tools.json if they don't exist yet (don't overwrite user layered files)
            Path blocksFile = folder.resolve("blocks.json");
            if (!Files.exists(blocksFile)) {
                OverridesConfig<BlockOverride> blocksConfig = new OverridesConfig<>();
                blocksConfig.setOverrides(config.getBlockOverrides());
                writeJson(blocksFile, blocksConfig);
            }

            Path toolsFile = folder.resolve("tools.json");
            if (!Files.exists(toolsFile)) {
                OverridesConfig<ToolOverride> toolsConfig = new OverridesConfig<>();
                toolsConfig.setOverrides(config.getToolOverrides());
                writeJson(toolsFile, toolsConfig);
            }

            Path equivFile = folder.resolve("equivalences.json");
            if (!Files.exists(equivFile)) {
                writeJson(equivFile, config.getEquivalencesConfig());
            }

        } catch (IOException e) {
            Constants.LOG.error("Failed to save BetterHarvestLevel config: {}", e.getMessage());
        }
    }

    /**
     * Find all layered config files matching a prefix (e.g., "blocks" matches blocks.json, blocks_create.json).
     * Files are returned sorted alphabetically so layering order is deterministic.
     */
    private static List<Path> findLayeredFiles(Path folder, String prefix) {
        TreeMap<String, Path> sorted = new TreeMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, prefix + "*.json")) {
            for (Path entry : stream) {
                sorted.put(entry.getFileName().toString(), entry);
            }
        } catch (IOException e) {
            Constants.LOG.error("Failed to scan config folder for {} files: {}", prefix, e.getMessage());
        }
        return new ArrayList<>(sorted.values());
    }

    private static <T> T readJson(Path file, Class<T> type) {
        try (Reader reader = Files.newBufferedReader(file)) {
            return GSON.fromJson(reader, type);
        } catch (Exception e) {
            Constants.LOG.error("Failed to read config file '{}': {}", file.getFileName(), e.getMessage());
            return null;
        }
    }

    private static <T> T readJson(Path file, java.lang.reflect.Type type) {
        try (Reader reader = Files.newBufferedReader(file)) {
            return GSON.fromJson(reader, type);
        } catch (Exception e) {
            Constants.LOG.error("Failed to read config file '{}': {}", file.getFileName(), e.getMessage());
            return null;
        }
    }

    private static void writeJson(Path file, Object data) {
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            Constants.LOG.error("Failed to write config file '{}': {}", file.getFileName(), e.getMessage());
        }
    }
}
