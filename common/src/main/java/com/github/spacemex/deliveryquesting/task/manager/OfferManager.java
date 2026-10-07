package com.github.spacemex.deliveryquesting.task.manager;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.task.definition.OfferDefinition;
import com.google.gson.*;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class OfferManager {

    private static final String OFFER_FILE = "DeliveryQuesting/offers.json";

    private static boolean initialized;

    private static volatile Map<Identifier, OfferDefinition> offers = Map.of();

    private OfferManager() {}

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        LifecycleEvent.SERVER_STARTING.register(server -> reload());
    }

    public static void reload() {
        Path file = Platform.getConfigFolder().resolve(OFFER_FILE);

        if (!Files.exists(file)) {
            offers = Map.of();

            DeliveryQuesting.LOGGER.info("No Delivery Questing offers file found at {}", file);
            return;
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement root = JsonParser.parseReader(reader);

            JsonArray array;

            if (root.isJsonArray()) {
                array = root.getAsJsonArray();
            } else if (root.isJsonObject()) {
                JsonElement offersElement = root.getAsJsonObject().get("offers");

                if (offersElement == null || !offersElement.isJsonArray()) {
                    throw error(file, "Expected root array or object containing an 'offers' array");
                }

                array = offersElement.getAsJsonArray();

            } else {
                throw error(file, "Root element must be an array or object");
            }

            Map<Identifier, OfferDefinition> loaded = new LinkedHashMap<>();

            for (JsonElement element : array) {
                if (!element.isJsonObject()) {
                    throw error(file, "Every offer must be an object");
                }

                OfferDefinition offer = parse(file, element.getAsJsonObject());
                OfferDefinition existing = loaded.putIfAbsent(offer.id(), offer);

                if (existing != null) {
                    throw error(file, "Duplicate offer ID '" + offer.id() + "'");
                }
            }

            offers = Collections.unmodifiableMap(loaded);

            DeliveryQuesting.LOGGER.info("Loaded {} Delivery Questing offer(s)", offers.size());
        } catch (IOException | JsonParseException exception) {
            throw new RuntimeException("Failed to load Delivery Questing offers", exception);
        }
    }

    private static OfferDefinition parse(Path source, JsonObject object) {
        Identifier id = parseIdentifier(source, "id", requiredString(source, object, "id"));
        Identifier item = parseIdentifier(source, "item", requiredString(source, object, "item"));

        int count = optionalInt(object, "count", 1);
        int minLevel = optionalInt(object, "min_level", 0);

        long price = requiredLong(source, object, "price");

        boolean forEveryMember = optionalBoolean(object, "for_every_member", false);

        return new OfferDefinition(id, item, count, price, minLevel, forEveryMember);
    }

    public static Collection<OfferDefinition> getOffers() {
        return offers.values();
    }

    public static Optional<OfferDefinition> getOffer(Identifier id) {
        return Optional.ofNullable(offers.get(id));
    }

    public static boolean contains(Identifier id) {
        return offers.containsKey(id);
    }

    private static Identifier parseIdentifier(Path source, String field, String value) {
        int separator = value.indexOf(':');

        if (separator <= 0 || separator == value.length() - 1) {
            throw error(source, "'" + field + "' must use a namespaced identifier: " + value);
        }

        try {
            return Identifier.fromNamespaceAndPath(value.substring(0, separator), value.substring(separator + 1));
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

    private static long requiredLong(Path source, JsonObject object, String key) {
        JsonElement element = object.get(key);

        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw error(source, "Missing or invalid number '" + key + "'");
        }

        return element.getAsLong();
    }

    private static int optionalInt(JsonObject object, String key, int defaultValue) {
        JsonElement element = object.get(key);

        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }

        return element.getAsInt();
    }

    private static boolean optionalBoolean(JsonObject object, String key, boolean defaultValue) {
        JsonElement element = object.get(key);

        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }

        return element.getAsBoolean();
    }

    private static IllegalArgumentException error(Path source, String message) {
        return new IllegalArgumentException(source + ": " + message);
    }
}