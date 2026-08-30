package dev.d4nilpzz.cib.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.d4nilpzz.cib.api.Banner;
import dev.d4nilpzz.cib.api.BannerGrid;
import dev.d4nilpzz.cib.api.BannerLayout;
import dev.d4nilpzz.cib.api.Banners;
import dev.d4nilpzz.cib.api.InventoryBanners;
import dev.d4nilpzz.cib.client.HoverAnimations;
import java.util.Collection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryBannerMixin {

    private static final int GRID_X = 9;
    private static final int GRID_Y = 18;
    private static final int SLOT_SIZE = 18;
    private static final int PLACEHOLDER_BLEED = 1;
    private static final int PLACEHOLDER_WIDTH = Banners.WIDTH + PLACEHOLDER_BLEED * 2;
    private static final int PLACEHOLDER_HEIGHT = Banners.HEIGHT + PLACEHOLDER_BLEED * 2;
    private static final int VISIBLE_ROWS = 5;

    @Shadow
    private static CreativeModeTab selectedTab;

    @Shadow
    private float scrollOffs;

    @Unique
    private CreativeModeTab cib$resolvedFor;

    @Unique
    private BannerGrid cib$grid;

    @Unique
    private final HoverAnimations cib$hovered = new HoverAnimations();

    @Unique
    private BannerGrid cib$grid() {
        CreativeModeTab tab = selectedTab;
        if (tab == null) {
            return BannerGrid.EMPTY;
        }
        if (tab != this.cib$resolvedFor) {
            this.cib$resolvedFor = tab;
            this.cib$grid = InventoryBanners.layoutOf(tab)
                    .map(BannerLayout::grid)
                    .orElse(BannerGrid.EMPTY);
            this.cib$hovered.clear();
        }
        return this.cib$grid;
    }

    @ModifyVariable(method = "refreshCurrentTabContents", at = @At("HEAD"), argsOnly = true)
    private Collection<ItemStack> cib$layOutOnRefresh(Collection<ItemStack> original) {
        BannerGrid grid = this.cib$grid();
        return grid.isEmpty() ? original : grid.copyCells();
    }

    @ModifyExpressionValue(
            method = "selectTab",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/CreativeModeTab;getDisplayItems()Ljava/util/Collection;"))
    private Collection<ItemStack> cib$layOutOnSelect(Collection<ItemStack> original) {
        BannerGrid grid = this.cib$grid();
        return grid.isEmpty() ? original : grid.copyCells();
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void cib$drawBanners(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                 float partialTick, CallbackInfo ci) {
        BannerGrid grid = this.cib$grid();
        if (grid.isEmpty()) {
            return;
        }

        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;
        int topRow = ((ItemPickerMenuAccessor) screen.getMenu())
                .cib$getRowIndexForScroll(this.scrollOffs);

        int left = ((AbstractContainerScreenAccessor) this).cib$getLeftPos();
        int top = ((AbstractContainerScreenAccessor) this).cib$getTopPos();
        long now = cib$now();
        int x = left + GRID_X;

        this.cib$hovered.hover(this.cib$rowAt(grid, topRow, top, mouseX, mouseY, x), now);

        for (int visibleRow = 0; visibleRow < VISIBLE_ROWS; visibleRow++) {
            int row = topRow + visibleRow;
            Banner banner = grid.banners().get(row);
            if (banner == null) {
                continue;
            }

            int y = top + GRID_Y + visibleRow * SLOT_SIZE;

            graphics.blit(RenderPipelines.GUI_TEXTURED, Banners.PLACEHOLDER,
                    x - PLACEHOLDER_BLEED, y - PLACEHOLDER_BLEED,
                    0.0F, 0.0F,
                    PLACEHOLDER_WIDTH, PLACEHOLDER_HEIGHT,
                    PLACEHOLDER_WIDTH, PLACEHOLDER_HEIGHT);

            graphics.blit(RenderPipelines.GUI_TEXTURED, banner.texture(),
                    x, y,
                    0.0F, banner.frameV(this.cib$frame(banner, row, now)),
                    Banners.WIDTH, Banners.HEIGHT,
                    Banners.WIDTH, banner.textureHeight());
        }
    }

    @Unique
    private int cib$frame(Banner banner, int row, long now) {
        if (!banner.isAnimated()) {
            return 0;
        }
        return banner.alwaysAnimated()
                ? banner.frameAt(now)
                : this.cib$hovered.frame(row, banner, now);
    }

    @Unique
    private int cib$rowAt(BannerGrid grid, int topRow, int top, int mouseX, int mouseY, int x) {
        if (mouseX < x || mouseX >= x + Banners.WIDTH) {
            return -1;
        }

        for (int visibleRow = 0; visibleRow < VISIBLE_ROWS; visibleRow++) {
            int row = topRow + visibleRow;
            if (!grid.banners().containsKey(row)) {
                continue;
            }

            int y = top + GRID_Y + visibleRow * SLOT_SIZE;
            if (mouseY >= y && mouseY < y + Banners.HEIGHT) {
                return row;
            }
        }
        return -1;
    }

    @Unique
    private static long cib$now() {
        return System.nanoTime() / 1_000_000L;
    }
}
