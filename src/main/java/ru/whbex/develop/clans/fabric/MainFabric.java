package ru.whbex.develop.clans.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import ru.whbex.develop.clans.common.ClansPlugin;
import ru.whbex.develop.clans.common.clan.ClanManager;
import ru.whbex.develop.clans.common.clan.loader.Bridge;
import ru.whbex.develop.clans.common.clan.loader.NullBridge;
import ru.whbex.develop.clans.common.clan.loader.SQLBridge;
import ru.whbex.develop.clans.common.db.H2SQLAdapter;
import ru.whbex.develop.clans.common.db.SQLAdapter;
import ru.whbex.develop.clans.common.db.SQLiteAdapter;
import ru.whbex.develop.clans.common.lang.LangFile;
import ru.whbex.develop.clans.common.lang.Language;
import ru.whbex.develop.clans.common.player.PlayerActor;
import ru.whbex.develop.clans.common.wrap.ConfigWrapper;
import ru.whbex.develop.clans.common.wrap.ConsoleActor;
import ru.whbex.develop.clans.common.wrap.Task;
import ru.whbex.develop.clans.fabric.wrap.ConfigWrapperFabric;
import ru.whbex.develop.clans.fabric.wrap.ConsoleActorFabric;
import ru.whbex.develop.clans.fabric.wrap.PlayerActorFabric;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* Main class for Clans port on Fabric */
@Environment(EnvType.SERVER)
public class MainFabric implements ModInitializer, ClansPlugin {
	public static final String MOD_ID = "clans-fabric";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private ConsoleActor consoleActor;
	private ConfigWrapper configWrapper;
	private SQLAdapter adapter;
	private Language lang;
	private final Map<UUID, PlayerActor> actors = new HashMap<>();

	private ExecutorService dbExecutor;



	@Override
	public void onInitialize() {
		Context.INSTANCE.logger = LOGGER;
		Context.INSTANCE.plugin = this;
		this.consoleActor = new ConsoleActorFabric();
		ClansPlugin.dbg("hello");
		ClansPlugin.log(Level.INFO, "=== Clans ===");
		ClansPlugin.log(Level.INFO, "Running on Fabric (Minecraft " + MinecraftVersion.CURRENT.getName() + ")");
		ClansPlugin.dbg("Config dir: " + FabricLoader.getInstance().getConfigDir().toString());
		File workdir = FabricLoader.getInstance().getConfigDir().toFile();


		this.configWrapper = new ConfigWrapperFabric();
		// TODO: Use assets for this
	//	LangFile lf = new LangFile(new File(workdir, "messages.lang"));
	//	lang = new Language(lf);


		ClansPlugin.log(Level.INFO, "Starting database executor thread");
		dbExecutor = Executors.newSingleThreadExecutor();

		// TODO: change exception handling
        try {
            adapter = new H2SQLAdapter(new File(workdir, MOD_ID + "_database.h2"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
		ClansPlugin.log(Level.INFO, "Connecting to db");
		// TODO: same
        try {
            adapter.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
		Bridge b = adapter == null ? new NullBridge() : new SQLBridge(adapter);
		ClanManager clanManager = new ClanManager(configWrapper, b);

		ClansPlugin.log(Level.INFO, "Registering event callbacks");
		ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
			ClansPlugin.dbg("register actor " + handler.getPlayer().getUuid());
			PlayerActor actor = actors.containsKey(handler.getPlayer().getUuid()) ?
					((PlayerActorFabric) actors.get(handler.getPlayer().getUuid())).updateEntity(handler.getPlayer()) :
					new PlayerActorFabric(handler.getPlayer());
			actors.put(actor.getUniqueId(), actor);
			ClansPlugin.dbg("ok");

		}));
		ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) -> {
            actors.remove(handler.getPlayer().getUuid());
		}));

		ClansPlugin.log(Level.INFO, "Startup completed");
    }

	@Override
	public ConsoleActor getConsoleActor() {
		return consoleActor;
	}

	@Override
	public PlayerActor getPlayerActor(UUID uuid) {
		return null;
	}

	@Override
	public PlayerActor getPlayerActor(String s) {
		return null;
	}

	@Override
	public PlayerActor getPlayerActorOrRegister(UUID uuid) {
		return null;
	}

	@Override
	public Collection<PlayerActor> getOnlineActors() {
		return List.of();
	}

	@Override
	public ClanManager getClanManager() {
		return null;
	}

	@Override
	public Language getLanguage() {
		return null;
	}

	@Override
	public SQLAdapter getSQLAdapter() {
		return null;
	}

	@Override
	public Task run(Runnable runnable) {
		return null;
	}

	@Override
	public Task runLater(long l, Runnable runnable) {
		return null;
	}

	@Override
	public Task runAsync(Runnable runnable) {
		return null;
	}

	@Override
	public Task runAsyncLater(long l, Runnable runnable) {
		return null;
	}

	@Override
	public <T> Future<T> runCallable(Callable<T> callable) {
		return dbExecutor.submit(callable);
	}

	@Override
	public ExecutorService getDatabaseExecutor() {
		return null;
	}

	@Override
	public void reloadLocales() throws Exception {

	}

	@Override
	public void reloadConfigs() throws Exception {

	}

	@Override
	public ConfigWrapper getConfigWrapped() {
		return configWrapper;
	}
}