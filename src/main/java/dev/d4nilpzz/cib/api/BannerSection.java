package dev.d4nilpzz.cib.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record BannerSection(Banner banner, List<Supplier<ItemStack>> entries) {

    public BannerSection {
        banner = banner == null ? Banner.DEFAULT : banner;
        entries = List.copyOf(Objects.requireNonNull(entries, "entries"));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static BannerSection of(Banner banner, ItemLike... items) {
        return builder().banner(banner).add(items).build();
    }

    public static BannerSection of(Identifier banner, ItemLike... items) {
        return builder().banner(banner).add(items).build();
    }

    public static BannerSection of(ItemLike... items) {
        return builder().add(items).build();
    }

    public int size() {
        return this.entries.size();
    }

    public int rows() {
        return 1 + Math.ceilDiv(this.entries.size(), Banners.COLUMNS);
    }

    public Stream<ItemStack> stacks() {
        return this.entries.stream().map(Supplier::get);
    }

    public static final class Builder {

        private final List<Supplier<ItemStack>> entries = new ArrayList<>();
        private Banner banner = Banner.DEFAULT;

        private Builder() {
        }

        public Builder banner(Banner banner) {
            this.banner = banner == null ? Banner.DEFAULT : banner;
            return this;
        }

        public Builder banner(Identifier banner) {
            return this.banner(banner == null ? Banner.DEFAULT : Banner.of(banner));
        }

        public Builder banner(String namespace, String name) {
            return this.banner(Banner.of(namespace, name));
        }

        public Builder animated(String namespace, String name, int frames) {
            return this.banner(Banner.animated(namespace, name, frames));
        }

        public Builder animated(Identifier banner, int frames) {
            return this.banner(Banner.animated(banner, frames));
        }

        public Builder animated(String namespace, String name, int frames, int frameTime) {
            return this.banner(Banner.animated(namespace, name, frames, frameTime));
        }

        public Builder animated(Identifier banner, int frames, int frameTime) {
            return this.banner(Banner.animated(banner, frames, frameTime));
        }

        public Builder add(ItemLike... items) {
            return this.addAll(Arrays.asList(items));
        }

        public Builder add(ItemStack... stacks) {
            for (ItemStack stack : stacks) {
                ItemStack snapshot = Objects.requireNonNull(stack, "stack").copy();
                this.entries.add(snapshot::copy);
            }
            return this;
        }

        public Builder addAll(Collection<? extends ItemLike> items) {
            for (ItemLike item : items) {
                Objects.requireNonNull(item, "item");
                this.entries.add(() -> new ItemStack(item));
            }
            return this;
        }

        public Builder addLazy(Supplier<ItemStack> stack) {
            this.entries.add(Objects.requireNonNull(stack, "stack"));
            return this;
        }

        public Builder apply(Consumer<Builder> action) {
            action.accept(this);
            return this;
        }

        public boolean isEmpty() {
            return this.entries.isEmpty();
        }

        public BannerSection build() {
            return new BannerSection(this.banner, this.entries);
        }
    }
}
