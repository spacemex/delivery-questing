package com.github.spacemex.deliveryquesting.progression;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.config.ConfigReader;
import com.github.spacemex.deliveryquesting.task.*;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class ProgressionManager {
    private static boolean initialized;
    private static final Identifier COMPUTER_ITEM = Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "computer");
    private static final int CONTRACT_EMAIL_INTERVAL_TICKS = 3600;
    private static final int MAILBOX_CONTRACT_INTERVAL_TICKS = 1200;

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

        TickEvent.SERVER_POST.register(server -> {
            DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(server);

            if (server.getTickCount() % MAILBOX_CONTRACT_INTERVAL_TICKS == 0) {
                data.generateMailboxContracts();
            }

            if (server.getTickCount() % CONTRACT_EMAIL_INTERVAL_TICKS == 0) {
                data.generateContractEmails();
                data.generateJobEmails();
            }

            long overworldTime = Math.floorMod(server.overworld().getOverworldClockTime(), 24000L);

            if (overworldTime == 20L) {
                int delivered = data.deliverPendingDeliveries();

                if (delivered > 0) {
                    DeliveryQuesting.LOGGER.debug("Delivered pending Minazon orders for {} group(s)", delivered);
                }
            }
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

        if (addOfferEmails(group, previousLevel + 1, currentLevel)) {
            changed = true;
        }

        int computerLevel = ConfigReader.getMinComputerLevel();

        if (previousLevel < computerLevel && currentLevel >= computerLevel && group.unlockComputer()) {
            changed = true;
            DeliveryQuesting.LOGGER.info("Group '{}' reached the Computer age at level {}", group.name(), currentLevel);
        }

        if (deliverComputerReward(group)) {
            changed = true;
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

        if (addOfferEmails(group, 0, currentLevel)) {
            changed = true;
        }

        if (currentLevel >= ConfigReader.getMinComputerLevel() && group.unlockComputer()) {
            changed = true;
            DeliveryQuesting.LOGGER.info("Reconciled Computer unlock for group '{}'", group.name());
        }

        if (deliverComputerReward(group)) {
            changed = true;
        }

        return changed;
    }

    private static boolean deliverComputerReward(DeliveryGroup group) {
        if (!group.computerUnlocked()) {
            return false;
        }

        if (!group.markComputerRewardDelivered()) {
            return false;
        }

        MailboxParcel parcel = MailboxParcel.create("Unknown", List.of(new ItemReward(COMPUTER_ITEM, 1)));

        group.addMailboxParcel(parcel);
        DeliveryQuesting.LOGGER.info("Delivered Computer unlock parcel to group '{}'", group.name());

        return true;
    }

    private static boolean addOfferEmails(DeliveryGroup group, int minimumLevel, int maximumLevel) {
        boolean changed = false;

        for (OfferDefinition offer : OfferManager.getOffers()) {
            if (offer.minLevel() < minimumLevel) {
                continue;
            }

            if (offer.minLevel() > maximumLevel) {
                continue;
            }

            if (group.hasOfferEmail(offer.id())) {
                continue;
            }

            if (group.addEmail(GroupEmail.offer(offer))) {
                changed = true;
                DeliveryQuesting.LOGGER.info("Offer {} unlocked for group '{}'", offer.id(), group.name());
            }
        }

        return changed;
    }
}
