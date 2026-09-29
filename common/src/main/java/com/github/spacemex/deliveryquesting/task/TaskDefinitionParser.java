package com.github.spacemex.deliveryquesting.task;

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

public final class TaskDefinitionParser {

    public static TaskDefinition parse(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement element = JsonParser.parseReader(reader);

            if (!element.isJsonObject()) {
                throw error(file, "Root element must be a JSON object");
            }

            return parse(file, element.getAsJsonObject());
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Failed to parse task file: " + file, e);
        }
    }

    private static TaskDefinition parse(Path source, JsonObject root) {
        Identifier id = parseIdentifier(source, "id", requiredString(source, root, "id"));
        String name = requiredString(source, root, "name");
        String description = optionalString(root, "description", "");
        ContractorDefinition contractor = parseContractor(source, root);
        int minLevel = optionalInt(root, "min_level", 0);
        boolean forced = optionalBoolean(root, "forced", false);
        List<Identifier> dependencies = parseDependencies(source, root);
        List<TaskRequirement> requirements = parseRequirements(source, root);
        TaskRewards rewards = parseRewards(source, root);

        return new TaskDefinition(id, name, description, contractor, minLevel, dependencies, forced, requirements, rewards);
    }

    private static ContractorDefinition parseContractor(Path source, JsonObject root) {
        JsonElement element = root.get("contractor");

        if (element == null || !element.isJsonObject()) {
            throw error(source, "Missing required object 'contractor'");
        }

        JsonObject contractor = element.getAsJsonObject();
        String name = requiredString(source, contractor, "name");
        String profession = optionalString(contractor, "profession", "");
        String skin = optionalString(contractor, "skin", "");

        return new ContractorDefinition(name, profession, skin);
    }

    private static List<Identifier> parseDependencies(Path source, JsonObject root) {
        JsonElement element = root.get("dependencies");

        if (!element.isJsonArray()) {
            throw error(source, "'dependencies' must be an array");
        }

        List<Identifier> dependencies = new ArrayList<>();

        for (JsonElement dependency : element.getAsJsonArray()) {
            if (!dependency.isJsonPrimitive() || !dependency.getAsJsonPrimitive().isString()) {
                throw error(source, "Every dependency must be an identifier string");
            }

            dependencies.add(parseIdentifier(source, "dependencies", dependency.getAsString()));
        }
        return dependencies;
    }

    private static List<TaskRequirement> parseRequirements(Path source, JsonObject root) {
        JsonElement element = root.get("requirements");

        if (element == null) {
            return List.of();
        }

        if (!element.isJsonArray()) {
            throw error(source, "'requirements' must be an array");
        }

        List<TaskRequirement> requirements = new ArrayList<>();

        for (JsonElement requirementElement : element.getAsJsonArray()) {
            if (!requirementElement.isJsonObject()) {
                throw error(source, "Every task requirement must be an object");
            }

            JsonObject requirement = requirementElement.getAsJsonObject();
            String type = requiredString(source, requirement, "type");

            switch (type) {
                case "item" -> requirements.add(parseItemRequirement(source, requirement));
                default -> throw error(source, "Unknown requirement type: " + type);
            }
        }
        return requirements;
    }

    private static ItemRequirement parseItemRequirement(Path source, JsonObject object) {
        boolean hasItem = object.has("item");
        boolean hasTag = object.has("tag");

        if (hasItem == hasTag) {
            throw error(source, "Item requirement must contain exactly one of 'item' or 'tag'");
        }

        long amount = optionalLong(object, "amount", 1L);

        if (hasItem) {
            Identifier item = parseIdentifier(source, "item", requiredString(source, object, "item"));
            return ItemRequirement.item(item, amount);
        }

        Identifier tag = parseIdentifier(source, "tag", requiredString(source, object, "tag"));
        return ItemRequirement.tag(tag, amount);
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
        List<ItemReward> itemRewards = parseItemRewards(source, rewards);

        return new TaskRewards(experience, money, itemRewards);
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

        for (JsonElement rewardElement : element.getAsJsonArray()) {
            if (!rewardElement.isJsonObject()) {
                throw error(source, "Every item reward must be an object");
            }

            JsonObject reward = rewardElement.getAsJsonObject();
            Identifier item = parseIdentifier(source, "rewards.items.item", requiredString(source, reward, "item"));
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

        String namespace = value.substring(0, separator);
        String path = value.substring(separator + 1);

        try {
            return Identifier.fromNamespaceAndPath(namespace, path);
        } catch (RuntimeException e) {
            throw error(source, "Invalid identifier for '" + field + "': " + value, e);
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

    private static boolean optionalBoolean(JsonObject object, String key, boolean defaultValue) {
        JsonElement element = object.get(key);

        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }

        return element.getAsBoolean();
    }

    private static IllegalArgumentException error(Path source, String message, Throwable cause) {
        return new IllegalArgumentException(source + ": " + message, cause);
    }

    private static IllegalArgumentException error(Path source, String message) {
        return new IllegalArgumentException(source + ": " + message);
    }
}
