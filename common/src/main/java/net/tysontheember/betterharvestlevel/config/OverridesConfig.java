package net.tysontheember.betterharvestlevel.config;

import java.util.ArrayList;
import java.util.List;

public class OverridesConfig<T> {
    private int configVersion = 1;
    private List<T> overrides = new ArrayList<>();

    public int getConfigVersion() { return configVersion; }
    public List<T> getOverrides() { return overrides; }

    public void setConfigVersion(int configVersion) { this.configVersion = configVersion; }
    public void setOverrides(List<T> overrides) { this.overrides = overrides; }
}
