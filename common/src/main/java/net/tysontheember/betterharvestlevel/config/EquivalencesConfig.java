package net.tysontheember.betterharvestlevel.config;

import java.util.ArrayList;
import java.util.List;

public class EquivalencesConfig {
    private int configVersion = 1;
    private List<EquivalenceGroup> groups = new ArrayList<>();

    public int getConfigVersion() { return configVersion; }
    public List<EquivalenceGroup> getGroups() { return groups; }

    public void setConfigVersion(int configVersion) { this.configVersion = configVersion; }
    public void setGroups(List<EquivalenceGroup> groups) { this.groups = groups; }
}
