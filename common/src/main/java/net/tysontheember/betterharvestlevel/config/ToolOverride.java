package net.tysontheember.betterharvestlevel.config;

public class ToolOverride {
    private String target;
    private String type;
    private String tier;

    public ToolOverride() {}

    public ToolOverride(String target, String type, String tier) {
        this.target = target;
        this.type = type;
        this.tier = tier;
    }

    public String getTarget() { return target; }
    public String getType() { return type; }
    public String getTier() { return tier; }

    public void setTarget(String target) { this.target = target; }
    public void setType(String type) { this.type = type; }
    public void setTier(String tier) { this.tier = tier; }
}
