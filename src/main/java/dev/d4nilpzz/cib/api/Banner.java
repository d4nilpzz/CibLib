package dev.d4nilpzz.cib.api;

import java.util.Objects;
import net.minecraft.resources.Identifier;

public record Banner(Identifier texture, int frames, int frameTime, boolean alwaysAnimated) {

    public static final int DEFAULT_FRAME_TIME = 2;

    public static final Banner DEFAULT = of(Banners.DEFAULT);

    public static final Banner ANIMATED_DEFAULT =
            animated(Banners.ANIMATED_DEFAULT, Banners.ANIMATED_DEFAULT_FRAMES);

    public Banner {
        Objects.requireNonNull(texture, "texture");
        if (frames < 1) {
            throw new IllegalArgumentException("frames must be at least 1, got " + frames);
        }
        if (frameTime < 1) {
            throw new IllegalArgumentException("frameTime must be at least 1 tick, got " + frameTime);
        }
    }

    public static Banner of(Identifier texture) {
        return new Banner(texture, 1, DEFAULT_FRAME_TIME, false);
    }

    public static Banner of(String namespace, String name) {
        return of(Banners.texture(namespace, name));
    }

    public static Banner animated(Identifier texture, int frames) {
        return new Banner(texture, frames, DEFAULT_FRAME_TIME, true);
    }

    public static Banner animated(String namespace, String name, int frames) {
        return animated(Banners.texture(namespace, name), frames);
    }

    public static Banner animated(Identifier texture, int frames, int frameTime) {
        return new Banner(texture, frames, frameTime, true);
    }

    public static Banner animated(String namespace, String name, int frames, int frameTime) {
        return animated(Banners.texture(namespace, name), frames, frameTime);
    }

    public Banner frames(int frames) {
        return new Banner(this.texture, frames, this.frameTime, this.alwaysAnimated);
    }

    public Banner frameTime(int frameTime) {
        return new Banner(this.texture, this.frames, frameTime, this.alwaysAnimated);
    }

    public Banner alwaysAnimated(boolean alwaysAnimated) {
        return new Banner(this.texture, this.frames, this.frameTime, alwaysAnimated);
    }

    public Banner onHover() {
        return this.alwaysAnimated(false);
    }

    public boolean isAnimated() {
        return this.frames > 1;
    }

    public int textureHeight() {
        return Banners.HEIGHT * this.frames;
    }

    public long duration() {
        return (long) this.frames * this.frameTime * 50L;
    }

    public int frameAt(long millis) {
        if (!this.isAnimated()) {
            return 0;
        }
        return (int) Math.floorMod(millis / (this.frameTime * 50L), (long) this.frames);
    }

    public float frameV(int frame) {
        return Math.floorMod(frame, this.frames) * (float) Banners.HEIGHT;
    }
}
