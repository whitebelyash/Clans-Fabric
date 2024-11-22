package ru.whbex.develop.clans.fabric.wrap;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import ru.whbex.develop.clans.common.conf.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

// Gson wrapper
public class ConfigWrapperFabric implements Config {
    @SerializedName("database-type") private DatabaseType dbType = DatabaseType.H2;
    @SerializedName("database-name") private String dbName = "example.h2";
    @SerializedName("database-user") private String dbUser = "user";
    @SerializedName("database-password") private String dbPassword = "password";
    @SerializedName("database-address") private String dbAddress = ".";
    @SerializedName("clan-flush-delay") private long flushDelay = 2500L;


    public ConfigWrapperFabric() {

    }

    @Override
    public boolean test() {
        return false;
    }

    @Override
    public void reload() throws Exception {

    }

    @Override
    public Config.DatabaseType getDatabaseBackend() {
        return dbType;
    }

    @Override
    public String getDatabaseName() {
        return dbName;
    }

    @Override
    public String getDatabaseUser() {
        return dbUser;
    }

    @Override
    public String getDatabasePassword() {
        return dbPassword;
    }

    @Override
    public String getDatabaseAddress() {
        return dbAddress;
    }

    @Override
    public long getClanFlushDelay() {
        return flushDelay;
    }

}
