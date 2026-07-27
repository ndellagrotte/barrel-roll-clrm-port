package io.github.ndellagrotte.doabarrelroll;

import io.github.ndellagrotte.doabarrelroll.api.RollEntity;
import io.github.ndellagrotte.doabarrelroll.api.RollMouse;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {
    private float lastRollBack;
    private float rollBack;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();

        // Residual roll spring-back, 50% per tick, when not rolling.
        if (mc.player == null || !((RollEntity) mc.player).doABarrelRoll$isRolling()) {
            lastRollBack = rollBack;
            rollBack -= rollBack * 0.5f;
        }

        if (!DoABarrelRollClient.isFallFlying()) {
            DoABarrelRollClient.clearValues();
        }
        ModKeybindings.clientTick(mc);
    }

    /**
     * Keeps the mouse pipeline (momentum mode plus the input-independent modifiers like banking and
     * righting) running even when the mouse isn't moving. This is the upstream Mouse.tick
     * maintainMouseMomentum hook: it runs once per rendered frame, immediately before
     * EntityRenderer.updateCameraAndRender applies the frame's mouse look, and shares that frame's
     * delta with it. Driving it per frame rather than per client tick matters - a per-tick pump
     * lands on an irregular subset of frames (Minecraft.runGameLoop runs 0..10 ticks inside a
     * frame), and since roll/prevRoll and rotation/prevRotation are advanced together the lump
     * reaches the camera unsmoothed, which reads as judder at tick rate whenever banking is
     * producing rotation.
     */
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        RollMouse mouse = (RollMouse) mc.entityRenderer;

        // Advance the frame clock unconditionally so it can't accumulate while there is no player.
        double frameDelta = mouse.doABarrelRoll$beginFrame();

        if (mc.player != null && !mc.isGamePaused()) {
            mouse.doABarrelRoll$updateMouse(mc.player, 0, 0, frameDelta);
        }
    }

    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (!(event.getEntity() instanceof RollEntity)) {
            return;
        }
        RollEntity rollEntity = (RollEntity) event.getEntity();
        float partialTicks = (float) event.getRenderPartialTicks();

        float applied;
        if (rollEntity.doABarrelRoll$isRolling()) {
            applied = rollEntity.doABarrelRoll$getRoll(partialTicks);
            lastRollBack = rollBack = rollEntity.doABarrelRoll$getRoll();
        } else {
            applied = lastRollBack + (rollBack - lastRollBack) * partialTicks;
        }

        if (applied != 0.0f) {
            // NOTE: roll sign verified in-game against upstream behavior.
            event.setRoll(event.getRoll() + applied);
        }
    }
}
