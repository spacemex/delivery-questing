package com.github.spacemex.deliveryquesting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ObjModel {

    private final Identifier modelLocation;

    private ModelData modelData;

    public ObjModel(Identifier modelLocation) {
        this.modelLocation = modelLocation;
    }

    public void submit(Identifier texture, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        ModelData data = getModelData();

        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(texture),
                (pose, consumer) -> {
                    for (Face face : data.faces()) {
                        for (int i = 1; i < face.vertices().size() - 1; i++) {
                            FaceVertex a = face.vertices().get(0);

                            FaceVertex b = face.vertices().get(i);

                            FaceVertex c = face.vertices().get(i + 1);

                            emitVertex(consumer, pose, data, a, light);

                            emitVertex(consumer, pose, data, b, light);

                            emitVertex(consumer, pose, data, c, light);

                            emitVertex(consumer, pose, data, c, light);
                        }
                    }
                });
    }

    private ModelData getModelData() {
        if (modelData == null) {
            modelData = loadModel();
        }

        return modelData;
    }

    private ModelData loadModel() {
        Resource resource = Minecraft.getInstance().getResourceManager().getResource(modelLocation)
                .orElseThrow(() -> new IllegalStateException("Missing OBJ model: " + modelLocation));

        List<Vector3f> positions = new ArrayList<>();

        List<Vec2> texCoords = new ArrayList<>();

        List<Vector3f> normals = new ArrayList<>();

        List<Face> faces = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.open(),
                StandardCharsets.UTF_8))) {
            String rawLine;

            while ((rawLine = reader.readLine()) != null) {
                String line = rawLine.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\s+");

                switch (parts[0]) {
                    case "v" -> {
                        if (parts.length < 4) {
                            continue;
                        }

                        positions.add(new Vector3f(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]),
                                Float.parseFloat(parts[3])));
                    }

                    case "vt" -> {
                        if (parts.length < 3) {
                            continue;
                        }

                        texCoords.add(new Vec2(Float.parseFloat(parts[1]), 1F - Float.parseFloat(parts[2])));
                    }

                    case "vn" -> {
                        if (parts.length < 4) {
                            continue;
                        }

                        normals.add(new Vector3f(
                                Float.parseFloat(parts[1]),
                                Float.parseFloat(parts[2]),
                                Float.parseFloat(parts[3])));
                    }

                    case "f" -> {
                        if (parts.length < 4) {
                            continue;
                        }

                        List<FaceVertex> vertices = new ArrayList<>();

                        for (int i = 1; i < parts.length; i++) {
                            vertices.add(parseFaceVertex(parts[i], positions.size(), texCoords.size(), normals.size()));
                        }

                        faces.add(new Face(List.copyOf(vertices)));
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load OBJ model " + modelLocation, e);
        }

        return new ModelData(
                List.copyOf(positions),
                List.copyOf(texCoords),
                List.copyOf(normals),
                List.copyOf(faces));
    }

    private static FaceVertex parseFaceVertex(String value, int positionCount, int textureCount, int normalCount) {
        String[] parts = value.split("/", -1);

        int position = resolveIndex(parts[0], positionCount);
        int texture = parts.length > 1 && !parts[1].isEmpty() ? resolveIndex(parts[1], textureCount) : -1;
        int normal = parts.length > 2 && !parts[2].isEmpty() ? resolveIndex(parts[2], normalCount) : -1;

        return new FaceVertex(position, texture, normal);
    }

    private static int resolveIndex(String value, int size) {
        int index = Integer.parseInt(value);

        if (index > 0) {
            return index - 1;
        }

        if (index < 0) {
            return size + index;
        }

        return -1;
    }

    private static void emitVertex(VertexConsumer consumer, PoseStack.Pose pose, ModelData data,
                                   FaceVertex vertex, int light) {
        Vector3f position = data.positions().get(vertex.position());

        Vec2 uv = vertex.texture() >= 0 ? data.texCoords().get(vertex.texture()) : new Vec2(0F, 0F);
        Vector3f normal = vertex.normal() >= 0 ? data.normals().get(vertex.normal()) : new Vector3f(0F, 1F, 0F);

        consumer
                .addVertex(pose.pose(), position.x(), position.y(), position.z())
                .setColor(255, 255, 255, 255)
                .setUv(uv.x, uv.y)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, normal.x(), normal.y(), normal.z());
    }

    private record ModelData(List<Vector3f> positions, List<Vec2> texCoords, List<Vector3f> normals, List<Face> faces) {
    }

    private record Face(List<FaceVertex> vertices) {
    }

    private record FaceVertex(int position, int texture, int normal) {
    }
}