package dev.d4nilpzz.cib.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class BannerLayout {

    public static final BannerLayout EMPTY = new BannerLayout(List.of());

    private final List<BannerSection> sections;
    private volatile BannerGrid grid;

    public BannerLayout(Collection<BannerSection> sections) {
        this.sections = List.copyOf(Objects.requireNonNull(sections, "sections"));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static BannerLayout of(BannerSection... sections) {
        return new BannerLayout(Arrays.asList(sections));
    }

    public static BannerLayout build(Consumer<Builder> spec) {
        Builder builder = builder();
        Objects.requireNonNull(spec, "spec").accept(builder);
        return builder.build();
    }

    public List<BannerSection> sections() {
        return this.sections;
    }

    public boolean isEmpty() {
        return this.sections.isEmpty();
    }

    public Stream<ItemStack> stacks() {
        return this.sections.stream().flatMap(BannerSection::stacks);
    }

    public void appendTo(CreativeModeTab.Output output) {
        Objects.requireNonNull(output, "output");
        this.stacks().forEach(output::accept);
    }

    public BannerGrid grid() {
        BannerGrid current = this.grid;
        if (current == null) {
            synchronized (this) {
                current = this.grid;
                if (current == null) {
                    current = this.buildGrid();
                    this.grid = current;
                }
            }
        }
        return current;
    }

    private BannerGrid buildGrid() {
        if (this.sections.isEmpty()) {
            return BannerGrid.EMPTY;
        }

        List<ItemStack> cells = new ArrayList<>();
        Map<Integer, Banner> banners = new HashMap<>();

        for (BannerSection section : this.sections) {
            banners.put(cells.size() / Banners.COLUMNS, section.banner());
            for (int column = 0; column < Banners.COLUMNS; column++) {
                cells.add(ItemStack.EMPTY);
            }

            section.stacks().forEach(cells::add);

            while (cells.size() % Banners.COLUMNS != 0) {
                cells.add(ItemStack.EMPTY);
            }
        }

        return new BannerGrid(cells, banners);
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof BannerLayout layout && this.sections.equals(layout.sections);
    }

    @Override
    public int hashCode() {
        return this.sections.hashCode();
    }

    @Override
    public String toString() {
        return "BannerLayout" + this.sections;
    }

    public static final class Builder {

        private final List<BannerSection> sections = new ArrayList<>();
        private BannerSection.Builder open;

        private Builder() {
        }

        public Builder section(BannerSection section) {
            this.seal();
            this.sections.add(Objects.requireNonNull(section, "section"));
            return this;
        }

        public Builder section(Consumer<BannerSection.Builder> spec) {
            BannerSection.Builder builder = BannerSection.builder();
            Objects.requireNonNull(spec, "spec").accept(builder);
            return this.section(builder.build());
        }

        public Builder section(Banner banner, Consumer<BannerSection.Builder> spec) {
            return this.section(builder -> spec.accept(builder.banner(banner)));
        }

        public Builder section(Identifier banner, Consumer<BannerSection.Builder> spec) {
            return this.section(builder -> spec.accept(builder.banner(banner)));
        }

        public Builder section(Banner banner) {
            this.seal();
            this.open = BannerSection.builder().banner(banner);
            return this;
        }

        public Builder section(Identifier banner) {
            return this.section(banner == null ? Banner.DEFAULT : Banner.of(banner));
        }

        public Builder section(String namespace, String name) {
            return this.section(Banner.of(namespace, name));
        }

        public Builder animated(String namespace, String name, int frames) {
            return this.section(Banner.animated(namespace, name, frames));
        }

        public Builder animated(String namespace, String name, int frames, int frameTime) {
            return this.section(Banner.animated(namespace, name, frames, frameTime));
        }

        public Builder animated(Identifier banner, int frames) {
            return this.section(Banner.animated(banner, frames));
        }

        public Builder animated(Identifier banner, int frames, int frameTime) {
            return this.section(Banner.animated(banner, frames, frameTime));
        }

        public Builder add(ItemLike... items) {
            this.open().add(items);
            return this;
        }

        public Builder add(ItemStack... stacks) {
            this.open().add(stacks);
            return this;
        }

        public Builder addAll(Collection<? extends ItemLike> items) {
            this.open().addAll(items);
            return this;
        }

        public Builder addLazy(Supplier<ItemStack> stack) {
            this.open().addLazy(stack);
            return this;
        }

        public Builder sections(Collection<BannerSection> sections) {
            sections.forEach(this::section);
            return this;
        }

        public Builder apply(Consumer<Builder> action) {
            action.accept(this);
            return this;
        }

        public BannerLayout build() {
            this.seal();
            return this.sections.isEmpty() ? EMPTY : new BannerLayout(this.sections);
        }

        private BannerSection.Builder open() {
            if (this.open == null) {
                this.open = BannerSection.builder();
            }
            return this.open;
        }

        private void seal() {
            if (this.open != null && !this.open.isEmpty()) {
                this.sections.add(this.open.build());
            }
            this.open = null;
        }
    }
}
