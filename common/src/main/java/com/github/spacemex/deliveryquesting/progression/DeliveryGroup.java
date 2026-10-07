package com.github.spacemex.deliveryquesting.progression;

import com.github.spacemex.deliveryquesting.progression.entry.MailboxParcel;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;

import java.util.*;

public final class DeliveryGroup {

    public static final int MAILBOX_INBOX_SIZE = 4;

    public static final Codec<DeliveryGroup> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                            UUIDUtil.CODEC.fieldOf("id")
                                    .forGetter(DeliveryGroup::id),
                            Codec.STRING.fieldOf("name")
                                    .forGetter(DeliveryGroup::name),
                            UUIDUtil.CODEC.optionalFieldOf("owner")
                                    .forGetter(group -> Optional.of(group.owner)),
                            UUIDUtil.CODEC.listOf()
                                    .optionalFieldOf("members", List.of())
                                    .forGetter(group -> List.copyOf(group.members)),
                            UUIDUtil.CODEC.listOf()
                                    .optionalFieldOf("pending_invitations", List.of())
                                    .forGetter(group -> List.copyOf(group.pendingInvitations)),
                            TaskProgress.CODEC.listOf()
                                    .optionalFieldOf("active_tasks", List.of())
                                    .forGetter(group -> List.copyOf(group.activeTasks.values())),
                            DeliveryJobProgress.CODEC.listOf()
                                    .optionalFieldOf("active_jobs", List.of())
                                    .forGetter(group -> List.copyOf(group.activeJobs.values())),
                            Identifier.CODEC.listOf()
                                    .optionalFieldOf("completed_tasks", List.of())
                                    .forGetter(group -> List.copyOf(group.completedTasks)),
                            Codec.LONG.optionalFieldOf("experience", 0L)
                                    .forGetter(DeliveryGroup::experience),
                            Codec.LONG.optionalFieldOf("balance", 0L)
                                    .forGetter(DeliveryGroup::balance),
                            Codec.BOOL.optionalFieldOf("computer_unlocked", false)
                                    .forGetter(DeliveryGroup::computerUnlocked),
                            Codec.BOOL.optionalFieldOf("computer_reward_delivered", false)
                                    .forGetter(DeliveryGroup::computerRewardDelivered),
                            GroupEmail.CODEC.listOf()
                                    .optionalFieldOf("emails", List.of())
                                    .forGetter(group -> List.copyOf(group.emails)),
                            MailboxParcel.CODEC.listOf()
                                    .optionalFieldOf("mailbox_inbox", List.of())
                                    .forGetter(group -> List.copyOf(group.mailboxInbox)),
                            MailboxParcel.CODEC.listOf().optionalFieldOf("pending_mailbox", List.of())
                                    .forGetter(group -> List.copyOf(group.pendingMailbox)),
                            MailboxParcel.CODEC.listOf()
                                    .optionalFieldOf("pending_deliveries", List.of())
                                    .forGetter(group -> List.copyOf(group.pendingDeliveries)))
                    .apply(instance, DeliveryGroup::new));

    private final UUID id;

    private UUID owner;

    private final String name;

    private final Set<UUID> members;

    private final Map<Identifier, TaskProgress> activeTasks;

    private final Set<Identifier> completedTasks;

    private final List<MailboxParcel> mailboxInbox;
    private final List<MailboxParcel> pendingMailbox;

    private final Set<UUID> pendingInvitations;

    private long experience;
    private long balance;

    private boolean computerUnlocked;
    private boolean computerRewardDelivered;

    private final List<GroupEmail> emails;
    private final List<MailboxParcel> pendingDeliveries;

    private final Map<UUID, DeliveryJobProgress> activeJobs;

    private DeliveryGroup(UUID id, String name, Optional<UUID> owner, List<UUID> members, List<UUID> pendingInvitations,
                          List<TaskProgress> activeTasks, List<DeliveryJobProgress> activeJobs,
                          List<Identifier> completedTasks, long experience, long balance,
                          boolean computerUnlocked, boolean computerRewardDelivered, List<GroupEmail> emails,
                          List<MailboxParcel> mailboxInbox, List<MailboxParcel> pendingMailbox,
                          List<MailboxParcel> pendingDeliveries) {
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

        if (this.members.isEmpty()) {
            throw new IllegalArgumentException("Group '" + name + "' must contain at least one member");
        }

        this.owner = owner.orElseGet(() -> this.members.iterator().next());

        if (!this.members.contains(this.owner)) {
            throw new IllegalArgumentException("Group owner must also be a member of the group");
        }

        this.pendingInvitations = new LinkedHashSet<>(Objects.requireNonNull(pendingInvitations,
                "pendingInvitations"));

        for (UUID member : this.members) {
            if (this.pendingInvitations.contains(member)) {
                throw new IllegalArgumentException("Group member " + member
                        + " cannot also have a pending invitation");
            }
        }

        this.activeTasks = new LinkedHashMap<>();

        for (TaskProgress task : Objects.requireNonNull(activeTasks, "activeTasks")) {
            TaskProgress existing = this.activeTasks.putIfAbsent(task.taskId(), task);

            if (existing != null) {
                throw new IllegalArgumentException("Group '" + name +
                        "' contains duplicate active task '" + task.taskId() + "'");
            }
        }

        this.activeJobs = new LinkedHashMap<>();

        for (DeliveryJobProgress job : Objects.requireNonNull(activeJobs, "activeJobs")) {
            DeliveryJobProgress existing = this.activeJobs.putIfAbsent(job.instanceId(), job);

            if (existing != null) {
                throw new IllegalArgumentException("Group '" + name +
                        "' contains duplicate active job instance '" + job.instanceId() + "'");
            }

            long sameDefinition = this.activeJobs.values().stream()
                    .filter(active -> active.jobId().equals(job.jobId())).count();

            if (sameDefinition > 1L) {
                throw new IllegalArgumentException("Group '" + name +
                        "' contains multiple active instances of job '" + job.jobId() + "'");
            }
        }

        this.completedTasks = new LinkedHashSet<>(Objects.requireNonNull(completedTasks, "completedTasks"));

        for (Identifier completed : this.completedTasks) {
            if (this.activeTasks.containsKey(completed)) {
                throw new IllegalArgumentException("Task '" + completed + "' cannot be active and completed");
            }
        }

        this.emails = new ArrayList<>(Objects.requireNonNull(emails, "emails"));

        validateEmails();

        Objects.requireNonNull(mailboxInbox, "mailboxInbox");
        Objects.requireNonNull(pendingMailbox, "pendingMailbox");

        if (mailboxInbox.size() > MAILBOX_INBOX_SIZE) {
            throw new IllegalArgumentException("Mailbox inbox cannot contain more than "
                    + MAILBOX_INBOX_SIZE + " parcels");
        }

        this.mailboxInbox = new ArrayList<>(mailboxInbox);

        this.pendingMailbox = new ArrayList<>(pendingMailbox);

        this.pendingDeliveries = new ArrayList<>(Objects.requireNonNull(pendingDeliveries,
                "pendingDeliveries"));

        validateMailboxParcels();

        this.experience = experience;

        this.balance = balance;

        this.computerUnlocked = computerUnlocked;

        this.computerRewardDelivered = computerRewardDelivered;
    }

    static DeliveryGroup create(UUID id, String name, UUID owner) {
        return new DeliveryGroup(
                id, name,
                Optional.of(owner), List.of(owner), List.of(), List.of(), List.of(), List.of(),
                0L, 0L, false, false,
                List.of(), List.of(), List.of(), List.of());
    }

    public Collection<DeliveryJobProgress> activeJobs() {
        return Collections.unmodifiableCollection(activeJobs.values());
    }

    public Optional<DeliveryJobProgress> getActiveJob(UUID instanceId) {
        return Optional.ofNullable(activeJobs.get(instanceId));
    }

    public boolean hasActiveJobDefinition(Identifier jobId) {
        return activeJobs.values().stream().anyMatch(job -> job.jobId().equals(jobId));
    }

    public List<MailboxParcel> pendingDeliveries() {
        return Collections.unmodifiableList(pendingDeliveries);
    }

    public boolean hasOfferEmail(Identifier offerId) {
        return emails.stream().anyMatch(email -> email.type() ==
                GroupEmail.Type.OFFER && email.referenceId().equals(offerId));
    }

    Optional<DeliveryJobProgress> addActiveJob(Identifier jobId) {
        if (hasActiveJobDefinition(jobId)) {
            return Optional.empty();
        }

        DeliveryJobProgress progress = new DeliveryJobProgress(jobId);

        activeJobs.put(progress.instanceId(), progress);

        return Optional.of(progress);
    }

    void addPendingDelivery(MailboxParcel parcel) {
        Objects.requireNonNull(parcel, "parcel");

        pendingDeliveries.add(parcel);
    }

    boolean removeActiveJob(UUID instanceId) {
        return activeJobs.remove(instanceId) != null;
    }

    public long unacceptedJobEmailCount() {
        return emails.stream().filter(email -> email.type() == GroupEmail.Type.JOB).count();
    }

    public boolean hasJobEmail(Identifier jobId) {
        return emails.stream().anyMatch(email ->
                email.type() == GroupEmail.Type.JOB && email.referenceId().equals(jobId));
    }

    boolean deliverPendingDeliveries() {
        if (pendingDeliveries.isEmpty()) {
            return false;
        }

        List<MailboxParcel> deliveries = new ArrayList<>(pendingDeliveries);

        pendingDeliveries.clear();

        for (MailboxParcel parcel : deliveries) {
            addMailboxParcel(parcel);
        }

        return true;
    }

    public boolean computerUnlocked() {
        return computerUnlocked;
    }

    public boolean computerRewardDelivered() {
        return computerRewardDelivered;
    }

    boolean markComputerRewardDelivered() {
        if (computerRewardDelivered) {
            return false;
        }

        computerRewardDelivered = true;

        return true;
    }

    public UUID owner() {
        return owner;
    }

    public boolean isOwner(UUID playerId) {
        return owner.equals(playerId);
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

    @SuppressWarnings("all")
    boolean addMember(UUID playerId) {
        if (!members.add(playerId)) {
            return false;
        }

        pendingInvitations.remove(playerId);

        return true;
    }

    boolean removeMember(UUID playerId) {
        if (isOwner(playerId)) {
            return false;
        }

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

    boolean invite(UUID playerId) {
        if (members.contains(playerId)) {
            return false;
        }

        return pendingInvitations.add(playerId);
    }

    boolean declineInvitation(UUID playerId) {
        return pendingInvitations.remove(playerId);
    }

    boolean transferOwnership(UUID newOwner) {
        if (!members.contains(newOwner)) {
            return false;
        }

        if (owner.equals(newOwner)) {
            return false;
        }

        owner = newOwner;

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

    Optional<MailboxParcel> removeMailboxParcel(UUID parcelId) {
        Objects.requireNonNull(parcelId, "parcelId");

        for (int i = 0; i < mailboxInbox.size(); i++) {
            MailboxParcel parcel = mailboxInbox.get(i);

            if (!parcel.id().equals(parcelId)) {
                continue;
            }

            mailboxInbox.remove(i);

            promotePendingMailbox();

            return Optional.of(parcel);
        }

        return Optional.empty();
    }

    boolean unlockComputer() {
        if (computerUnlocked) {
            return false;
        }

        computerUnlocked = true;

        return true;
    }

    boolean addEmail(GroupEmail email) {
        Objects.requireNonNull(email, "email");

        if (getEmail(email.id()).isPresent()) {
            return false;
        }

        emails.add(email);

        return true;
    }

    boolean markEmailRead(UUID emailId) {
        return getEmail(emailId).map(GroupEmail::markRead).orElse(false);
    }

    boolean removeEmail(UUID emailId) {
        return emails.removeIf(email -> email.id().equals(emailId));
    }

    private void promotePendingMailbox() {
        while (mailboxInbox.size() < MAILBOX_INBOX_SIZE && !pendingMailbox.isEmpty()) {
            mailboxInbox.add(pendingMailbox.removeFirst());
        }
    }

    public List<GroupEmail> emails() {
        return Collections.unmodifiableList(emails);
    }

    public Optional<GroupEmail> getEmail(UUID emailId) {
        return emails.stream().filter(email -> email.id().equals(emailId)).findFirst();
    }

    public long unreadEmailCount() {
        return emails.stream().filter(email -> !email.read()).count();
    }

    public long unacceptedContractEmailCount() {
        return emails.stream().filter(email -> email.type() == GroupEmail.Type.CONTRACT)
                .filter(email ->
                        !hasActiveTask(email.referenceId())).filter(email ->
                        !hasCompletedTask(email.referenceId())).count();
    }

    public boolean hasContractEmail(Identifier taskId) {
        return emails.stream().anyMatch(email ->
                email.type() == GroupEmail.Type.CONTRACT && email.referenceId().equals(taskId));
    }

    public Set<UUID> pendingInvitations() {
        return Collections.unmodifiableSet(pendingInvitations);
    }

    public boolean hasInvitation(UUID playerId) {
        return pendingInvitations.contains(playerId);
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

        for (MailboxParcel parcel : pendingDeliveries) {
            if (!ids.add(parcel.id())) {
                throw new IllegalArgumentException("Duplicate pending delivery parcel ID: " + parcel.id());
            }
        }
    }

    private void validateEmails() {
        Set<UUID> ids = new HashSet<>();

        for (GroupEmail email : emails) {
            if (!ids.add(email.id())) {
                throw new IllegalArgumentException("Duplicate group email ID: " + email.id());
            }
        }
    }
}