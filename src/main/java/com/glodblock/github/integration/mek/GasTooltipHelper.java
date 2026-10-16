package com.glodblock.github.integration.mek;

import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.util.UtilClient;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;

import java.util.Collections;

public class GasTooltipHelper {

    @Optional.Method(modid = "mekeng")
    public static boolean handleGasTooltip(GuiContainer gui, ItemStack item, int mouseX, int mouseY) {
        if (item.getItem() == FCGasItems.GAS_DROP) {
            Object gs = FakeItemRegister.getStack(item);
            if (gs != null) {
                gui.drawHoveringText(Collections.singletonList(UtilClient.getGasDisplayName(gs)), mouseX, mouseY);
                return true;
            }
        }
        return false;
    }
}
