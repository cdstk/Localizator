package kameib.localizator.proxy;


import kameib.localizator.client.event.NewConfigEvent;
import kameib.localizator.common.networking.PrimaryPacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class CommonProxy {

    public void preInit() {
        PrimaryPacketHandler.registerPackets();
    }
    
    public void onInit(FMLInitializationEvent event) {
        registerEvents();
    }
    
    protected void registerEvents() {
        MinecraftForge.EVENT_BUS.register(new NewConfigEvent());
    }

}