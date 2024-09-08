package ru.whbex.develop.clans.fabric.wrap;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ru.whbex.develop.clans.common.ClansPlugin;
import ru.whbex.develop.clans.common.lang.Language;
import ru.whbex.develop.clans.common.misc.requests.Request;
import ru.whbex.develop.clans.common.player.PlayerActor;

import java.util.UUID;

public class PlayerActorFabric implements PlayerActor {
    private ServerPlayerEntity player;

    public PlayerActorFabric(ServerPlayerEntity player){
        this.player = player;
    }
    @Override
    public void sendMessage(String s) {
        this.player.sendMessage(Text.of(s));
    }

    @Override
    public boolean isOnline() {
        return !player.isDisconnected();
    }

    @Override
    public void teleport(int i, int i1, int i2, String s) {

    }

    @Override
    public UUID getUniqueId() {
        return player.getUuid();
    }

    @Override
    public String getName() {
        return player.getName().getString();
    }

    @Override
    public Language getLanguage() {
        return ClansPlugin.Context.INSTANCE.plugin.getLanguage();
    }

    // TODO: Implement requests

    @Override
    public void addRequest(Request request) {

    }

    @Override
    public void removeRequest(Request request) {

    }

    @Override
    public void removeRequest(PlayerActor playerActor) {

    }

    @Override
    public boolean hasRequestFrom(PlayerActor playerActor) {
        return false;
    }

    @Override
    public boolean hasRequest(Request request) {
        return false;
    }

    @Override
    public Request getRequest(PlayerActor playerActor) {
        return null;
    }

    public PlayerActorFabric updateEntity(ServerPlayerEntity spe){
        this.player = spe;
        return this;
    }
}
