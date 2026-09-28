package com.github.spacemex.deliveryquesting.config;

import com.github.spacemex.yml.YamlConfigUtil;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public final class ConfigReader {

    private static final int DEFAULT_MIN_COMPUTER_LEVEL = 10;

    /*
     * Local config.
     *
     * Server:
     *     This is the server's config.yml.
     *
     * Client:
     *     This is the client's own config.yml and is used when
     *     there is no synchronized server config.
     */
    private static YamlConfigUtil localConfig;
    private static Map<String, Object> localRaw;

    /*
     * Server-synchronized config.
     *
     * Only populated on the client after joining a server.
     */
    private static YamlConfigUtil syncedConfig;
    private static Map<String, Object> syncedRaw;

    private ConfigReader() {
    }

    /**
     * Loads the local config from disk.
     * <p>
     * This should normally only be called once during mod startup,
     * or again if you intentionally implement config reloading.
     */
    public static void load() {
        File yamlFile = Platform.getConfigFolder()
                .resolve("DeliveryQuesting/config.yml")
                .toFile();

        Map<String, Object> data;

        try (Reader reader = new FileReader(yamlFile)) {
            Object loaded = new Yaml().load(reader);

            if (loaded instanceof Map<?, ?> map) {
                data = castMap(map);
            } else {
                data = new HashMap<>();
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load DeliveryQuesting config file",
                    e
            );
        }

        localRaw = data;
        localConfig = new YamlConfigUtil(data);
    }

    /**
     * Applies a config received from the server.
     * <p>
     * Intended to be called on the client when SyncPayload
     * is received.
     */
    public static void setSyncedConfig(Map<String, Object> data) {
        if (data == null) {
            clearSyncedConfig();
            return;
        }

        syncedRaw = data;
        syncedConfig = new YamlConfigUtil(data);
    }

    /**
     * Removes the server-synchronized config.
     * <p>
     * Call this when the client disconnects from a server so that
     * singleplayer / another server does not keep using the previous
     * server's configuration.
     */
    public static void clearSyncedConfig() {
        syncedConfig = null;
        syncedRaw = null;
    }

    /**
     * Returns whichever config should currently be used.
     * <p>
     * On a multiplayer client:
     * server config
     * <p>
     * Everywhere else:
     * local config
     */
    private static YamlConfigUtil getConfig() {
        ensureLoaded();

        if (Platform.getEnv() == EnvType.CLIENT && syncedConfig != null) {
            return syncedConfig;
        }

        return localConfig;
    }

    /**
     * Raw LOCAL config.
     * <p>
     * This is intentionally localRaw rather than the synchronized
     * config because this method is used by the server when sending
     * its configuration to joining players.
     */
    public static Map<String, Object> getRawLocal() {
        ensureLoaded();
        return localRaw;
    }

    /**
     * Optional if you ever need access to the synchronized raw map.
     */
    public static Map<String, Object> getRawSynced() {
        return syncedRaw;
    }

    /**
     * Config option:
     * <p>
     * minComputerLevel
     */
    public static int getMinComputerLevel() {
        int value = getConfig().getInt(
                "minComputerLevel",
                DEFAULT_MIN_COMPUTER_LEVEL
        );

        /*
         * Your config description states the minimum is 0.
         * int already cannot exceed Integer.MAX_VALUE.
         */
        return Math.max(0, value);
    }

    private static void ensureLoaded() {
        if (localConfig == null || localRaw == null) {
            throw new IllegalStateException(
                    "ConfigReader has not been loaded. Call ConfigReader.load() during mod initialization."
            );
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }
}