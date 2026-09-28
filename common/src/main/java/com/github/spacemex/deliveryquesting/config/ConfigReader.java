package com.github.spacemex.deliveryquesting.config;

import com.github.spacemex.yml.YamlConfigUtil;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public final class ConfigReader {
    private static final int DEFAULT_MIN_COMPUTER_LEVEL = 10;

    private static YamlConfigUtil localConfig;
    private static Map<String, Object> localRaw;

    private static YamlConfigUtil syncedConfig;
    private static Map<String, Object> syncedRaw;

    public static void load() {
        File yamlFile = Platform.getConfigFolder().resolve("DeliveryQuesting/config.yml").toFile();
        Map<String, Object> data;

        try (Reader reader = new FileReader(yamlFile)) {
            Object loaded = new Yaml().load(reader);

            if (loaded instanceof Map<?, ?> map) {
                data = castMap(map);
            } else {
                data = new HashMap<>();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load DeliveryQuesting config file", e);
        }

        localRaw = new HashMap<>(data);
        localConfig = new YamlConfigUtil(localRaw);
    }

    public static void setSyncedConfig(Map<String, Object> data) {
        if (data == null) {
            clearSyncedConfig();
            return;
        }

        syncedRaw = new HashMap<>(data);
        syncedConfig = new YamlConfigUtil(syncedRaw);
    }

    public static void clearSyncedConfig() {
        syncedConfig = null;
        syncedRaw = null;
    }

    private static YamlConfigUtil getConfig() {
        ensureLoaded();

        if (Platform.getEnvironment() == Env.CLIENT && syncedConfig != null) {
            return syncedConfig;
        }
        return localConfig;
    }

    public static Map<String, Object> getRawSynced() {
        return syncedRaw;
    }

    public static Map<String, Object> getRawLocal() {
        ensureLoaded();
        return localRaw;
    }

    public static int getMinComputerLevel() {
        int value = getConfig().getInt("minComputerLevel", DEFAULT_MIN_COMPUTER_LEVEL);
        return Math.min(0, value);
    }

    private static void ensureLoaded() {
        if (localConfig == null || localRaw == null) {
            throw new IllegalStateException("ConfigReader has not been loaded. " + "Call ConfigReader.load() during mod initialization.");
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }
}