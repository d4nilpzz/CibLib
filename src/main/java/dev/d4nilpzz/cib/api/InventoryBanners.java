package dev.d4nilpzz.cib.api;

import dev.d4nilpzz.cib.Cib;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public final class InventoryBanners {

    private static final Map<ResourceKey<CreativeModeTab>, BannerLayout> LAYOUTS =
            new ConcurrentHashMap<>();

    private static final Map<CreativeModeTab, BannerLayout> RESOLVED = new ConcurrentHashMap<>();

    public static BannerLayout register(ResourceKey<CreativeModeTab> tab, BannerLayout layout) {
        Objects.requireNonNull(tab, "tab");
        Objects.requireNonNull(layout, "layout");

        BannerLayout previous = LAYOUTS.put(tab, layout);
        RESOLVED.clear();
        if (previous != null) {
            Cib.LOGGER.debug("Replaced the banner layout registered for {}", tab);
        }
        return layout;
    }

    public static BannerLayout register(ResourceKey<CreativeModeTab> tab, Consumer<BannerLayout.Builder> spec) {
        return register(tab, BannerLayout.build(spec));
    }

    public static Optional<BannerLayout> unregister(ResourceKey<CreativeModeTab> tab) {
        BannerLayout removed = LAYOUTS.remove(Objects.requireNonNull(tab, "tab"));
        RESOLVED.clear();
        return Optional.ofNullable(removed);
    }

    public static Optional<BannerLayout> layout(ResourceKey<CreativeModeTab> tab) {
        return Optional.ofNullable(LAYOUTS.get(Objects.requireNonNull(tab, "tab")));
    }

    public static Optional<BannerLayout> layoutOf(CreativeModeTab tab) {
        if (tab == null) {
            return Optional.empty();
        }

        BannerLayout cached = RESOLVED.get(tab);
        if (cached != null) {
            return Optional.of(cached);
        }

        for (Map.Entry<ResourceKey<CreativeModeTab>, BannerLayout> entry : LAYOUTS.entrySet()) {
            if (BuiltInRegistries.CREATIVE_MODE_TAB.getValue(entry.getKey()) == tab) {
                RESOLVED.put(tab, entry.getValue());
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    public static boolean isDecorated(ResourceKey<CreativeModeTab> tab) {
        return LAYOUTS.containsKey(Objects.requireNonNull(tab, "tab"));
    }

    public static Set<ResourceKey<CreativeModeTab>> decoratedTabs() {
        return Collections.unmodifiableSet(LAYOUTS.keySet());
    }

    public static CreativeModeTab.DisplayItemsGenerator displayItems(ResourceKey<CreativeModeTab> tab) {
        Objects.requireNonNull(tab, "tab");
        return (parameters, output) -> layout(tab).ifPresent(layout -> layout.appendTo(output));
    }

    private InventoryBanners() {
    }
}
