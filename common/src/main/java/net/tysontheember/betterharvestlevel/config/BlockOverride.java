package net.tysontheember.betterharvestlevel.config;

public class BlockOverride {
    private String target;
    private String type;
    private String requiredTier;

    public BlockOverride() {}

    public BlockOverride(String target, String type, String requiredTier) {
        this.target = target;
        this.type = type;
        this.requiredTier = requiredTier;
    }

    public String getTarget() { return target; }
    public String getType() { return type; }
    public String getRequiredTier() { return requiredTier; }

    public void setTarget(String target) { this.target = target; }
    public void setType(String type) { this.type = type; }
    public void setRequiredTier(String requiredTier) { this.requiredTier = requiredTier; }
}
