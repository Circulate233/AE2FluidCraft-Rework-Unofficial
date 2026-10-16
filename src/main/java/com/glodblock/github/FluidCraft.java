package com.glodblock.github;

import com.glodblock.github.ae2fc.Tags;
import com.glodblock.github.proxy.CommonProxy;
import com.glodblock.github.util.ModAndClassUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = FluidCraft.MODID, version = FluidCraft.VERSION, useMetadata = true,
    dependencies = "required-after:appliedenergistics2@[v0.56.5,);" +
        "required-after:mixinbooter@[8.0,);"
)
public class FluidCraft {

    public static final String MODID = Tags.MOD_ID;
    public static final String VERSION = Tags.VERSION;
    public static final boolean beta = false;

    @Mod.Instance(MODID)
    public static FluidCraft INSTANCE;

    @SidedProxy(clientSide = "com.glodblock.github.proxy.ClientProxy", serverSide = "com.glodblock.github.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static Logger log;

    @Mod.EventHandler
    public void onPreInit(final FMLPreInitializationEvent event) {
        log = event.getModLog();
        ModAndClassUtil.init();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void onInit(final FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void onPostInit(final FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    public static ResourceLocation resource(final String path) {
        return new ResourceLocation(MODID, path);
    }

}
