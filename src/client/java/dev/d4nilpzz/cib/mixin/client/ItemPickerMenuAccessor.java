package dev.d4nilpzz.cib.mixin.client;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CreativeModeInventoryScreen.ItemPickerMenu.class)
public interface ItemPickerMenuAccessor {

    @Invoker("getRowIndexForScroll")
    int cib$getRowIndexForScroll(float scrollOffs);
}
