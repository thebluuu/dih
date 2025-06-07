package dev.lvstrng.argon.module.modules.combat;

import dev.lvstrng.argon.event.events.TickListener;
import dev.lvstrng.argon.module.Category;
import dev.lvstrng.argon.module.Module;
import dev.lvstrng.argon.module.setting.NumberSetting;
import dev.lvstrng.argon.utils.BlockUtils;
import dev.lvstrng.argon.utils.EncryptedString;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public final class DoubleAnchor extends Module implements TickListener {
    private final BindSetting activateKey = new BindSetting(EncryptedString.of("Activate Key"), 71, false).setDescription(EncryptedString.of("Key that starts double anchoring"));
    private final NumberSetting switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0.0, 20.0, 0.0, 1.0);
    private final NumberSetting totemSlot = new NumberSetting(EncryptedString.of("Totem Slot"), 1.0, 9.0, 1.0, 1.0);
    private int delayCounter = 0;
    private int step = 0;
    private boolean isAnchoring = false;

    public DoubleAnchor() {
        super(EncryptedString.of("Double Anchor"), EncryptedString.of("Automatically Places 2 anchors"), -1, Category.COMBAT);
        this.addSettings(this.switchDelay, this.totemSlot, this.activateKey);
    }

    @Override
    public void onEnable() {
		eventManager.add(TickListener.class, this);
	    super.onEnable();
    }

    @Override
    public void onDisable() {
	    		eventManager.remove(TickListener.class, this);
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (this.mc.currentScreen != null) {
            return;
        }
        if (this.mc.player == null) {
            return;
        }
        if (!this.hasRequiredItems()) {
            return;
        }
        if (!this.isAnchoring && !this.checkActivationKey()) {
            return;
        }
        final HitResult crosshairTarget = this.mc.crosshairTarget;
        if (!(this.mc.crosshairTarget instanceof BlockHitResult) || BlockUtil.isBlockAtPosition(((BlockHitResult) crosshairTarget).getBlockPos(), Blocks.AIR)) {
            this.isAnchoring = false;
            this.resetState();
            return;
        }
        if (this.delayCounter < this.switchDelay.getIntValue()) {
            ++this.delayCounter;
            return;
        }
        if (this.step == 0) {
            InventoryUtil.selectItemFromHotbar(Items.RESPAWN_ANCHOR);
        } else if (this.step == 1) {
            BlockUtils.interactWithBlock((BlockHitResult) crosshairTarget, true);
        } else if (this.step == 2) {
            InventoryUtil.selectItemFromHotbar(Items.GLOWSTONE);
        } else if (this.step == 3) {
            BlockUtils.interactWithBlock((BlockHitResult) crosshairTarget, true);
        } else if (this.step == 4) {
            InventoryUtil.selectItemFromHotbar(Items.RESPAWN_ANCHOR);
        } else if (this.step == 5) {
            BlockUtils.interactWithBlock((BlockHitResult) crosshairTarget, true);
            BlockUtils.interactWithBlock((BlockHitResult) crosshairTarget, true);
        } else if (this.step == 6) {
            InventoryUtil.selectItemFromHotbar(Items.GLOWSTONE);
        } else if (this.step == 7) {
            BlockUtils.interactWithBlock((BlockHitResult) crosshairTarget, true);
        } else if (this.step == 8) {
            InventoryUtil.swap(this.totemSlot.getIntValue() - 1);
        } else if (this.step == 9) {
            BlockUtils.interactWithBlock((BlockHitResult) crosshairTarget, true);
        } else if (this.step == 10) {
            this.isAnchoring = false;
            this.step = 0;
            this.resetState();
            return;
        }
        ++this.step;
    }

    private boolean hasRequiredItems() {
        boolean b = false;
        boolean b2 = false;
        for (int i = 0; i < 9; ++i) {
            final ItemStack getStack = this.mc.player.getInventory().getStack(i);
            if (getStack.getItem().equals(Items.RESPAWN_ANCHOR)) {
                b = true;
            }
            if (getStack.getItem().equals(Items.GLOWSTONE)) {
                b2 = true;
            }
        }
        return b && b2;
    }

    private boolean checkActivationKey() {
        final int d = this.activateKey.getValue();
        if (d == -1 || !KeyUtils.isKeyPressed(d)) {
            this.resetState();
            return false;
        }
        return this.isAnchoring = true;
    }

    private void resetState() {
        this.delayCounter = 0;
    }

    public boolean isAnchoringActive() {
        return this.isAnchoring;
    }
}
