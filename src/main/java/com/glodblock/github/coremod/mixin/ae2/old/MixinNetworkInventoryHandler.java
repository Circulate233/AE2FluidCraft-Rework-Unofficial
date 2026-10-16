package com.glodblock.github.coremod.mixin.ae2.old;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.me.cache.SecurityCache;
import appeng.me.storage.NetworkInventoryHandler;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.interfaces.FCNetworkInventoryHandler;
import com.glodblock.github.interfaces.FCNetworkMonitor;
import com.glodblock.github.util.FakeMonitor;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Deque;
import java.util.List;
import java.util.NavigableMap;

@SuppressWarnings({"DataFlowIssue", "unchecked", "AddedMixinMembersNamePattern"})
@Mixin(value = NetworkInventoryHandler.class, remap = false, priority = 1001)
public abstract class MixinNetworkInventoryHandler<T extends IAEStack<T>> implements FCNetworkInventoryHandler {

    @Unique
    private FakeMonitor<?> fluidMonitor;

    @Unique
    private FakeMonitor<?> gasMonitor;

    @Shadow
    protected abstract void surface(NetworkInventoryHandler<T> networkInventoryHandler, Actionable type);

    @Unique
    private IMEMonitor<IAEItemStack> monitor;

    @Shadow
    protected abstract boolean diveList(NetworkInventoryHandler<T> networkInventoryHandler, Actionable type);

    @Shadow
    @Final
    private NavigableMap<Integer, List<IMEInventoryHandler<T>>> priorityInventory;

    @Shadow
    protected abstract Deque<?> getDepth(Actionable type);

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(final IStorageChannel<?> chan, final SecurityCache security, final CallbackInfo ci) {
        monitor = security.getGrid().<IStorageGrid>getCache(IStorageGrid.class).getInventory(Util.getItemChannel());
    }

    // In 0.56.5, auto-crafting inventories are part of the sticky pass. Convert fluids only after that pass.
    @Inject(method = "injectItems", at = @At(value = "INVOKE", target = "Ljava/util/NavigableMap;values()Ljava/util/Collection;", ordinal = 1), cancellable = true)
    private void notItemInject(final T input, final Actionable type, IActionSource src, final CallbackInfoReturnable<T> cir) {
        if (input == null || input instanceof IAEItemStack) return;
        if (type == Actionable.SIMULATE || src instanceof FakeMonitor.FakeMonitorSource) return;
        final var drop = Util.packAEStackToDrop(input);
        if (drop != null) {
            if (!this.getDepth(type).isEmpty()) this.surface(null, type);
            cir.setReturnValue(FakeItemRegister.getAEStack(monitor.injectItems(drop, type, src)));
            this.diveList((NetworkInventoryHandler<T>) (Object) this, type);
        } else return;
        this.surface(null, type);
    }

    @Inject(method = "injectItems", at = @At(value = "FIELD", target = "Lappeng/me/storage/NetworkInventoryHandler;priorityInventory:Ljava/util/NavigableMap;", opcode = Opcodes.GETFIELD), cancellable = true)
    private void injectItemsN(final T input, final Actionable type, final IActionSource src, final CallbackInfoReturnable<T> cir, @Share("fc$fakeInput") final LocalBooleanRef fakeInput) {
        if (input == null || fakeInput.get() || !this.priorityInventory.isEmpty()) return;
        if (input instanceof final IAEItemStack i) {
            if (FakeFluids.isFluidFakeItem(i.getDefinition())) {
                cir.setReturnValue((T) fluidMonitor.injectItems(i, type, src));
            } else if (ModAndClassUtil.GAS && FakeGases.isGasFakeItem(i.getDefinition())) {
                cir.setReturnValue((T) gasMonitor.injectItems(i, type, src));
            } else {
                fakeInput.set(true);
                return;
            }
        } else {
            fakeInput.set(true);
            return;
        }
        this.surface(null, type);
    }

    // The crafting grid is handled in priority pass 1; convert fake fluids only in pass 2.
    @Inject(method = "injectItems", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 2), cancellable = true)
    private void injectItems(final T input, final Actionable type, final IActionSource src, final CallbackInfoReturnable<T> cir, @Share("fc$fakeInput") final LocalBooleanRef fakeInput) {
        if (input == null || fakeInput.get()) return;
        if (input instanceof final IAEItemStack i) {
            if (FakeFluids.isFluidFakeItem(i.getDefinition())) {
                cir.setReturnValue((T) fluidMonitor.injectItems(i, type, src));
            } else if (ModAndClassUtil.GAS && FakeGases.isGasFakeItem(i.getDefinition())) {
                cir.setReturnValue((T) gasMonitor.injectItems(i, type, src));
            } else {
                fakeInput.set(true);
                return;
            }
        } else {
            fakeInput.set(true);
            return;
        }
        this.surface(null, type);
    }

    @Inject(method = "extractItems", at = @At(value = "FIELD", target = "Lappeng/me/storage/NetworkInventoryHandler;priorityInventory:Ljava/util/NavigableMap;", opcode = Opcodes.GETFIELD), cancellable = true)
    public void extractItems(final T request, final Actionable mode, final IActionSource src, final CallbackInfoReturnable<T> cir) {
        if (request == null) return;
        if (request instanceof final IAEItemStack i) {
            if (FakeFluids.isFluidFakeItem(i.getDefinition())) {
                cir.setReturnValue((T) fluidMonitor.extractItems(i, mode, src));
            } else if (ModAndClassUtil.GAS && FakeGases.isGasFakeItem(i.getDefinition())) {
                cir.setReturnValue((T) gasMonitor.extractItems(i, mode, src));
            } else return;
        } else {
            if (src instanceof FakeMonitor.FakeMonitorSource || mode == Actionable.SIMULATE) return;
            final var drop = Util.packAEStackToDrop(request);
            if (drop != null) {
                this.surface(null, mode);
                cir.setReturnValue(FakeItemRegister.getAEStack(monitor.extractItems(drop, mode, src)));
                this.diveList((NetworkInventoryHandler<T>) (Object) this, mode);
            } else return;
        }
        this.surface(null, mode);
    }

    @Override
    public void init(final FCNetworkMonitor<?> monitor) {
        fluidMonitor = monitor.getFluidMonitor();
        gasMonitor = monitor.getGasMonitor();
    }
}
