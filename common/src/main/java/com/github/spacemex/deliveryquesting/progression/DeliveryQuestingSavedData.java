package com.github.spacemex.deliveryquesting.progression;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.job.JobDefinition;
import com.github.spacemex.deliveryquesting.job.JobManager;
import com.github.spacemex.deliveryquesting.task.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class DeliveryQuestingSavedData extends SavedData {
    public static final Codec<DeliveryQuestingSavedData> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(DeliveryGroup.CODEC.listOf().optionalFieldOf("groups", List.of())
                                    .forGetter(data -> List.copyOf(data.groups.values())))
                            .apply(instance, DeliveryQuestingSavedData::new));
    private static final SavedDataType<DeliveryQuestingSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "progression"),
            DeliveryQuestingSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );
    private final Map<UUID, DeliveryGroup> groups = new LinkedHashMap<>();

    public DeliveryQuestingSavedData() {
    }

    private DeliveryQuestingSavedData(List<DeliveryGroup> loadedGroups) {
        Objects.requireNonNull(loadedGroups, "loadedGroups");

        for (DeliveryGroup group : loadedGroups) {
            DeliveryGroup existing = groups.putIfAbsent(group.id(), group);

            if (existing != null) {
                throw new IllegalArgumentException("Duplicate Delivery Questing group ID: " + group.id());
            }
        }
        validateMembership();
    }

    public static DeliveryQuestingSavedData get(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public Collection<DeliveryGroup> groups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    public int groupCount() {
        return groups.size();
    }

    public Optional<DeliveryGroup> getGroup(UUID groupId) {
        return Optional.ofNullable(groups.get(groupId));
    }

    public Optional<DeliveryGroup> getGroupForPlayer(UUID playerId) {
        return groups.values().stream().filter(group -> group.hasMember(playerId)).findFirst();
    }

    public Optional<DeliveryGroup> getGroupByName(String name) {
        if (name == null) {
            return Optional.empty();
        }

        return groups.values().stream().filter(group -> group.name().equalsIgnoreCase(name)).findFirst();
    }

    public DeliveryGroup createGroup(String name, UUID owner) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(owner, "owner");
        String trimmedName = name.trim();

        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be blank");
        }

        if (getGroupByName(trimmedName).isPresent()) {
            throw new IllegalArgumentException("A group named '" + trimmedName + "' already exists");
        }

        if (getGroupForPlayer(owner).isPresent()) {
            throw new IllegalStateException("Player " + owner + " is already in a group");
        }

        UUID id = UUID.randomUUID();
        DeliveryGroup group = DeliveryGroup.create(id, trimmedName, owner);

        ProgressionManager.reconcileGroup(group);

        groups.put(id, group);
        clearInvitationsForPlayer(owner);

        setDirty();
        return group;
    }

    public boolean deleteGroup(UUID groupId) {
        DeliveryGroup removed = groups.remove(groupId);

        if (removed == null) {
            return false;
        }

        setDirty();
        return true;
    }

    public boolean addMember(UUID groupId, UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");

        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return false;
        }

        if (getGroupForPlayer(playerId).isPresent()) {
            return false;
        }

        if (!group.addMember(playerId)) {
            return false;
        }

        clearInvitationsForPlayer(playerId);

        setDirty();
        return true;
    }

    public boolean removeMember(UUID groupId, UUID playerId) {
        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return false;
        }

        if (!group.removeMember(playerId)) {
            return false;
        }

        setDirty();
        return true;
    }

    public boolean addExperience(UUID groupId, long amount) {
        if (amount <= 0L) {
            return false;
        }

        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return false;
        }

        int previousLevel = group.wholeLevel();

        group.addExperience(amount);

        int currentLevel = group.wholeLevel();
        ProgressionManager.processLevelChange(group, previousLevel, currentLevel);

        setDirty();
        return true;
    }

    public boolean addBalance(UUID groupId, long amount) {
        if (amount <= 0L) {
            return false;
        }

        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return false;
        }

        group.addBalance(amount);

        setDirty();
        return true;
    }

    public boolean spendBalance(UUID groupId, long amount) {
        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return false;
        }

        if (!group.spendBalance(amount)) {
            return false;
        }

        setDirty();
        return true;
    }

    public boolean acceptTask(UUID groupId, Identifier taskId) {
        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return false;
        }

        if (!group.addActiveTask(taskId)) {
            return false;
        }

        setDirty();
        return true;
    }

    public long addTaskProgress(UUID groupId, Identifier taskId, TaskRequirement requirement, long amount) {
        if (amount <= 0L) {
            return 0L;
        }

        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return 0L;
        }

        Optional<TaskProgress> optionalProgress = group.getActiveTask(taskId);

        if (optionalProgress.isEmpty()) {
            return 0L;
        }

        long accepted = optionalProgress.get().addProgress(requirement, amount);

        if (accepted > 0L) {
            setDirty();
        }
        return accepted;
    }

    public boolean completeTask(UUID groupId, TaskDefinition task) {
        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return false;
        }

        Optional<TaskProgress> optionalProgress = group.getActiveTask(task.id());

        if (optionalProgress.isEmpty()) {
            return false;
        }

        if (!optionalProgress.get().isComplete(task)) {
            return false;
        }

        if (!group.markTaskCompleted(task.id())) {
            return false;
        }

        int previousLevel = group.wholeLevel();

        group.addExperience(task.rewards().experience());

        int currentLevel = group.wholeLevel();

        ProgressionManager.processLevelChange(group, previousLevel, currentLevel);

        group.addBalance(task.rewards().money());

        if (!task.rewards().items().isEmpty()) {
            MailboxParcel parcel = MailboxParcel.create(task.contractor().name(), task.rewards().items());
            group.addMailboxParcel(parcel);
        }

        setDirty();
        return true;
    }

    public Optional<MailboxParcel> collectMailboxParcel(UUID groupId, UUID parcelId) {
        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return Optional.empty();
        }

        Optional<MailboxParcel> parcel = group.removeMailboxParcel(parcelId);

        if (parcel.isEmpty()) {
            return Optional.empty();
        }

        setDirty();
        return parcel;
    }

    public List<DeliveryGroup> getInvitationsForPlayer(UUID playerId) {
        return groups.values().stream().filter(group -> group.hasInvitation(playerId)).toList();
    }

    public boolean invitePlayer(UUID groupId, UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return false;
        }

        if (getGroupForPlayer(playerId).isPresent()) {
            return false;
        }

        if (!group.invite(playerId)) {
            return false;
        }

        setDirty();
        return true;
    }

    public boolean acceptInvitation(UUID groupId, UUID playerId) {
        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return false;
        }

        if (!group.hasInvitation(playerId)) {
            return false;
        }

        if (getGroupForPlayer(playerId).isPresent()) {
            return false;
        }

        if (!group.addMember(playerId)) {
            return false;
        }

        clearInvitationsForPlayer(playerId);

        setDirty();
        return true;
    }

    public boolean declineInvitation(UUID groupId, UUID playerId) {
        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return false;
        }

        if (!group.declineInvitation(playerId)) {
            return false;
        }

        setDirty();
        return true;
    }

    public boolean transferOwnership(UUID groupId, UUID currentOwner, UUID newOwner) {
        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return false;
        }

        if (!group.isOwner(currentOwner)) {
            return false;
        }

        if (!group.transferOwnership(newOwner)) {
            return false;
        }

        setDirty();
        return true;
    }

    public void reconcileProgression() {
        boolean changed = false;

        for (DeliveryGroup group : groups.values()) {
            if (ProgressionManager.reconcileGroup(group)) {
                changed = true;
            }
        }

        if (changed) {
            setDirty();
        }
    }

    public boolean markEmailRead(UUID groupId, UUID emailId) {
        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return false;
        }

        if (!group.markEmailRead(emailId)) {
            return false;
        }

        setDirty();
        return true;
    }

    public int validateEmails(UUID groupId) {
        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return 0;
        }

        int removed = 0;

        for (GroupEmail email : new ArrayList<>(group.emails())) {
            boolean valid = switch (email.type()) {
                case CONTRACT -> TaskManager.contains(email.referenceId());
                case OFFER -> OfferManager.contains(email.referenceId());
                case JOB -> JobManager.contains(email.referenceId());
            };

            if (valid) {
                continue;
            }

            if (group.removeEmail(email.id())) {
                removed++;
            }
        }

        if (removed > 0) {
            setDirty();
        }

        return removed;
    }

    public boolean removeEmail(UUID groupId, UUID emailId) {
        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return false;
        }

        if (!group.removeEmail(emailId)) {
            return false;
        }

        setDirty();
        return true;
    }

    public int generateJobEmails() {
        int generated = 0;

        for (DeliveryGroup group : groups.values()) {
            if (generateJobEmail(group).isPresent()) {
                generated++;
            }
        }

        return generated;
    }

    public Optional<GroupEmail> generateJobEmail(DeliveryGroup group) {
        Objects.requireNonNull(group, "group");

        if (!group.computerUnlocked()) {
            return Optional.empty();
        }

        validateEmails(group.id());

        if (group.unacceptedJobEmailCount() >= 3L) {
            return Optional.empty();
        }

        if (group.activeJobs().size() >= 3) {
            return Optional.empty();
        }

        List<JobDefinition> possibleJobs = JobManager.getJobs().stream().filter(job ->
                JobRuntimeManager.getAcceptanceFailure(group, job).isEmpty()).filter(job -> !group.hasJobEmail(job.id())).toList();

        if (possibleJobs.isEmpty()) {
            return Optional.empty();
        }

        JobDefinition job = possibleJobs.get(ThreadLocalRandom.current().nextInt(possibleJobs.size()));
        GroupEmail email = GroupEmail.job(job);

        if (!group.addEmail(email)) {
            return Optional.empty();
        }

        setDirty();
        return Optional.of(email);
    }

    public Optional<GroupEmail> generateContractEmail(DeliveryGroup group) {
        Objects.requireNonNull(group, "group");

        if (!group.computerUnlocked()) {
            return Optional.empty();
        }

        validateEmails(group.id());

        if (group.unacceptedContractEmailCount() >= 3L) {
            return Optional.empty();
        }

        if (group.activeTasks().size() >= 5) {
            return Optional.empty();
        }

        List<TaskDefinition> possibleTasks = TaskManager.getTasks().stream().filter(task -> !task.forced())
                .filter(task -> TaskRuntimeManager.getAcceptanceFailure(group, task).isEmpty())
                .filter(task -> !group.hasContractEmail(task.id())).toList();

        if (possibleTasks.isEmpty()) {
            return Optional.empty();
        }

        TaskDefinition task = possibleTasks.get(ThreadLocalRandom.current().nextInt(possibleTasks.size()));
        GroupEmail email = GroupEmail.contract(task);

        if (!group.addEmail(email)) {
            return Optional.empty();
        }

        setDirty();
        return Optional.of(email);
    }

    public int generateContractEmails() {
        int generated = 0;

        for (DeliveryGroup group : groups.values()) {
            if (generateContractEmail(group).isPresent()) {
                generated++;
            }
        }

        return generated;
    }

    private void validateMembership() {
        Set<UUID> seenPlayers = new HashSet<>();

        for (DeliveryGroup group : groups.values()) {
            for (UUID member : group.members()) {
                if (!seenPlayers.add(member)) {
                    throw new IllegalStateException("Player " + member + " belongs to multiple Delivery Questing groups");
                }
            }
        }
    }

    private void clearInvitationsForPlayer(UUID playerId) {
        for (DeliveryGroup group : groups.values()) {
            group.declineInvitation(playerId);
        }
    }

    public PurchaseResult purchaseOffer(UUID groupId, Identifier offerId) {
        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return new PurchaseResult(false, "Delivery group no longer exists.", 0L);
        }

        Optional<OfferDefinition> optionalOffer = OfferManager.getOffer(offerId);

        if (optionalOffer.isEmpty()) {
            return new PurchaseResult(false, "That Minazon offer no longer exists.", group.balance());
        }

        OfferDefinition offer = optionalOffer.get();

        if (!group.computerUnlocked()) {
            return new PurchaseResult(false, "Your group has not unlocked the Computer.", group.balance());
        }

        if (group.wholeLevel() < offer.minLevel()) {
            return new PurchaseResult(false, "That offer requires delivery level " + offer.minLevel() + ".", group.balance());
        }

        if (group.balance() < offer.price()) {
            return new PurchaseResult(false, "Your group does not have enough money.", group.balance());
        }

        Item item = BuiltInRegistries.ITEM.getValue(offer.item());

        if (item == null || item == Items.AIR) {
            return new PurchaseResult(false, "That offer contains an invalid item.", group.balance());
        }

        int multiplier = offer.forEveryMember() ? group.members().size() : 1;
        long totalCount;

        try {
            totalCount = Math.multiplyExact((long) offer.count(), multiplier);

        } catch (ArithmeticException exception) {
            return new PurchaseResult(false, "The resulting order is too large.", group.balance());
        }

        if (totalCount > Integer.MAX_VALUE) {
            return new PurchaseResult(false, "The resulting order is too large.", group.balance());
        }

        if (!group.spendBalance(offer.price())) {
            return new PurchaseResult(false, "Failed to charge the group balance.", group.balance());
        }

        MailboxParcel parcel = MailboxParcel.create("Minazon", List.of(new ItemReward(offer.item(), (int) totalCount)));

        group.addPendingDelivery(parcel);

        setDirty();
        return new PurchaseResult(true, "Purchased " + totalCount + "x " + offer.item() + " for " + offer.price()
                + ". Delivery is scheduled for the next morning.", group.balance());
    }

    public Optional<UUID> acceptJob(UUID groupId, Identifier jobId) {
        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return Optional.empty();
        }

        Optional<DeliveryJobProgress> progress = group.addActiveJob(jobId);

        if (progress.isEmpty()) {
            return Optional.empty();
        }

        setDirty();
        return Optional.of(progress.get().instanceId());
    }

    public long addJobProgress(UUID groupId, UUID instanceId, TaskRequirement requirement, long amount) {
        if (amount <= 0L) {
            return 0L;
        }

        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return 0L;
        }

        Optional<DeliveryJobProgress> optionalProgress = group.getActiveJob(instanceId);

        if (optionalProgress.isEmpty()) {
            return 0L;
        }

        long accepted = optionalProgress.get().addProgress(requirement, amount);

        if (accepted > 0L) {
            setDirty();
        }

        return accepted;
    }

    public boolean completeJob(UUID groupId, UUID instanceId, JobDefinition job) {
        DeliveryGroup group = groups.get(groupId);

        if (group == null) {
            return false;
        }

        Optional<DeliveryJobProgress> optionalProgress = group.getActiveJob(instanceId);

        if (optionalProgress.isEmpty()) {
            return false;
        }

        DeliveryJobProgress progress = optionalProgress.get();

        if (!progress.jobId().equals(job.id())) {
            return false;
        }

        if (!progress.isComplete(job)) {
            return false;
        }

        if (!group.removeActiveJob(instanceId)) {
            return false;
        }

        int previousLevel = group.wholeLevel();

        group.addExperience(job.rewards().experience());

        int currentLevel = group.wholeLevel();
        ProgressionManager.processLevelChange(group, previousLevel, currentLevel);

        group.addBalance(job.rewards().money());

        if (!job.rewards().items().isEmpty()) {
            MailboxParcel parcel = MailboxParcel.create(job.contractor().name(), job.rewards().items());

            group.addMailboxParcel(parcel);
        }

        setDirty();
        return true;
    }

    public int deliverPendingDeliveries() {
        int groupsDelivered = 0;

        for (DeliveryGroup group : groups.values()) {
            if (!group.deliverPendingDeliveries()) {
                continue;
            }

            groupsDelivered++;
        }

        if (groupsDelivered > 0) {
            setDirty();
        }

        return groupsDelivered;
    }

    public record PurchaseResult(boolean success, String message, long balance) {
    }
}
