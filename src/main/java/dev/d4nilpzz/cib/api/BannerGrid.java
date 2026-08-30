package dev.d4nilpzz.cib.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

public record BannerGrid(List<ItemStack> cells, Map<Integer, Banner> banners) {

    public static final BannerGrid EMPTY = new BannerGrid(List.of(), Map.of());

    public BannerGrid {
        cells = List.copyOf(Objects.requireNonNull(cells, "cells"));
        banners = Map.copyOf(Objects.requireNonNull(banners, "banners"));
    }

    public int rows() {
        return this.cells.size() / Banners.COLUMNS;
    }

    public boolean isEmpty() {
        return this.cells.isEmpty();
    }

    public Optional<Banner> bannerAt(int row) {
        return Optional.ofNullable(this.banners.get(row));
    }

    public List<ItemStack> copyCells() {
        return this.cells.stream().map(ItemStack::copy).toList();
    }
}
