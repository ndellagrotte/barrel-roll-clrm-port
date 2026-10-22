package io.github.ndellagrotte.doabarrelroll.proxy;

import io.github.ndellagrotte.doabarrelroll.ClientEventHandler;
import io.github.ndellagrotte.doabarrelroll.DoABarrelRollClient;
import io.github.ndellagrotte.doabarrelroll.ModKeybindings;
import io.github.ndellagrotte.doabarrelroll.RollMouseHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ModKeybindings.ALL.forEach(ClientRegistry::registerKeyBinding);
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(RollMouseHandler.INSTANCE);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        DoABarrelRollClient.init();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

}
