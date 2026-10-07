package com.github.spacemex.deliveryquesting.job;

import com.github.spacemex.deliveryquesting.task.TaskRequirement;
import com.github.spacemex.deliveryquesting.task.definition.ContractorDefinition;
import com.github.spacemex.deliveryquesting.task.entry.ItemRequirement;
import com.github.spacemex.deliveryquesting.task.entry.ItemReward;
import com.github.spacemex.deliveryquesting.task.entry.TaskRewards;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public final class JobDefinitionParser {

    private JobDefinitionParser() {
    }

    public static JobDefinition parse(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement element = JsonParser.parseReader(reader);

            if (!element.isJsonObject()) {
                throw error(file, "Root element must be a JSON object");
            }

            return parse(file, element.getAsJsonObject());

        } catch (JsonParseException exception) {
            throw new IllegalArgumentException("Failed to parse job file: " + file, exception);
        }
    }

    private static JobDefinition parse(Path source, JsonObject root) {
        Identifier id = parseIdentifier(source, "id", requiredString(source, root, "id"));

        String name = requiredString(source, root, "name");

        String description = optionalString(root, "description", "");

        ContractorDefinition contractor = parseContractor(source, root);

        int minLevel = optionalInt(root, "min_level", 0);

        List<TaskRequirement> requirements = parseRequirements(source, root);

        TaskRewards rewards = parseRewards(source, root);

        return new JobDefinition(id, name, description, contractor, minLevel, requirements, rewards);
    }

    private static ContractorDefinition parseContractor(Path source, JsonObject root) {
        JsonElement element = root.get("contractor");

        if (element == null || !element.isJsonObject()) {
            throw error(source, "Missing required object 'contractor'");
        }

        JsonObject contractor = element.getAsJsonObject();

        return new ContractorDefinition(requiredString(source, contractor, "name"),
                optionalString(contractor, "profession", ""),
                optionalString(contractor, "skin", ""));
    }

    private static List<TaskRequirement> parseRequirements(Path source, JsonObject root) {
        JsonElement element = root.get("requirements");

        if (element == null) {
            return List.of();
        }

        if (!element.isJsonArray()) {
            throw error(source, "'requirements' must be an array");
        }

        List<TaskRequirement> result = new ArrayList<>();

        for (JsonElement entry : element.getAsJsonArray()) {
            if (!entry.isJsonObject()) {
                throw error(source, "Every job requirement must be an object");
            }

            JsonObject requirement = entry.getAsJsonObject();

            String type = requiredString(source, requirement, "type");

            switch (type) {
                case "item" -> result.add(parseItemRequirement(source, requirement));
                default -> throw error(source, "Unknown requirement type: " + type);
            }
        }

        return result;
    }

    private static ItemRequirement parseItemRequirement(Path source, JsonObject object) {
        boolean hasItem = object.has("item");
        boolean hasTag = object.has("tag");

        if (hasItem == hasTag) {
            throw error(source, "Item requirement must contain exactly one of 'item' or 'tag'");
        }

        long amount = optionalLong(object, "amount", 1L);

        if (hasItem) {
            return ItemRequirement.item(parseIdentifier(source, "item", requiredString(source, object,
                    "item")), amount);
        }

        return ItemRequirement.tag(parseIdentifier(source, "tag", requiredString(source, object,
                "tag")), amount);
    }

    private static TaskRewards parseRewards(Path source, JsonObject root) {
        JsonElement element = root.get("rewards");

        if (element == null) {
            return TaskRewards.empty();
        }

        if (!element.isJsonObject()) {
            throw error(source, "'rewards' must be an object");
        }

        JsonObject rewards = element.getAsJsonObject();

        int experience = optionalInt(rewards, "experience", 0);

        long money = optionalLong(rewards, "money", 0L);

        List<ItemReward> items = parseItemRewards(source, rewards);

        return new TaskRewards(experience, money, items);
    }

    private static List<ItemReward> parseItemRewards(Path source, JsonObject rewards) {
        JsonElement element = rewards.get("items");

        if (element == null) {
            return List.of();
        }

        if (!element.isJsonArray()) {
            throw error(source, "'rewards.items' must be an array");
        }

        List<ItemReward> result = new ArrayList<>();

        for (JsonElement entry : element.getAsJsonArray()) {
            if (!entry.isJsonObject()) {
                throw error(source, "Every item reward must be an object");
            }

            JsonObject reward = entry.getAsJsonObject();

            Identifier item = parseIdentifier(source, "rewards.items.item",
                    requiredString(source, reward, "item"));

            int count = optionalInt(reward, "count", 1);

            result.add(new ItemReward(item, count));
        }

        return result;
    }

    private static Identifier parseIdentifier(Path source, String field, String value) {
        int separator = value.indexOf(':');

        if (separator <= 0 || separator == value.length() - 1) {
            throw error(source, "'" + field + "' must use a namespaced identifier: " + value);
        }

        try {
            return Identifier.fromNamespaceAndPath(value.substring(0, separator),
                    value.substring(separator + 1));
        } catch (RuntimeException exception) {
            throw error(source, "Invalid identifier for '" + field + "': " + value);
        }
    }

    private static String requiredString(Path source, JsonObject object, String key) {
        JsonElement element = object.get(key);

        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw error(source, "Missing or invalid string '" + key + "'");
        }

        String value = element.getAsString();

        if (value.isBlank()) {
            throw error(source, "'" + key + "' cannot be blank");
        }

        return value;
    }

    private static String optionalString(JsonObject object, String key, String defaultValue) {
        JsonElement element = object.get(key);

        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }

        return element.getAsString();
    }

    private static int optionalInt(JsonObject object, String key, int defaultValue) {
        JsonElement element = object.get(key);

        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }

        return element.getAsInt();
    }

    private static long optionalLong(JsonObject object, String key, long defaultValue) {
        JsonElement element = object.get(key);

        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }

        return element.getAsLong();
    }

    private static IllegalArgumentException error(Path source, String message) {
        return new IllegalArgumentException(source + ": " + message);
    }
}