package dev.d4nilpzz.cib.api;

import dev.d4nilpzz.cib.Cib;
import java.util.Objects;
import net.minecraft.resources.Identifier;

public final class Banners {

    public static final int WIDTH = 160;

    public static final int HEIGHT = 16;

    public static final int COLUMNS = 9;

    public static final String FOLDER = "textures/gui/banner";

    public static final Identifier PLACEHOLDER = Cib.id(FOLDER + "/placeholder.png");

    public static final Identifier DEFAULT = Cib.id(FOLDER + "/default.png");

    public static final Identifier ANIMATED_DEFAULT = Cib.id(FOLDER + "/animated_default.png");

    public static final int ANIMATED_DEFAULT_FRAMES = 4;

    public static Identifier texture(String namespace, String name) {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(name, "name");
        return Identifier.fromNamespaceAndPath(namespace, FOLDER + "/" + name + ".png");
    }

    public static Identifier texture(Identifier shortId) {
        Objects.requireNonNull(shortId, "shortId");
        return texture(shortId.getNamespace(), shortId.getPath());
    }

    private Banners() {
    }
}
