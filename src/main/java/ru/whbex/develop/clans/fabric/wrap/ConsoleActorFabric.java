package ru.whbex.develop.clans.fabric.wrap;

import net.minecraft.text.Text;
import org.slf4j.event.Level;
import ru.whbex.develop.clans.common.ClansPlugin;
import ru.whbex.develop.clans.common.player.ConsoleActor;
import ru.whbex.develop.clans.fabric.MainFabric;

public class ConsoleActorFabric implements ConsoleActor {
    @Override
    public void sendMessage(String s) {
        ((MainFabric) ClansPlugin.Context.INSTANCE.plugin).getMinecraftServer().sendMessage(Text.of(s));
    }

    @Override
    public String getName() {
        return "Server";
    }
}
