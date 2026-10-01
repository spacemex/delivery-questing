package com.github.spacemex.deliveryquesting;

import com.github.spacemex.deliveryquesting.client.DeliveryQuestingClient;
import com.github.spacemex.deliveryquesting.command.GroupCommand;
import com.github.spacemex.deliveryquesting.command.TaskCommand;
import com.github.spacemex.deliveryquesting.config.CommonConfig;
import com.github.spacemex.deliveryquesting.config.ConfigReader;
import com.github.spacemex.deliveryquesting.networking.*;
import com.github.spacemex.deliveryquesting.progression.ProgressionManager;
import com.github.spacemex.deliveryquesting.registry.ModRegistries;
import com.github.spacemex.deliveryquesting.task.OfferManager;
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
        ModRegistries.initialize();

        Path configPath = Platform.getConfigFolder().resolve("DeliveryQuesting/config.yml");
        CommonConfig.generate(configPath);
        ConfigReader.load();

        if (Platform.getEnvironment() == Env.SERVER) {
            ConfigSyncHandler.initialize();
        }

        TaskInteractionNetworkHandler.initialize();
        MailboxNetworkHandler.initialize();
        ComputerNetworkHandler.initialize();


        TaskManager.initialize();
        OfferManager.initialize();
        ProgressionManager.initialize();

        GroupCommand.initialize();
        TaskCommand.initialize();
    }

    public static void initializeClientOnly() {
        ClientConfigSyncHandler.initialize();
        DeliveryQuestingClient.initialize();
    }
}
