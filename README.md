# Cib Lib

A Fabric library that lets any mod split its creative tab into **sections**, each one headed by its
own banner.

A section takes one full row of the creative grid for its header and lays its items underneath.
Rendering happens in two layers:

```
inventory background  ->  placeholder (162x18, always)  ->  section banner (160x16)
```

The **placeholder** is always drawn by the library — it is the frame that covers the empty slots —
and sits below the banner. The **banner** is yours; if you do not declare one, `default.png` is used.

A banner may also be **animated**: give it a texture with its frames stacked vertically and it either
cycles on its own or only while the pointer is over it. See [Animated banners](#animated-banners).

---

## Requirements

| | |
|---|---|
| Minecraft | `26.2` |
| Fabric Loader | `>=0.19.3` |
| Fabric API | yes |
| Java | 25 |

---

## Installation

Releases live on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/creative-inventory-banner).
Pull them into your build through [Cursemaven](https://cursemaven.com), the community Maven proxy for
CurseForge files:

```groovy
repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Cursemaven"
                url = "https://cursemaven.com"
            }
        }
        filter {
            includeGroup "curse.maven"
        }
    }
}

dependencies {
    implementation "curse.maven:creative-inventory-banner-1642452:FILE_ID"
}
```

`1642452` is this project's CurseForge id. Replace **`FILE_ID`** with the file you want to build
against: open the *Files* tab, pick a release, and take the trailing number from its URL
(`.../files/1234567`). Cursemaven pins an exact file rather than a version range, so bumping the
library means bumping that number. The `creative-inventory-banner` part is only a readable label —
the artifact is resolved from the numbers alone.

> **It is `implementation`, not `modImplementation`.** Minecraft 26.2 ships unobfuscated, so Loom
> does not create the `modXxx` configurations and mod jars go straight on the normal ones. Using
> `modImplementation` fails at evaluation time with
> `Could not find method modImplementation()`.

And in your `fabric.mod.json`:

```json
"depends": {
  "cib": ">=1.0.0"
}
```

### Building from source

To test against unreleased changes, clone this repository and publish it locally:

```bash
./gradlew publishToMavenLocal
```

Then add `mavenLocal()` to your repositories and depend on `dev.d4nilpzz:cib:1.0.0` instead.

---

## Quick start

Two steps: declare the layout and hand it to your tab's `displayItems`. A complete mod drawing three
sections fits in one class:

```java
package com.example.examplemod;

import dev.d4nilpzz.cib.api.Banner;
import dev.d4nilpzz.cib.api.InventoryBanners;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ExampleMod implements ModInitializer {

    public static final String MOD_ID = "examplemod";

    public static final ResourceKey<CreativeModeTab> TAB =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("example"));

    public static final Item RUBY = item("ruby");
    public static final Item RUBY_SWORD = item("ruby_sword");
    public static final Item RUBY_PIE = item("ruby_pie");

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        InventoryBanners.register(TAB, layout -> layout
                .section(section -> section
                        .banner(MOD_ID, "materials")
                        .add(RUBY))
                .section(section -> section
                        .banner(MOD_ID, "gear")
                        .add(RUBY_SWORD))
                .section(section -> section
                        .banner(Banner.ANIMATED_DEFAULT)
                        .add(RUBY_PIE)));

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB, FabricCreativeModeTab.builder()
                .title(Component.translatable("itemGroup." + MOD_ID + ".example"))
                .icon(() -> new ItemStack(RUBY))
                .displayItems(InventoryBanners.displayItems(TAB))
                .build());
    }

    private static Item item(String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, new Item(new Item.Properties().setId(key)));
    }
}
```

The first two sections need their textures at
`assets/examplemod/textures/gui/banner/{materials,gear}.png`. The food one takes `Banner.ANIMATED_DEFAULT`,
the library's own `animated_default.png` — four frames that cycle by themselves, no texture of your
own required. Dropping the `.banner(...)` line altogether would have given it the static
`default.png` instead.

`InventoryBanners.displayItems(TAB)` returns a `CreativeModeTab.DisplayItemsGenerator` that resolves
the layout lazily, so the order in which you register the two things does not matter.

### When sections may be declared

A section stores **suppliers** of `ItemStack`, not the stacks themselves: `add(ItemLike...)` and
`addAll(...)` do not call `new ItemStack(...)` until the layout is actually used — when the creative
screen opens or when the tab contents are generated.

That is what lets you declare sections inside your item class's own `<clinit>`, while you register
the items. Building the stacks at that point would blow up with
`NullPointerException: Components not bound yet`, because item components are not bound yet during
startup.

The trade-off: the items you pass must already be registered by the time the layout is consumed. In
practice they always are.

---

## Textures

Put your banners in:

```
assets/<your_mod_id>/textures/gui/banner/<name>.png
```

and refer to them with `.banner("<your_mod_id>", "<name>")`.

> **A banner must be exactly 160x16 px**, like the library's `default.png` — or 160x`16*frames` if
> it is animated, like the 160x64 `animated_default.png`. Any other size gets stretched when drawn.
> The placeholder (162x18) is supplied by the library and is not yours to change.

If you prefer an arbitrary path, use the `Identifier` overload:

```java
.banner(Identifier.fromNamespaceAndPath("mymod", "textures/other/path.png"))
```

---

## Animated banners

An animated banner is **one** png with its frames stacked vertically, each frame 160x16. Four frames
is a 160x64 file — the shape of the library's `animated_default.png`. There is no `.mcmeta`
involved: you say how many frames the sheet holds.

```java
.section(section -> section
        .animated(MOD_ID, "portal", 8)
        .add(PORTAL_BLOCK))
```

Frames advance every `frameTime` ticks and loop forever.

### `always_animated`

Whether the frames run on their own or only under the pointer:

```java
// always: cycles the whole time the section is on screen (the default for animated banners)
.banner(Banner.animated(MOD_ID, "portal", 8).alwaysAnimated(true))

// on hover: only plays while the mouse is over the banner
.banner(Banner.animated(MOD_ID, "portal", 8).alwaysAnimated(false))
.banner(Banner.animated(MOD_ID, "portal", 8).onHover())   // same thing, shorter
```

A hover banner **keeps the frame it stopped at** when the pointer leaves, and carries on from there
the next time you go over it — it pauses, it does not rewind. Each row remembers its own progress,
so two hover banners on screen sit wherever you each left them; the memory lasts as long as the tab
does. Always-animated banners share one clock instead, so several of them stay in step with each
other.

### Speed

`frameTime` is how many **ticks** each frame is held — 50 ms apiece, so lower is faster. It defaults
to `2` (100 ms per frame) and `1` is a frame per tick, as fast as the game draws. Either pass it to
the shorthand or set it on the `Banner`:

```java
.animated(MOD_ID, "portal", 8, 4)                        // 8 frames, 4 ticks each = 200 ms
.banner(Banner.animated(MOD_ID, "portal", 8, 1))         // one frame per tick
.banner(Banner.animated(MOD_ID, "portal", 8).frameTime(4).onHover())
```

`frames`, `frameTime`, `alwaysAnimated` and `onHover` are all withers on the record, so they chain in
any order and each one hands you a new `Banner`.

A `Banner` with a single frame is simply static, so `Banner.of(...)`, `.banner(id)` and
`.banner(namespace, name)` behave exactly as before.

---

## API

### `InventoryBanners`

| Method | What it does |
|---|---|
| `register(ResourceKey<CreativeModeTab>, Consumer<BannerLayout.Builder>)` | Registers a layout using the builder. |
| `register(ResourceKey<CreativeModeTab>, BannerLayout)` | Registers an already built layout. |
| `unregister(ResourceKey<CreativeModeTab>)` | Drops the layout, returns the previous one. |
| `layout(ResourceKey<CreativeModeTab>)` | `Optional<BannerLayout>` by key. |
| `layoutOf(CreativeModeTab)` | `Optional<BannerLayout>` by tab instance. |
| `isDecorated(ResourceKey<CreativeModeTab>)` | Whether that tab has a layout. |
| `decoratedTabs()` | Immutable view of every registered tab. |
| `displayItems(ResourceKey<CreativeModeTab>)` | `DisplayItemsGenerator` ready for `CreativeModeTab.Builder`. |

### `BannerLayout.Builder`

| Method | What it does |
|---|---|
| `section(Consumer<BannerSection.Builder>)` | Adds a section using the builder. |
| `section(BannerSection)` | Adds an already built section. |
| `section(Banner, Consumer<BannerSection.Builder>)` | Adds a section with the banner pinned. |
| `section(Identifier, Consumer<BannerSection.Builder>)` | Same, from a plain texture. |
| `section(Banner)` / `section(Identifier)` / `section(String, String)` | Opens a section: the `add`s that follow go into it. |
| `animated(String, String, int frames[, int frameTime])` | Opens a section under an animated banner. |
| `animated(Identifier, int frames[, int frameTime])` | Same, with a full path. |
| `add(ItemLike...)`, `add(ItemStack...)`, `addAll(...)`, `addLazy(...)` | Adds to the open section, opening a default one if there is none. |
| `sections(Collection<BannerSection>)` | Adds several at once. |
| `apply(Consumer<Builder>)` | Conditional block without breaking the chain. |

### `BannerSection.Builder`

| Method | What it does |
|---|---|
| `banner(String namespace, String name)` | Banner from `textures/gui/banner/<name>.png`. |
| `banner(Identifier)` | Banner with a full path; `null` falls back to the default. |
| `banner(Banner)` | An already built banner, animation included. |
| `animated(String namespace, String name, int frames)` | Animated banner from `textures/gui/banner/<name>.png`. |
| `animated(Identifier, int frames)` | Animated banner with a full path. |
| `animated(..., int frames, int frameTime)` | Either of the two, with the frame time in ticks. |
| `add(ItemLike...)` | Adds items as stacks of one. |
| `add(ItemStack...)` | Adds preconfigured stacks (enchantments, components...). |
| `addAll(Collection<? extends ItemLike>)` | Adds a collection, preserving order. |
| `addLazy(Supplier<ItemStack>)` | Adds a stack built on demand. |
| `apply(Consumer<Builder>)` | Conditional block without breaking the chain. |

### `Banner`

`record Banner(Identifier texture, int frames, int frameTime, boolean alwaysAnimated)`.

| Member | What it does |
|---|---|
| `Banner.of(Identifier)` / `of(String, String)` | Static banner, one frame. |
| `Banner.animated(Identifier, int frames)` / `animated(String, String, int)` | Animated banner, `alwaysAnimated` on. |
| `Banner.animated(..., int frames, int frameTime)` | The same, with the frame time in ticks. |
| `Banner.DEFAULT` | The library's `default.png`. |
| `Banner.ANIMATED_DEFAULT` | The library's `animated_default.png`, 4 frames, always animated. |
| `frames(int)`, `frameTime(int)`, `alwaysAnimated(boolean)`, `onHover()` | Withers; each returns a new `Banner`. |
| `Banner.DEFAULT_FRAME_TIME` | The 2 ticks a frame is held when you do not say. |
| `isAnimated()` | Whether it holds more than one frame. |
| `textureHeight()`, `frameV(int)`, `frameAt(long millis)`, `duration()` | What the renderer uses to pick and place a frame. |

Records are immutable, so the constants are safe to reuse and to hand to several sections.

### `Banners`

Constants: `WIDTH` (160), `HEIGHT` (16), `COLUMNS` (9), `FOLDER`, `DEFAULT`, `ANIMATED_DEFAULT`,
`ANIMATED_DEFAULT_FRAMES` (4), `PLACEHOLDER`.
Helpers: `texture(String namespace, String name)` and `texture(Identifier shortId)`.

### `BannerGrid`

The laid-out result, in case you need it: `cells()`, `banners()` (row -> `Banner`), `rows()`,
`bannerAt(int)`, `copyCells()`, `isEmpty()`. Computed lazily and cached by `BannerLayout.grid()`.

---

## Examples

### Conditional sections

```java
InventoryBanners.register(TAB_KEY, layout -> layout
        .section(section -> section.banner(MOD_ID, "basics").add(PICKAXE, AXE))
        .apply(l -> {
            if (FabricLoader.getInstance().isModLoaded("other_mod")) {
                l.section(section -> section.banner(MOD_ID, "compat").add(COMPAT_ITEM));
            }
        }));
```

### Stacks with components

```java
ItemStack sharpBlade = new ItemStack(BLADE);
sharpBlade.enchant(...);

InventoryBanners.register(TAB_KEY, layout -> layout
        .section(section -> section
                .banner(MOD_ID, "gear")
                .add(sharpBlade)
                .add(SHIELD, HELMET)));
```

### Sections from declaration order

With dozens of items, repeating their names in the layout is a source of omissions. Since field
initialisers and `static` blocks run in textual order, you can mark the sections right there and let
the creative order be, literally, the order in which you declare the fields:

```java
public final class ModItems {
    private static final BannerLayout.Builder LAYOUT = BannerLayout.builder();

    static { LAYOUT.section(MOD_ID, "materials"); }

    public static final Item RUBY = item("ruby");
    public static final Item SAPPHIRE = item("sapphire");

    static { LAYOUT.section(MOD_ID, "gear"); }

    public static final Item RUBY_SWORD = item("ruby_sword");
    public static final Item RUBY_PICKAXE = item("ruby_pickaxe");

    static { LAYOUT.animated(MOD_ID, "relics", 6); }

    public static final Item RUBY_TOTEM = item("ruby_totem");

    public static void register() {
        InventoryBanners.register(MY_TAB, LAYOUT.build());
    }

    private static Item item(String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, MyMod.id(name));
        Item registered = Registry.register(BuiltInRegistries.ITEM, key,
                new Item(new Item.Properties().setId(key)));
        LAYOUT.add(registered);
        return registered;
    }
}
```

No lists, no bookkeeping of your own: a `section(...)` with just a banner leaves that section **open**
and every `LAYOUT.add(...)` after it drops into it, until the next marker or `build()` closes it.
Adding an item is one more line; moving it to another section is moving it above or below a marker.
There is no `sealSection()` at the end either — `build()` closes whatever is still open, and an open
section that never got an item is dropped.

A marker can be static (`section(MOD_ID, "gear")`), animated (`animated(MOD_ID, "relics", 6)`),
animated at your own speed (`animated(MOD_ID, "relics", 6, 4)`) or any `Banner` you build
(`section(Banner.ANIMATED_DEFAULT.onHover())`).

The two styles mix, so a layout can be part markers and part whole sections — the open one is closed
first, and the order you wrote is the order you get:

```java
LAYOUT.section(BannerSection.of(Banners.texture(MOD_ID, "compat"), COMPAT_ITEM));
```

### Reusing a layout

```java
BannerLayout shared = BannerLayout.build(l -> l
        .section(BannerSection.of(Banners.texture(MOD_ID, "tools"), PICKAXE, AXE))
        .section(BannerSection.of(DIRT, STONE)));

InventoryBanners.register(TAB_ONE, shared);
InventoryBanners.register(TAB_TWO, shared);
```

---

## How it works

- `BannerLayout.grid()` builds the grid: for each section, a row of 9 empty cells (where the banner
  goes) followed by the items, padded to a full row.
- Three mixins on `CreativeModeInventoryScreen`, in the client source set:
  - `refreshCurrentTabContents` and `selectTab` swap the item collection for the grid.
  - `extractRenderState` draws, for each of the 5 visible rows, the placeholder and the matching
    banner on top of it. An animated banner is blitted with a v offset of `frame * 16` out of a
    sheet `16 * frames` tall.
- The frame of an always-animated banner comes straight from a shared monotonic clock. A hover
  animated one is kept by `HoverAnimations`, which stores how far each row got: the hovered row
  accumulates while it is hovered and the number is frozen when the pointer leaves, which is what
  holds the last frame and lets the next hover resume.
- Tabs with no registered layout are left alone: the library is inert for the rest of the game.

---

## License

All Rights Reserved.
