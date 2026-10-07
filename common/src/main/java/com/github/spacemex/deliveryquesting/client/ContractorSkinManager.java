package com.github.spacemex.deliveryquesting.client;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.mojang.blaze3d.platform.NativeImage;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class ContractorSkinManager {

    private static final Path SKIN_DIRECTORY = Platform.getConfigFolder().resolve("DeliveryQuesting").resolve("skins")

            .toAbsolutePath().normalize();

    private static final Map<String, Identifier> CACHE = new HashMap<>();

    private static final Set<String> WARNED = new HashSet<>();

    private ContractorSkinManager() {
    }

    public static @Nullable Identifier getTexture(String skinName) {
        if (skinName == null || skinName.isBlank()) {
            return null;
        }

        String normalizedName = skinName.trim();

        Identifier cached = CACHE.get(normalizedName);

        if (cached != null) {
            return cached;
        }

        try {
            Files.createDirectories(SKIN_DIRECTORY);
        } catch (IOException e) {
            DeliveryQuesting.LOGGER.error("Failed to create contractor skin directory {}", SKIN_DIRECTORY, e);

            return null;
        }

        String fileName = normalizedName.endsWith(".png") ? normalizedName : normalizedName + ".png";

        Path skinPath = SKIN_DIRECTORY.resolve(fileName).normalize();

        if (!skinPath.startsWith(SKIN_DIRECTORY)) {
            warnOnce(normalizedName, "Rejected invalid contractor skin path '{}'", normalizedName);

            return null;
        }

        if (!Files.isRegularFile(skinPath)) {
            warnOnce(normalizedName, "Contractor skin '{}' was not found at {}", normalizedName, skinPath);

            return null;
        }

        try {
            NativeImage image;

            try (InputStream stream = Files.newInputStream(skinPath)) {
                image = NativeImage.read(stream);
            }

            if (image.getWidth() != 64 || image.getHeight() != 64) {
                DeliveryQuesting.LOGGER.warn("Contractor skin '{}' must be 64x64, found {}x{}",
                        normalizedName, image.getWidth(), image.getHeight());

                image.close();

                return null;
            }

            String hash = UUID.nameUUIDFromBytes(skinPath.toString().getBytes(StandardCharsets.UTF_8)).toString();

            Identifier textureId = Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID,
                    "contractor_skins/" + hash);

            DynamicTexture texture = new DynamicTexture(() -> "Delivery Questing contractor skin: "
                    + normalizedName, image);

            Minecraft.getInstance().getTextureManager().register(textureId, texture);

            CACHE.put(normalizedName, textureId);

            return textureId;

        } catch (IOException e) {
            DeliveryQuesting.LOGGER.error("Failed to load contractor skin '{}' from {}", normalizedName, skinPath, e);

            return null;
        }
    }

    private static void warnOnce(String key, String message, Object... arguments) {
        if (WARNED.add(key)) {
            DeliveryQuesting.LOGGER.warn(message, arguments);
        }
    }
}