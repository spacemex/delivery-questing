package com.github.spacemex.deliveryquesting.progression;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.config.ConfigReader;
import com.github.spacemex.deliveryquesting.task.TaskDefinition;
import com.github.spacemex.deliveryquesting.task.TaskManager;
import dev.architectury.event.events.common.LifecycleEvent;

public final class ProgressionManager {
    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        LifecycleEvent.SERVER_STARTING.register(server -> {
            DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(server);
            data.reconcileProgression();
            DeliveryQuesting.LOGGER.info("Loaded Delivery Questing progression with {} group(s)", data.groupCount());
        });
    }

    static boolean processLevelChange(DeliveryGroup group, int previousLevel, int currentLevel) {
        if (currentLevel <= previousLevel) {
            return false;
        }

        boolean changed = false;

        for (TaskDefinition task : TaskManager.getTasks()) {
            if (!task.forced()) {
                continue;
            }

            if (task.minLevel() <= previousLevel || task.minLevel() > currentLevel) {
                continue;
            }

            if (group.hasCompletedTask(task.id())) {
                continue;
            }

            if (group.hasActiveTask(task.id())) {
                continue;
            }

            if (group.addActiveTask(task.id())) {
                changed = true;
                DeliveryQuesting.LOGGER.info("Forced task {} activated for group '{}' at level {}", task.id(), group.name(), currentLevel);
            }
        }

        int computerLevel = ConfigReader.getMinComputerLevel();

        if (previousLevel < computerLevel && currentLevel >= computerLevel && group.unlockComputer()) {
            changed = true;
            DeliveryQuesting.LOGGER.info("Group '{}' reached the Computer age at level {}", group.name(), currentLevel);
        }

        return changed;
    }

    static boolean reconcileGroup(DeliveryGroup group) {
        boolean changed = false;

        int currentLevel = group.wholeLevel();

        for (TaskDefinition task : TaskManager.getTasks()) {

            if (!task.forced()) {
                continue;
            }

            if (task.minLevel() > currentLevel) {
                continue;
            }

            if (group.hasCompletedTask(task.id())) {
                continue;
            }

            if (group.hasActiveTask(task.id())) {
                continue;
            }

            if (group.addActiveTask(task.id())) {
                changed = true;
                DeliveryQuesting.LOGGER.info("Reconciled forced task {} for group '{}'", task.id(), group.name());
            }
        }

        if (currentLevel >= ConfigReader.getMinComputerLevel() && group.unlockComputer()) {
            changed = true;
            DeliveryQuesting.LOGGER.info("Reconciled Computer unlock for group '{}'", group.name());
        }

        return changed;
    }
}
