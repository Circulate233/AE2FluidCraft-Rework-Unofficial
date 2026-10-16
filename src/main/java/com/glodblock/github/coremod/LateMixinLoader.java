package com.glodblock.github.coremod;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import zone.rong.mixinbooter.ILateMixinLoader;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

@SuppressWarnings("unused")
public class LateMixinLoader implements ILateMixinLoader {

    private static final Map<String, BooleanSupplier> MIXIN_CONFIGS = new Object2ObjectLinkedOpenHashMap<>();

    static {
        addMixinCFG("mixins.ae2fc.json");
        addMixinCFG("mixins.ae2fc.ae2.old.json", () -> !isNewAE2());
        addMixinCFG("mixins.ae2fc.ae2.new.json", LateMixinLoader::isNewAE2);
        addMixinCFG("mixins.ae2fc.packagedauto.json", () -> Loader.isModLoaded("packagedauto"));
        addMixinCFG("mixins.ae2fc.jei.json", () -> Loader.isModLoaded("jei"));
        addMixinCFG("mixins.ae2fc.wct.json", () -> Loader.isModLoaded("wct"));
        addMixinCFG("mixins.ae2fc.ae2ctl.json", () -> Loader.isModLoaded("ae2ctl"));
        addMixinCFG("mixins.ae2fc.mmce.hasGT.json", () -> isClassPresent("hellfirepvp.modularmachinery.common.base.Mods$3"));
        addMixinCFG("mixins.ae2fc.mmce.noGT.json", () -> !isClassPresent("hellfirepvp.modularmachinery.common.base.Mods$3") && isClassPresent("hellfirepvp.modularmachinery.common.crafting.component.ComponentItemFluid"));
        addMixinCFG("mixins.ae2fc.thaumicenergistics.json", () -> isClassPresent("thaumicenergistics.client.gui.part.GuiArcaneTerminal"));
    }

    private static void addMixinCFG(final String mixinConfig) {
        addMixinCFG(mixinConfig, () -> true);
    }

    private static void addMixinCFG(final String mixinConfig, @Nonnull final BooleanSupplier conditions) {
        MIXIN_CONFIGS.put(mixinConfig, conditions);
    }

    @Override
    public List<String> getMixinConfigs() {
        return new ObjectArrayList<>(MIXIN_CONFIGS.keySet());
    }

    @Override
    public boolean shouldMixinConfigQueue(final String mixinConfig) {
        return MIXIN_CONFIGS.get(mixinConfig).getAsBoolean();
    }

    private static boolean isNewAE2() {
        final String version = Loader.instance().getIndexedModList().get("appliedenergistics2").getVersion();
        final String normalizedVersion = version.replaceFirst("^[vV]", "");
        // 0.56.7 added crafting inventories before the sticky and regular inventory passes.
        return new DefaultArtifactVersion(normalizedVersion).compareTo(new DefaultArtifactVersion("0.56.7")) >= 0;
    }

    private static boolean isClassPresent(final String className) {
        final String classFilePath = className.replace('.', '/') + ".class";
        final ClassLoader classLoader = LateMixinLoader.class.getClassLoader();
        return classLoader.getResource(classFilePath) != null;
    }
}
