package ru.whbex.develop.clans.fabric.listener;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import ru.whbex.develop.clans.common.ClansPlugin;
import ru.whbex.develop.clans.common.player.PlayerActor;
import ru.whbex.develop.clans.fabric.MainFabric;
import ru.whbex.develop.clans.fabric.wrap.PlayerActorFabric;

public class ListenerFabric implements
        ServerPlayConnectionEvents.Init,
        ServerPlayConnectionEvents.Disconnect

{
    @Override
    public void onPlayInit(ServerPlayNetworkHandler handler, MinecraftServer server) {
        MainFabric f = (MainFabric) ClansPlugin.Context.INSTANCE.plugin;


    }
    @Override
    public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {

    }


}
