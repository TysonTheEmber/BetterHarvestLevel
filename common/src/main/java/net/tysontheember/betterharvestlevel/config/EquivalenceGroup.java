package net.tysontheember.betterharvestlevel.config;

import java.util.List;

public class EquivalenceGroup {
    private String name;
    private List<String> tiers;

    public EquivalenceGroup() {}

    public EquivalenceGroup(String name, List<String> tiers) {
        this.name = name;
        this.tiers = tiers;
    }

    public String getName() { return name; }
    public List<String> getTiers() { return tiers; }

    public void setName(String name) { this.name = name; }
    public void setTiers(List<String> tiers) { this.tiers = tiers; }
}
