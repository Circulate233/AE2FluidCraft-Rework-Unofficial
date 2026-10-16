package com.glodblock.github.coremod.mixin.ae2.gui;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiCraftAmount;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.GasTooltipHelper;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.UtilClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;

@Mixin(AEBaseGui.class)
public abstract class MixinAEBaseGui {

    @WrapOperation(method = {"mouseClickMove", "handleMouseClick"}, at = @At(value = "NEW", target = "(Lappeng/helpers/InventoryAction;IJ)Lappeng/core/sync/packets/PacketInventoryAction;",ordinal = 0, remap = false))
    protected PacketInventoryAction writeMouseButton(final InventoryAction action, final int slot, final long id, final Operation<PacketInventoryAction> original) {
        return original.call(action, slot, action == InventoryAction.PICKUP_OR_SET_DOWN ? 0L : 1L);
    }

    @WrapOperation(
            method = "drawScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/gui/AEBaseGui;renderHoveredToolTip(II)V"
            )
    )
    private void wrapRenderHoveredToolTip(AEBaseGui instance, int mouseX, int mouseY, Operation<Void> original) {
        if (instance instanceof GuiCraftAmount) {
            Slot slot = instance.getSlotUnderMouse();

            if (UtilClient.getMouseItem().isEmpty() && slot != null && slot.getHasStack()) {
                ItemStack item = slot.getStack();

                if (item.getItem() == FCItems.FLUID_DROP) {
                    IAEFluidStack fluidStack = FakeItemRegister.getAEStack(item);
                    if (fluidStack != null) {
                        instance.drawHoveringText(
                                Collections.singletonList(fluidStack.getFluidStack().getLocalizedName()),
                                mouseX,
                                mouseY
                        );
                        return;
                    }
                }

                if (ModAndClassUtil.GAS) {
                    if (GasTooltipHelper.handleGasTooltip(instance, item, mouseX, mouseY)) {
                        return;
                    }
                }
            }
        }

        original.call(instance, mouseX, mouseY);
    }
}