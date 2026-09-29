package com.github.spacemex.deliveryquesting.task;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public final class TaskManager {

    private static final String TASK_DIRECTORY = "DeliveryQuesting/tasks";
    private static boolean initialized;
    private static volatile Map<Identifier, TaskDefinition> tasks = Map.of();

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        LifecycleEvent.SERVER_STARTING.register(server -> reload());
    }

    public static void reload() {
        Path directory = Platform.getConfigFolder().resolve(TASK_DIRECTORY);

        try {
            Files.createDirectories(directory);
            Map<Identifier, TaskDefinition> loaded = loadDirectory(directory);

            validateDependencies(loaded);
            validateDependencyCycles(loaded);
            tasks = Collections.unmodifiableMap(new LinkedHashMap<>(loaded));
            DeliveryQuesting.LOGGER.info("Loaded {} Delivery Questing task(s)", tasks.size());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Delivery Questing tasks", e);
        }
    }

    private static Map<Identifier, TaskDefinition> loadDirectory(Path directory) throws IOException {
        Map<Identifier, TaskDefinition> loaded = new LinkedHashMap<>();

        try (Stream<Path> stream = Files.walk(directory)) {
            List<Path> files = stream.filter(Files::isRegularFile).filter(TaskManager::isJsonFile).sorted().toList();

            for (Path file : files) {
                TaskDefinition task = TaskDefinitionParser.parse(file);
                TaskDefinition existing = loaded.putIfAbsent(task.id(), task);

                if (existing != null) {
                    throw new RuntimeException("Duplicate task ID '" + task.id() + "' found while loading " + file);
                }

                DeliveryQuesting.LOGGER.debug("Loaded task {} from {}", task.id(), file);
            }
        }
        return loaded;
    }

    private static boolean isJsonFile(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".json");
    }

    private static void validateDependencies(Map<Identifier, TaskDefinition> loaded) {
        for (TaskDefinition task : loaded.values()) {
            for (Identifier dependency : task.dependencies()) {
                if (!loaded.containsKey(dependency)) {
                    throw new IllegalArgumentException("Task '" + task.id() + "' depends on missing task '" + dependency + "'");
                }
            }
        }
    }

    private static void validateDependencyCycles(Map<Identifier, TaskDefinition> loaded) {
        Map<Identifier, VisitState> states = new HashMap<>();
        Deque<Identifier> path = new ArrayDeque<>();

        for (Identifier id : loaded.keySet()) {
            visit(id, loaded, states, path);
        }
    }

    private static void visit(Identifier id, Map<Identifier, TaskDefinition> loaded, Map<Identifier, VisitState> states, Deque<Identifier> path) {
        VisitState state = states.get(id);

        if (state == VisitState.VISITED) {
            return;
        }

        if (state == VisitState.VISITING) {
            throw new IllegalArgumentException("Cyclic task dependency detected: " + formatCycle(path, id));
        }

        states.put(id, VisitState.VISITING);
        path.addLast(id);
        TaskDefinition task = loaded.get(id);

        for (Identifier dependency : task.dependencies()) {
            visit(dependency, loaded, states, path);
        }

        path.removeLast();
        states.put(id, VisitState.VISITED);
    }

    private static String formatCycle(Deque<Identifier> path, Identifier repeated) {
        List<Identifier> cycle = new ArrayList<>();
        boolean recording = false;

        for (Identifier id : path) {
            if (id.equals(repeated)) {
                recording = true;
            }

            if (recording) {
                cycle.add(id);
            }
        }

        cycle.add(repeated);
        return String.join(" -> ", cycle.stream().map(Identifier::toString).toList());
    }

    public static Optional<TaskDefinition> getTask(Identifier id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public static boolean contains(Identifier id) {
        return tasks.containsKey(id);
    }

    public static Collection<TaskDefinition> getTasks() {
        return tasks.values();
    }

    public static Map<Identifier, TaskDefinition> getTaskMap() {
        return tasks;
    }

    public static int size() {
        return tasks.size();
    }

    public enum VisitState {
        VISITING,
        VISITED
    }
}
