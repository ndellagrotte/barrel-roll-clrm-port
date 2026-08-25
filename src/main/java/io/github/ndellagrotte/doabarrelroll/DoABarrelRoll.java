package io.github.ndellagrotte.doabarrelroll;

import io.github.ndellagrotte.doabarrelroll.proxy.IProxy;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Cleanroom 1.12.2 port of "Do a Barrel Roll" by enjarai.
 * Original mod: https://github.com/enjarai/do-a-barrel-roll (GPL-3.0)
 */
// MixinBooter supplies Mixin on vanilla MinecraftForge and is a built-in mod on Cleanroom, so this
// dependency resolves on both loaders.
@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION,
        acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:mixinbooter")
public class DoABarrelRoll {

    public static final String MODID = Reference.MOD_ID;
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    @Mod.Instance(Reference.MOD_ID)
    public static DoABarrelRoll instance;

    @SidedProxy(modId = Reference.MOD_ID,
            clientSide = "io.github.ndellagrotte.doabarrelroll.proxy.ClientProxy",
            serverSide = "io.github.ndellagrotte.doabarrelroll.proxy.CommonProxy")
    public static IProxy proxy;

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Reference.MOD_ID, path);
    }

    /**
     * 1.12.2 has no permission-node API; the upstream permission nodes map to plain op-level checks.
     */
    public static boolean checkPermission(NetHandlerPlayServer handler, String permission, int operatorLevel) {
        return checkPermission(handler.player, permission, operatorLevel);
    }

    public static boolean checkPermission(EntityPlayerMP player, String permission, int operatorLevel) {
        return player.canUseCommand(operatorLevel, "");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Initializing {} {}", Reference.MOD_NAME, Reference.VERSION);
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

}
