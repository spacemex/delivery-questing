package com.github.spacemex.deliveryquesting;

import com.github.spacemex.deliveryquesting.client.DeliveryQuestingClient;
import com.github.spacemex.deliveryquesting.command.GroupCommand;
import com.github.spacemex.deliveryquesting.command.TaskCommand;
import com.github.spacemex.deliveryquesting.config.CommonConfig;
import com.github.spacemex.deliveryquesting.config.ConfigReader;
import com.github.spacemex.deliveryquesting.job.JobManager;
import com.github.spacemex.deliveryquesting.networking.*;
import com.github.spacemex.deliveryquesting.progression.ProgressionManager;
import com.github.spacemex.deliveryquesting.registry.ModRegistries;
import com.github.spacemex.deliveryquesting.task.manager.OfferManager;
import com.github.spacemex.deliveryquesting.task.manager.TaskManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

// TODO: Move Scripting Dir Creation To Its Own Class
public final class DeliveryQuesting {
    public static final String MOD_ID = "delivery_questing";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Path MOD_DIR = Platform.getConfigFolder().resolve("DeliveryQuesting");
    private static final Path CONFIG_FILE = MOD_DIR.resolve("config.yml");
    private static final Path TASKS_DIR = MOD_DIR.resolve("tasks");
    private static final Path SKINS_DIR = MOD_DIR.resolve("skins");
    private static final Path JOBS_DIR = MOD_DIR.resolve("jobs");

    public static void initialize() {
        ModRegistries.initialize();

        CommonConfig.generate(CONFIG_FILE);
        ConfigReader.load();

        if (Platform.getEnvironment() == Env.SERVER) {
            ConfigSyncHandler.initialize();
        }

        generateScriptingDir();

        TaskInteractionNetworkHandler.initialize();
        MailboxNetworkHandler.initialize();
        ComputerNetworkHandler.initialize();
        DronePadNetworkHandler.initialize();


        TaskManager.initialize();
        JobManager.initialize();
        OfferManager.initialize();
        ProgressionManager.initialize();

        GroupCommand.initialize();
        TaskCommand.initialize();
    }

    public static void initializeClientOnly() {
        ClientConfigSyncHandler.initialize();
        DeliveryQuestingClient.initialize();
    }

    private static void generateScriptingDir() {
        if (!TASKS_DIR.toFile().exists()) {
            if (TASKS_DIR.toFile().mkdirs()) {
                generateExampleTasks();
            }
        }

        if (!JOBS_DIR.toFile().exists()) {
            if (JOBS_DIR.toFile().mkdirs()) {
                generateExampleJobs();
            }
        }

        if (!SKINS_DIR.toFile().exists()) {
            SKINS_DIR.toFile().mkdirs();
        }
    }

    private static void generateExampleJobs() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject cobblestoneShipment = new JsonObject();
        cobblestoneShipment.addProperty("id", "delivery_questing:cobblestone_shipment");
        cobblestoneShipment.addProperty("name", "Cobblestone Shipment");
        cobblestoneShipment.addProperty("description", "A repeatable order for bulk cobblestone.");

        JsonObject contractor = new JsonObject();
        contractor.addProperty("name", "Stoneworks Inc.");
        contractor.addProperty("profession", "Construction Supplier");
        contractor.addProperty("skin", "");
        cobblestoneShipment.add("contractor", contractor);

        cobblestoneShipment.addProperty("min_level", 10);

        JsonArray requirements = new JsonArray();

        JsonObject cobblestoneRequirement = new JsonObject();
        cobblestoneRequirement.addProperty("type", "item");
        cobblestoneRequirement.addProperty("item", "minecraft:cobblestone");
        cobblestoneRequirement.addProperty("amount", 16);

        requirements.add(cobblestoneRequirement);
        cobblestoneShipment.add("requirements", requirements);

        JsonObject rewards = new JsonObject();
        rewards.addProperty("experience", 5);
        rewards.addProperty("money", 40);
        rewards.add("items", new JsonArray());

        cobblestoneShipment.add("rewards", rewards);

        try {
            Files.writeString(JOBS_DIR.resolve("cobblestone_shipment.json"), gson.toJson(cobblestoneShipment));
        } catch (Exception e) {
            LOGGER.error("Failed to generate example job JSON", e);
        }
    }

    private static void generateExampleTasks() {
        final Path gettingStartedDir = TASKS_DIR.resolve("getting_started");

        if (!gettingStartedDir.toFile().exists()) {
            gettingStartedDir.toFile().mkdirs();
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject logDelivery = new JsonObject();
        logDelivery.addProperty("id", "delivery_questing:getting_started/log_delivery");
        logDelivery.addProperty("name", "A Stack of Timber");
        logDelivery.addProperty("description", "The local carpenter needs a fresh shipment of logs.");

        JsonObject contractor = new JsonObject();
        contractor.addProperty("name", "William");
        contractor.addProperty("profession", "Carpenter");
        contractor.addProperty("skin", "");
        logDelivery.add("contractor", contractor);

        logDelivery.addProperty("min_level", 0);
        logDelivery.addProperty("forced", false);

        JsonArray dependencies = new JsonArray();
        logDelivery.add("dependencies", dependencies);

        JsonArray requirements = new JsonArray();

        JsonObject logRequirement = new JsonObject();
        logRequirement.addProperty("type", "item");
        logRequirement.addProperty("tag", "minecraft:logs");
        logRequirement.addProperty("amount", 16);

        requirements.add(logRequirement);
        logDelivery.add("requirements", requirements);

        JsonObject rewards = new JsonObject();
        rewards.addProperty("experience", 25);
        rewards.addProperty("money", 100);

        JsonArray rewardItems = new JsonArray();

        JsonObject breadReward = new JsonObject();
        breadReward.addProperty("item", "minecraft:bread");
        breadReward.addProperty("count", 4);

        rewardItems.add(breadReward);
        rewards.add("items", rewardItems);

        logDelivery.add("rewards", rewards);

        try {
            Files.writeString(gettingStartedDir.resolve("log_delivery.json"), gson.toJson(logDelivery));
        } catch (Exception e) {
            LOGGER.error("Failed to generate example task JSON", e);
        }
    }
}
