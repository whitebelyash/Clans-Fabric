package ru.whbex.develop.clans.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import ru.whbex.develop.clans.common.ClansPlugin;
import ru.whbex.develop.clans.common.Constants;
import ru.whbex.develop.clans.common.clan.ClanManager;

import ru.whbex.develop.clans.common.conf.Config;
import ru.whbex.develop.clans.common.player.PlayerActor;
import ru.whbex.develop.clans.common.player.PlayerManager;
import ru.whbex.develop.clans.common.task.DatabaseService;
import ru.whbex.develop.clans.common.task.TaskScheduler;
import ru.whbex.develop.clans.fabric.task.TaskSchedulerFabric;
import ru.whbex.develop.clans.fabric.wrap.ConfigWrapperFabric;
import ru.whbex.develop.clans.fabric.wrap.ConsoleActorFabric;
import ru.whbex.lib.lang.Language;
import ru.whbex.lib.lang.LanguageFile;
import ru.whbex.lib.log.LogContext;
import ru.whbex.lib.log.Debug;
import ru.whbex.lib.sql.conn.ConnectionConfig;
import ru.whbex.lib.sql.conn.ConnectionProvider;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;

/* Main class for Clans port on Fabric */

public class MainFabric implements DedicatedServerModInitializer, ClansPlugin {
	public static final String MOD_ID = "clans-fabric";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private File workdir;
	private Config config;

	private TaskScheduler taskScheduler;
	private ClanManager clanManager;
	private PlayerManager playerManager;

	private MinecraftServer server;
	private Language lang;

	@Override
	public void onInitializeServer() {

		/* Context init */
		LogContext.provideLogger(LOGGER);
		Context.INSTANCE.plugin = this;
		Debug.print("hello");

		/* Server lifecycle callbacks */
		ServerLifecycleEvents.SERVER_STARTING.register((server1 -> {
			// Will continue startup on server starting
			this.server = server1;
            try {
                this.onPostInit();
            } catch (IOException | ClassNotFoundException | SQLException e) {
				// TODO: Do not crash the game if post-init failed
				LogContext.log(Level.ERROR, "Mod startup failed. Bailing out");
                throw new RuntimeException(e);
            }
        }));
		ServerLifecycleEvents.SERVER_STOPPING.register(server1 -> this.onShutdown());
		LogContext.log(Level.INFO, "Registered server lifecycle callbacks, continuing init");

		/* Startup */
		LogContext.log(Level.INFO, "=== Clans ===");
		LogContext.log(Level.INFO, "Running on Fabric (Minecraft " + MinecraftVersion.CURRENT.getName() + ")");
		// TODO: Properly handle early init exceptions
        try {
            onEarlyInit();
        } catch (IOException | SQLException | InvocationTargetException | NoSuchMethodException |
                 InstantiationException | IllegalAccessException e) {
			LogContext.log(Level.ERROR, "Failed to initialize. Bailing out");
            throw new RuntimeException(e);
        }
    }
	// Those init tasks don't require MinecraftServer instance
	private void onEarlyInit() throws IOException, SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		LogContext.log(Level.INFO, "Doing early init!");
		this.taskScheduler = new TaskSchedulerFabric();
		this.configInit();
		this.localeInit();
		this.databaseInit();

		LogContext.log(Level.INFO, "=== Early init complete! ===");

	}

	private void onPostInit() throws IOException, ClassNotFoundException, SQLException {
		LogContext.log(Level.INFO, "=== Starting up ===");
		LogContext.log(Level.INFO, "=== Startup complete! ===");

	}
	private void onShutdown(){
		LogContext.log(Level.INFO, "=== Shutting down ===");
		// shutdown tasks go here
		DatabaseService.destroyService();
		LogContext.log(Level.INFO, "Complete, goodbye!");

	}
	private void configInit() throws IOException {
		LogContext.log(Level.INFO, "Initializing configuration...");
		workdir = new File(FabricLoader.getInstance().getConfigDir().toFile(), MOD_ID);
		if(!workdir.isDirectory())
			if(!workdir.mkdir())
				throw new IllegalStateException("Work directory create returned false!");
		Debug.print("Working directory: " + workdir.getAbsolutePath());
		loadConfig();
	}
	private void loadConfig() throws IOException {
		File configFile = new File(workdir, "config.json");
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();
		// Save default config
		if(!configFile.exists() || !configFile.isFile()){
			LogContext.log(Level.INFO, "Config file not found, creating default");
			if(!configFile.createNewFile())
				throw new IllegalStateException("Config file create returned false!");
			Writer fw = new FileWriter(configFile);
			gson.toJson(new ConfigWrapperFabric(), fw);
			fw.close();
		}
		// Init config from json
		Reader fr = new FileReader(configFile);
		this.config = gson.fromJson(fr, ConfigWrapperFabric.class);
		fr.close();
	}
	private void localeInit() throws IOException {
		LogContext.log(Level.INFO, "Unpacking locales...");
		File l = new File(workdir, "messages.lang");
		if(l.exists() && l.isFile()){
			LogContext.log(Level.INFO, "Locales are already unpacked");
		} else {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("messages.lang");
			if(is == null)
				throw new IllegalStateException("Failed to unpack locales, not found in jar!");
			if(!l.createNewFile())
				throw new IllegalStateException("Failed to create locale file!");
			long bytes = Files.copy(is, l.toPath(), StandardCopyOption.REPLACE_EXISTING);
			is.close();
			LogContext.log(Level.INFO, "Unpacked " + bytes + " bytes!");
		}
		LanguageFile lf = new LanguageFile(new File(workdir, Constants.LANGUAGE_FILE_NAME));
		lang = new Language(lf);
	}
	private void databaseInit() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, SQLException {
		LogContext.log(Level.INFO, "Initializing database...");
		LogContext.log(Level.INFO, "Using database backend {0}", config.getDatabaseBackend());
		ConnectionConfig conf = new ConnectionConfig(
				config.getDatabaseName(),
				config.getDatabaseBackend().isFile() ? workdir.getAbsolutePath() : config.getDatabaseAddress(),
				config.getDatabaseUser(),
				config.getDatabasePassword()
		);
		ConnectionProvider prov = config.getDatabaseBackend().provider().getConstructor(ConnectionConfig.class).newInstance(conf);
		prov.newConnection();
		DatabaseService.initializeService(prov);
		if(!DatabaseService.isInitialized())
			throw new IllegalStateException("Failed to initialize database service!");
	}
	public MinecraftServer getMinecraftServer(){
		return server;
	}

	@Override
	public String _getName() {
		return MOD_ID;
	}

	@Override
	public String _getDescription() {
		return "Clans plugin"; // TODO: get description from assets
	}

	@Override
	public String _getVersionString() {
		return "0.1"; // TODO: get version from assets
	}

	@Override
	public PlayerManager getPlayerManager() {
		return playerManager;
	}

	@Override
	public ClanManager getClanManager() {
		return clanManager;
	}

	@Override
	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	@Override
	public Language getLanguage() {
		return lang;
	}

	@Override
	public void reloadLangFiles() throws Exception {
		// TODO: Implement
		throw new UnsupportedOperationException("WIP on Fabric");
	}

	@Override
	public void reloadConfigs() throws Exception {
		this.loadConfig();
	}

	@Override
	public Config getConfigWrapped() {
		return config;
	}
}