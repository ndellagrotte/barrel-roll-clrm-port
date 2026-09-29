package io.github.ndellagrotte.doabarrelroll;

import io.github.ndellagrotte.doabarrelroll.flight.RollMouse;
import io.github.ndellagrotte.doabarrelroll.flight.RollPlayer;
import io.github.ndellagrotte.doabarrelroll.net.ClientNetworking;
import io.github.ndellagrotte.doabarrelroll.util.ElytraActivation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.living.LivingElytraDeployEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {
    private float lastRollBack;
    private float rollBack;

    private EntityPlayerSP lastSentPlayer;
    private boolean lastSentIsRolling;
    private float lastSentRoll;

    private boolean pushedPlayerRoll;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();

        // Residual roll spring-back, 50% per tick, when not rolling.
        if (mc.player == null || !RollPlayer.isRolling()) {
            lastRollBack = rollBack;
            rollBack -= rollBack * 0.5f;
        }

        if (!DoABarrelRollClient.isFallFlying()) {
            DoABarrelRollClient.clearValues();
        }
        ModKeybindings.clientTick(mc);
    }

    /**
     * End of the local player's tick: after {@code onLivingUpdate}, so the elytra deploy check,
     * {@code movementInput.jump} and {@code onGround} are final for this tick. The loader has already
     * snapshotted {@code prevRoll} at tick start.
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // The integrated server's players share this event bus.
        if (event.phase != TickEvent.Phase.END || event.side != Side.CLIENT) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (event.player != mc.player) {
            return;
        }
        EntityPlayerSP player = mc.player;

        RollPlayer.tick(player);

        // Jump tracking for the triple-jump/hybrid activation behaviours.
        if (player.onGround) {
            ElytraActivation.secondJump = false;
            ElytraActivation.thirdJump = false;
        }
        ElytraActivation.wasJumping = player.movementInput.jump;

        // Respawning and changing dimension create a new player whose server-side roll starts at 0.
        if (player != lastSentPlayer) {
            lastSentPlayer = player;
            lastSentIsRolling = false;
            lastSentRoll = 0.0f;
        }
        var isRolling = RollPlayer.isRolling();
        if (isRolling != lastSentIsRolling || player.getRoll() != lastSentRoll) {
            ClientNetworking.sendRollUpdate(player, isRolling);

            lastSentIsRolling = isRolling;
            lastSentRoll = player.getRoll();
        }
    }

    /**
     * Cancels the elytra deploy for the TRIPLE_JUMP/HYBRID activation behaviours. The event also
     * fires on the server side of an integrated server, which must be left alone.
     */
    @SubscribeEvent
    public void onElytraDeploy(LivingElytraDeployEvent event) {
        if (!event.getEntityLiving().world.isRemote || event.getEntityLiving() != Minecraft.getMinecraft().player) {
            return;
        }
        if (ElytraActivation.shouldSuppressDeploy()) {
            event.setCanceled(true);
        }
    }

    /**
     * Substitutes the roll flight pipeline for vanilla's mouse look while rolling. Fired once per
     * rendered frame with the loader's frame delta, which the idle pump below shares.
     */
    @SubscribeEvent
    public void onMouseTurn(InputEvent.MouseTurnEvent event) {
        if (RollMouse.updateMouse(event.getPlayer(), event.getYaw(), event.getPitch(), event.getFrameDeltaSeconds())) {
            event.setCanceled(true);
        }
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

        if (mc.player != null && !mc.isGamePaused()) {
            RollMouse.updateMouse(mc.player, 0, 0, ForgeHooksClient.getFrameDeltaSeconds());
        }
    }

    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        float partialTicks = (float) event.getRenderPartialTicks();

        float applied;
        if (event.getEntity() == Minecraft.getMinecraft().player && RollPlayer.isRolling()) {
            applied = event.getEntity().getRoll(partialTicks);
            lastRollBack = rollBack = event.getEntity().getRoll();
        } else {
            applied = lastRollBack + (rollBack - lastRollBack) * partialTicks;
        }

        if (applied != 0.0f) {
            // NOTE: roll sign verified in-game against upstream behavior.
            event.setRoll(event.getRoll() + applied);
        }
    }

    /**
     * Rolls gliding player models about their look axis, through the feet where vanilla pivots the
     * elytra pose. Remote players carry the roll synced by the loader; the local player's shows in
     * third person. LOWEST so a cancel by another handler cannot leave the matrix pushed.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();
        float partialTicks = event.getPartialRenderTick();
        float roll = player.getRoll(partialTicks);
        if (roll == 0.0f || !player.isElytraFlying()) {
            return;
        }

        Vec3d look = player.getLook(partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.translate(event.getX(), event.getY(), event.getZ());
        GlStateManager.rotate(roll, (float) look.x, (float) look.y, (float) look.z);
        GlStateManager.translate(-event.getX(), -event.getY(), -event.getZ());
        pushedPlayerRoll = true;
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (pushedPlayerRoll) {
            pushedPlayerRoll = false;
            GlStateManager.popMatrix();
        }
    }
}
