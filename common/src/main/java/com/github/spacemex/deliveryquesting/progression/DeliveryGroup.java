package com.github.spacemex.deliveryquesting.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;

import java.util.*;

public final class DeliveryGroup {
    public static final int MAILBOX_INBOX_SIZE = 4;

    public static final Codec<DeliveryGroup> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                            UUIDUtil.CODEC.fieldOf("id").forGetter(DeliveryGroup::id),
                            Codec.STRING.fieldOf("name").forGetter(DeliveryGroup::name),
                            UUIDUtil.CODEC.listOf().optionalFieldOf("members", List.of())
                                    .forGetter(group -> List.copyOf(group.members)),
                            TaskProgress.CODEC.listOf().optionalFieldOf("active_tasks", List.of())
                                    .forGetter(group -> List.copyOf(group.activeTasks.values())),
                            Identifier.CODEC.listOf().optionalFieldOf("completed_tasks", List.of())
                                    .forGetter(group -> List.copyOf(group.completedTasks)),
                            Codec.LONG.optionalFieldOf("experience", 0L).forGetter(DeliveryGroup::experience),
                            Codec.LONG.optionalFieldOf("balance", 0L).forGetter(DeliveryGroup::balance),
                            MailboxParcel.CODEC.listOf().optionalFieldOf("mailbox_inbox", List.of())
                                    .forGetter(group -> List.copyOf(group.mailboxInbox)),
                            MailboxParcel.CODEC.listOf().optionalFieldOf("pending_mailbox", List.of())
                                    .forGetter(group -> List.copyOf(group.pendingMailbox)))
                    .apply(instance, DeliveryGroup::new));
    private final UUID id;
    private final String name;
    private final Set<UUID> members;
    private final Map<Identifier, TaskProgress> activeTasks;
    private final Set<Identifier> completedTasks;
    private final List<MailboxParcel> mailboxInbox;
    private final List<MailboxParcel> pendingMailbox;
    private long experience;
    private long balance;

    private DeliveryGroup(UUID id, String name, List<UUID> members, List<TaskProgress> activeTasks, List<Identifier> completedTasks,
                          long experience, long balance, List<MailboxParcel> mailboxInbox, List<MailboxParcel> pendingMailbox) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be blank");
        }

        if (experience < 0L) {
            throw new IllegalArgumentException("Group experience cannot be negative");
        }

        if (balance < 0L) {
            throw new IllegalArgumentException("Group balance cannot be negative");
        }

        this.members = new LinkedHashSet<>(Objects.requireNonNull(members, "members"));
        this.activeTasks = new LinkedHashMap<>();

        for (TaskProgress task : Objects.requireNonNull(activeTasks, "activeTasks")) {
            TaskProgress existing = this.activeTasks.putIfAbsent(task.taskId(), task);

            if (existing != null) {
                throw new IllegalArgumentException("Group '" + name + "' contains duplicate active task '" + task.taskId() + "'");
            }
        }

        this.completedTasks = new LinkedHashSet<>(Objects.requireNonNull(completedTasks, "completedTasks"));

        for (Identifier completed : this.completedTasks) {

            if (this.activeTasks.containsKey(completed)) {
                throw new IllegalArgumentException("Task '" + completed + "' cannot be active and completed");
            }
        }

        Objects.requireNonNull(mailboxInbox, "mailboxInbox");
        Objects.requireNonNull(pendingMailbox, "pendingMailbox");

        if (mailboxInbox.size() > MAILBOX_INBOX_SIZE) {
            throw new IllegalArgumentException("Mailbox inbox cannot contain more than " + MAILBOX_INBOX_SIZE + " parcels");
        }

        this.mailboxInbox = new ArrayList<>(mailboxInbox);
        this.pendingMailbox = new ArrayList<>(pendingMailbox);

        validateMailboxParcels();

        this.experience = experience;
        this.balance = balance;
    }

    static DeliveryGroup create(UUID id, String name, UUID owner) {
        return new DeliveryGroup(id, name, List.of(owner), List.of(), List.of(), 0L, 0L, List.of(), List.of());
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Set<UUID> members() {
        return Collections.unmodifiableSet(members);
    }

    public Collection<TaskProgress> activeTasks() {
        return Collections.unmodifiableCollection(activeTasks.values());
    }

    public Set<Identifier> completedTasks() {
        return Collections.unmodifiableSet(completedTasks);
    }

    public List<MailboxParcel> mailboxInbox() {
        return Collections.unmodifiableList(mailboxInbox);
    }

    public List<MailboxParcel> pendingMailbox() {
        return Collections.unmodifiableList(pendingMailbox);
    }

    public long experience() {
        return experience;
    }

    public long balance() {
        return balance;
    }

    public double level() {
        return levelFromExperience(experience);
    }

    public int wholeLevel() {
        return (int) Math.floor(level());
    }

    public static double levelFromExperience(long experience) {
        return Math.sqrt(0.2D * experience + 0.25D) - 0.5D;
    }

    public boolean hasMember(UUID playerId) {
        return members.contains(playerId);
    }

    public boolean hasActiveTask(Identifier taskId) {
        return activeTasks.containsKey(taskId);
    }

    public boolean hasCompletedTask(Identifier taskId) {
        return completedTasks.contains(taskId);
    }

    public Optional<TaskProgress> getActiveTask(Identifier taskId) {
        return Optional.ofNullable(activeTasks.get(taskId));
    }

    public Optional<MailboxParcel> getMailboxParcel(UUID parcelId) {
        return mailboxInbox.stream().filter(parcel -> parcel.id().equals(parcelId)).findFirst();
    }

    boolean addMember(UUID playerId) {
        return members.add(playerId);
    }

    boolean removeMember(UUID playerId) {
        return members.remove(playerId);
    }

    void addExperience(long amount) {
        if (amount <= 0L) {
            return;
        }

        experience = Math.addExact(experience, amount);
    }

    void addBalance(long amount) {
        if (amount <= 0L) {
            return;
        }

        balance = Math.addExact(balance, amount);
    }

    boolean spendBalance(long amount) {
        if (amount <= 0L || balance < amount) {
            return false;
        }

        balance -= amount;
        return true;
    }

    boolean addActiveTask(Identifier taskId) {
        if (completedTasks.contains(taskId) || activeTasks.containsKey(taskId)) {
            return false;
        }

        activeTasks.put(taskId, new TaskProgress(taskId));
        return true;
    }

    boolean markTaskCompleted(Identifier taskId) {
        TaskProgress removed = activeTasks.remove(taskId);

        if (removed == null) {
            return false;
        }

        completedTasks.add(taskId);
        return true;
    }

    void addMailboxParcel(MailboxParcel parcel) {
        Objects.requireNonNull(parcel, "parcel");

        if (mailboxInbox.size() < MAILBOX_INBOX_SIZE) {
            mailboxInbox.add(parcel);
            return;
        }

        pendingMailbox.add(parcel);
    }

    private void validateMailboxParcels() {
        Set<UUID> ids = new HashSet<>();

        for (MailboxParcel parcel : mailboxInbox) {
            if (!ids.add(parcel.id())) {
                throw new IllegalArgumentException("Duplicate mailbox parcel ID: " + parcel.id());
            }
        }

        for (MailboxParcel parcel : pendingMailbox) {

            if (!ids.add(parcel.id())) {
                throw new IllegalArgumentException("Duplicate mailbox parcel ID: " + parcel.id());
            }
        }
    }
}