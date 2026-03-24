package net.tysontheember.betterharvestlevel;

import net.tysontheember.betterharvestlevel.config.BHLConfig;
import net.tysontheember.betterharvestlevel.config.ConfigIO;
import net.tysontheember.betterharvestlevel.platform.Services;
import net.tysontheember.betterharvestlevel.platform.services.ITierService;
import net.tysontheember.betterharvestlevel.tier.TierRegistry;

import java.nio.file.Path;

/**
 * Central manager for BetterHarvestLevel. Holds the loaded config and tier registry.
 * Platform-specific code should call init() during mod setup and reload() for hot-reloading.
 */
public final class BHLManager {
    private static BHLManager instance;

    private final Path configDir;
    private BHLConfig config;
    private final TierRegistry tierRegistry = new TierRegistry();

    private BHLManager(Path configDir) {
        this.configDir = configDir;
    }

    public static void init(Path configDir) {
        instance = new BHLManager(configDir);
        instance.loadConfig();
        instance.applyAll();
    }

    public static BHLManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("BHLManager has not been initialized yet");
        }
        return instance;
    }

    public void loadConfig() {
        config = ConfigIO.load(configDir);
        tierRegistry.loadFromConfig(config.getTiersConfig(), config.getEquivalencesConfig());
        Constants.LOG.info("BetterHarvestLevel config loaded: {} tiers, {} block overrides, {} tool overrides",
            tierRegistry.getTierCount(),
            config.getBlockOverrides().size(),
            config.getToolOverrides().size());
    }

    public void applyAll() {
        ITierService tierService = Services.TIER_SERVICE;
        tierService.registerCustomTiers(tierRegistry);
        tierService.applyBlockOverrides(config.getBlockOverrides(), tierRegistry);
        tierService.applyToolOverrides(config.getToolOverrides(), tierRegistry);
    }

    public void reload() {
        loadConfig();
        applyAll();
        Constants.LOG.info("BetterHarvestLevel config reloaded");
    }

    public BHLConfig getConfig() { return config; }
    public TierRegistry getTierRegistry() { return tierRegistry; }
}
