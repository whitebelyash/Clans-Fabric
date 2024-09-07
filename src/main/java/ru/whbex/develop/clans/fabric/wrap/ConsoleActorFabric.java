package ru.whbex.develop.clans.fabric.wrap;

import org.slf4j.event.Level;
import ru.whbex.develop.clans.common.ClansPlugin;
import ru.whbex.develop.clans.common.wrap.ConsoleActor;

public class ConsoleActorFabric implements ConsoleActor {
    @Override
    public void sendMessage(String s) {
        // TODO: Discover better way to send messages (ConsoleSender on Fabric?)
        ClansPlugin.log(Level.INFO, s);
    }

    @Override
    public String getName() {
        return "Server";
    }
}
