package ru.whbex.develop.clans.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* Main class for Clans port on Fabric */

public class MainFabric implements DedicatedServerModInitializer, ClansPlugin {
	public static final String MOD_ID = "clans-fabric";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private ConsoleActor consoleActor;
	private ConfigWrapper configWrapper;
	private File workdir;

	private ClanManager clanManager;
	private SQLAdapter adapter;

	private MinecraftServer server;
	private Language lang;
	private final Map<UUID, PlayerActor> actors = new HashMap<>();
	private final Map<String, PlayerActor> actorsByName = new HashMap<>();


	private ExecutorService dbExecutor;



	@Override
	public void onInitializeServer() {

		/* Context init */
		Context.INSTANCE.logger = LOGGER;
		Context.INSTANCE.plugin = this;
		this.consoleActor = new ConsoleActorFabric();

		/* Server lifecycle callbacks */
		ServerLifecycleEvents.SERVER_STARTING.register((server1 -> {
			this.server = server1;
            try {
                this.onPostInitialize();
            } catch (IOException | ClassNotFoundException | SQLException e) {
				ClansPlugin.log(Level.INFO, "Plugin init failed !!!");
                throw new RuntimeException(e);
            }
        }));
		ServerLifecycleEvents.SERVER_STOPPING.register(server1 -> this.onShutdown());
		ClansPlugin.log(Level.INFO, "Registered server lifecycle callbacks, continuing init");

		/* Startup */
		ClansPlugin.dbg("hello");
		ClansPlugin.log(Level.INFO, "=== Clans ===");
		ClansPlugin.log(Level.INFO, "Running on Fabric (Minecraft " + MinecraftVersion.CURRENT.getName() + ")");
		ClansPlugin.dbg("Config dir: " + FabricLoader.getInstance().getConfigDir().toString());
		workdir = FabricLoader.getInstance().getConfigDir().toFile();

		/* Config init */
		this.configWrapper = new ConfigWrapperFabric();
		File f = new File(workdir, "messages.lang"); // TODO: new name
		// TODO: Use assets for this
	//	LangFile lf = new LangFile(new File(workdir, "messages.lang"));
	//	lang = new Language(lf);


		ClansPlugin.log(Level.INFO, "Starting database executor thread");
		dbExecutor = Executors.newSingleThreadExecutor();


		ClansPlugin.log(Level.INFO, "=== Early init completed! ===");
    }

	private void onPostInitialize() throws IOException, ClassNotFoundException, SQLException {
		ClansPlugin.log(Level.INFO, "=== Starting up ===");

		adapter = new H2SQLAdapter(new File(workdir, MOD_ID + "_database.h2"));
		adapter.connect();

		// initial db update
		// TOOD: move to ClansPlugin
		adapter.update("CREATE TABLE IF NOT EXISTS clans (id varchar(36), tag varchar(16), " +
					"name varchar(24), " +
					"description varchar(255), " +
					"creationEpoch LONG, " + // TODO: fixxx
					"leader varchar(36), " +
					"deleted TINYINT, " +
					"level INT, " +
					"exp INT);");

		Bridge b = adapter == null ? new NullBridge() : new SQLBridge(adapter);
		clanManager = new ClanManager(configWrapper, b);

		ClansPlugin.log(Level.INFO, "Registering event callbacks");
		ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
			ClansPlugin.dbg("ok");

		}));
		ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) -> {
			actors.remove(handler.getPlayer().getUuid());
		}));

		ClansPlugin.log(Level.INFO, "=== Startup completed! ===");

	}
	private void onShutdown(){
		ClansPlugin.log(Level.INFO, "=== Shutting down ===");
		// shutdown tasks going here

		ClansPlugin.log(Level.INFO, "Complete, goodbye!");

	}

	@Override
	public ConsoleActor getConsoleActor() {
		return consoleActor;
	}

	@Override
	public PlayerActor getPlayerActor(UUID uuid) {
		return actors.get(uuid);
	}

	@Override
	public PlayerActor getPlayerActor(String s) {
		return actorsByName.get(s);
	}

	@Override
	public PlayerActor getPlayerActorOrRegister(UUID uuid) {
		return null;
	}

	@Override
	public Collection<PlayerActor> getOnlineActors() {
		return actors.values();
	}

	@Override
	public ClanManager getClanManager() {
		return clanManager;
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