package net.tysontheember.betterharvestlevel.platform;

import net.tysontheember.betterharvestlevel.platform.services.IPlatformHelper;
import net.tysontheember.betterharvestlevel.platform.services.ITierService;

import java.util.ServiceLoader;

public final class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final ITierService TIER_SERVICE = load(ITierService.class);

    private Services() {}

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz).findFirst()
            .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }
}
