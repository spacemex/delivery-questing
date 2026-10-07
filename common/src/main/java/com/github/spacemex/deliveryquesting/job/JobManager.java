package com.github.spacemex.deliveryquesting.job;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public final class JobManager {

    private static final String JOB_DIRECTORY = "DeliveryQuesting/jobs";

    private static boolean initialized;

    private static volatile Map<Identifier, JobDefinition> jobs = Map.of();

    private JobManager() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        LifecycleEvent.SERVER_STARTING.register(server -> reload());
    }

    public static void reload() {
        Path directory = Platform.getConfigFolder().resolve(JOB_DIRECTORY);

        try {
            Files.createDirectories(directory);

            Map<Identifier, JobDefinition> loaded = loadDirectory(directory);

            jobs = Collections.unmodifiableMap(new LinkedHashMap<>(loaded));

            DeliveryQuesting.LOGGER.info("Loaded {} Delivery Questing repeatable job(s)", jobs.size());
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load Delivery Questing jobs", exception);
        }
    }

    private static Map<Identifier, JobDefinition> loadDirectory(Path directory) throws IOException {
        Map<Identifier, JobDefinition> loaded = new LinkedHashMap<>();

        try (Stream<Path> stream = Files.walk(directory)) {
            List<Path> files = stream.filter(Files::isRegularFile).filter(JobManager::isJsonFile).sorted().toList();

            for (Path file : files) {
                JobDefinition job = JobDefinitionParser.parse(file);
                JobDefinition existing = loaded.putIfAbsent(job.id(), job);

                if (existing != null) {
                    throw new IllegalArgumentException("Duplicate job ID '" + job.id()
                            + "' found while loading " + file);
                }

                DeliveryQuesting.LOGGER.debug("Loaded repeatable job {} from {}", job.id(), file);
            }
        }

        return loaded;
    }

    private static boolean isJsonFile(Path file) {
        return file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json");
    }

    public static Optional<JobDefinition> getJob(Identifier id) {
        return Optional.ofNullable(jobs.get(id));
    }

    public static boolean contains(Identifier id) {
        return jobs.containsKey(id);
    }

    public static Collection<JobDefinition> getJobs() {
        return jobs.values();
    }

    public static int size() {
        return jobs.size();
    }
}