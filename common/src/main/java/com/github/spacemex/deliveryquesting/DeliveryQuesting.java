package com.github.spacemex.deliveryquesting;

import com.github.spacemex.deliveryquesting.config.CommonConfig;
import com.github.spacemex.deliveryquesting.config.ConfigReader;
import com.github.spacemex.deliveryquesting.networking.ClientConfigSyncHandler;
import com.github.spacemex.deliveryquesting.networking.ConfigSyncHandler;
import com.github.spacemex.deliveryquesting.task.TaskManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class DeliveryQuesting {
    public static final String MOD_ID = "delivery_questing";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void initialize() {
        Path configPath = Platform.getConfigFolder().resolve("DeliveryQuesting/config.yml");
        CommonConfig.generate(configPath);
        ConfigReader.load();

        if (Platform.getEnvironment() == Env.SERVER) {
            ConfigSyncHandler.initialize();
        }

        TaskManager.initialize();
    }

    public static void initializeClientOnly() {
        ClientConfigSyncHandler.initialize();
    }
}
