package net.tysontheember.betterharvestlevel.config;

public class TierDefinition {
    private String name;
    private int level;
    private String displayName;
    private String color;
    private boolean builtIn;
    private String after;
    private String before;
    private String iconItem;

    public TierDefinition() {}

    public TierDefinition(String name, int level, String displayName, String color, boolean builtIn) {
        this.name = name;
        this.level = level;
        this.displayName = displayName;
        this.color = color;
        this.builtIn = builtIn;
    }

    public TierDefinition(String name, int level, String displayName, String color, boolean builtIn, String iconItem) {
        this(name, level, displayName, color, builtIn);
        this.iconItem = iconItem;
    }

    public String getName() { return name; }
    public int getLevel() { return level; }
    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public boolean isBuiltIn() { return builtIn; }
    public String getAfter() { return after; }
    public String getBefore() { return before; }

    public void setName(String name) { this.name = name; }
    public void setLevel(int level) { this.level = level; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setColor(String color) { this.color = color; }
    public void setBuiltIn(boolean builtIn) { this.builtIn = builtIn; }
    public void setAfter(String after) { this.after = after; }
    public void setBefore(String before) { this.before = before; }

    public String getIconItem() { return iconItem; }
    public void setIconItem(String iconItem) { this.iconItem = iconItem; }
}
