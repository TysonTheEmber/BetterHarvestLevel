package net.tysontheember.betterharvestlevel.tier;

import net.tysontheember.betterharvestlevel.Constants;
import net.tysontheember.betterharvestlevel.config.EquivalenceGroup;
import net.tysontheember.betterharvestlevel.config.EquivalencesConfig;
import net.tysontheember.betterharvestlevel.config.TierDefinition;
import net.tysontheember.betterharvestlevel.config.TiersConfig;

import java.util.*;

public class TierRegistry {
    private final List<TierDefinition> orderedTiers = new ArrayList<>();
    private final Map<String, TierDefinition> tiersByName = new LinkedHashMap<>();
    private final Map<String, Set<String>> equivalenceMap = new HashMap<>();

    public void loadFromConfig(TiersConfig tiersConfig, EquivalencesConfig equivConfig) {
        orderedTiers.clear();
        tiersByName.clear();
        equivalenceMap.clear();

        // Add tiers in order from config
        if (tiersConfig.getTiers() != null) {
            for (TierDefinition tier : tiersConfig.getTiers()) {
                orderedTiers.add(tier);
                tiersByName.put(tier.getName(), tier);
            }
        }

        // Build equivalence groups
        if (equivConfig != null && equivConfig.getGroups() != null) {
            for (EquivalenceGroup group : equivConfig.getGroups()) {
                Set<String> tierSet = new HashSet<>(group.getTiers());
                for (String tierName : group.getTiers()) {
                    equivalenceMap.computeIfAbsent(tierName, k -> new HashSet<>()).addAll(tierSet);
                }
            }
        }

        Constants.LOG.info("Loaded {} tiers and {} equivalence groups",
            orderedTiers.size(),
            equivConfig != null && equivConfig.getGroups() != null ? equivConfig.getGroups().size() : 0);
    }

    public TierDefinition getTier(String name) {
        return tiersByName.get(name);
    }

    public List<TierDefinition> getOrderedTiers() {
        return Collections.unmodifiableList(orderedTiers);
    }

    public List<TierDefinition> getCustomTiers() {
        List<TierDefinition> custom = new ArrayList<>();
        for (TierDefinition tier : orderedTiers) {
            if (!tier.isBuiltIn()) {
                custom.add(tier);
            }
        }
        return custom;
    }

    /**
     * Check if toolTier is sufficient to mine a block requiring requiredTier.
     * Takes into account tier levels and equivalence groups.
     */
    public boolean canMine(String toolTierName, String requiredTierName) {
        TierDefinition toolTier = tiersByName.get(toolTierName);
        TierDefinition requiredTier = tiersByName.get(requiredTierName);

        if (toolTier == null || requiredTier == null) {
            return true; // If we can't resolve, allow mining
        }

        // Direct level comparison
        if (toolTier.getLevel() >= requiredTier.getLevel()) {
            return true;
        }

        // Check equivalence groups
        Set<String> toolEquivalents = equivalenceMap.get(toolTierName);
        if (toolEquivalents != null) {
            for (String equiv : toolEquivalents) {
                TierDefinition equivTier = tiersByName.get(equiv);
                if (equivTier != null && equivTier.getLevel() >= requiredTier.getLevel()) {
                    return true;
                }
            }
        }

        return false;
    }

    public int getTierCount() {
        return orderedTiers.size();
    }
}
