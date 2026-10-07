package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.block.entity.ComputerBlockEntity;
import com.github.spacemex.deliveryquesting.config.ConfigReader;
import com.github.spacemex.deliveryquesting.job.JobManager;
import com.github.spacemex.deliveryquesting.menu.entry.ComputerInboxEntry;
import com.github.spacemex.deliveryquesting.menu.entry.ComputerJobMailEntry;
import com.github.spacemex.deliveryquesting.menu.entry.ComputerMailEntry;
import com.github.spacemex.deliveryquesting.menu.entry.ComputerOfferEntry;
import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.github.spacemex.deliveryquesting.progression.GroupEmail;
import com.github.spacemex.deliveryquesting.registry.ModBlocks;
import com.github.spacemex.deliveryquesting.registry.ModMenus;
import com.github.spacemex.deliveryquesting.task.definition.OfferDefinition;
import com.github.spacemex.deliveryquesting.task.manager.OfferManager;
import com.github.spacemex.deliveryquesting.task.manager.TaskManager;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.*;

public final class ComputerMenu extends AbstractContainerMenu {

    private final BlockPos blockPos;

    private final String groupName;

    private final int level;
    private final int activeTasks;
    private final int completedTasks;

    private final long balance;

    private final List<ComputerMailEntry> mail;

    private final List<ComputerInboxEntry> inbox;

    private final List<ComputerOfferEntry> offers;

    private final List<ComputerJobMailEntry> jobMail;


    public ComputerMenu(int containerId, Inventory inventory, BlockPos blockPos, String groupName, int level,
                        long balance, int activeTasks, int completedTasks, List<ComputerInboxEntry> inbox,
                        List<ComputerMailEntry> mail, List<ComputerOfferEntry> offers,
                        List<ComputerJobMailEntry> jobMail) {
        super(ModMenus.COMPUTER.get(), containerId);

        this.blockPos = blockPos;

        this.groupName = groupName;

        this.level = level;

        this.balance = balance;

        this.activeTasks = activeTasks;

        this.completedTasks = completedTasks;

        this.inbox = List.copyOf(inbox);

        this.mail = List.copyOf(mail);

        this.offers = List.copyOf(offers);

        this.jobMail = List.copyOf(jobMail);
    }

    public static ComputerMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos blockPos = buffer.readBlockPos();

        String groupName = buffer.readUtf(256);

        int level = buffer.readVarInt();

        long balance = buffer.readLong();

        int activeTasks = buffer.readVarInt();

        int completedTasks = buffer.readVarInt();

        List<ComputerInboxEntry> inbox = ComputerInboxEntry.readList(buffer);

        List<ComputerMailEntry> mail = ComputerMailEntry.readList(buffer);

        List<ComputerOfferEntry> offers = ComputerOfferEntry.readList(buffer);

        List<ComputerJobMailEntry> jobMail = ComputerJobMailEntry.readList(buffer);

        return new ComputerMenu(containerId, inventory, blockPos, groupName, level, balance, activeTasks,
                completedTasks, inbox, mail, offers, jobMail);
    }

    public static void open(ServerPlayer player, BlockPos pos, ComputerBlockEntity computer) {
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(player.level().getServer());

        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            player.sendSystemMessage(Component.literal("You must be in a delivery group to use a Computer."));
            return;
        }

        DeliveryGroup group = optionalGroup.get();

        if (!group.computerUnlocked()) {
            player.sendSystemMessage(Component.literal("Computer access requires delivery level "
                    + ConfigReader.getMinComputerLevel() + "."));

            return;
        }

        Optional<UUID> boundGroup = computer.groupId();

        if (boundGroup.isEmpty()) {
            computer.bindToGroup(group.id());
        } else if (!boundGroup.get().equals(group.id())) {
            if (data.getGroup(boundGroup.get()).isPresent()) {
                player.sendSystemMessage(Component.literal("This Computer belongs to another delivery group."));

                return;
            }

            computer.rebindToGroup(group.id());

            player.sendSystemMessage(Component.literal("Reclaimed abandoned Computer for '"
                    + group.name() + "'."));
        }

        data.validateEmails(group.id());

        List<ComputerInboxEntry> inbox = createInboxEntries(group);

        List<ComputerMailEntry> mail = createMailEntries(group);

        List<ComputerOfferEntry> offers = createOfferEntries(group);

        List<ComputerJobMailEntry> jobMail = createJobMailEntries(group);

        SimpleMenuProvider provider = new SimpleMenuProvider((containerId, inventory, menuPlayer) ->
                new ComputerMenu(containerId, inventory, pos, group.name(), group.wholeLevel(), group.balance(),
                        group.activeTasks().size(), group.completedTasks().size(), inbox, mail, offers, jobMail),

                Component.translatable("screen.delivery_questing.computer"));

        MenuRegistry.openExtendedMenu(player, provider, buffer -> {
            buffer.writeBlockPos(pos);

            buffer.writeUtf(group.name(), 256);

            buffer.writeVarInt(group.wholeLevel());

            buffer.writeLong(group.balance());

            buffer.writeVarInt(group.activeTasks().size());

            buffer.writeVarInt(group.completedTasks().size());

            ComputerInboxEntry.writeList(buffer, inbox);

            ComputerMailEntry.writeList(buffer, mail);

            ComputerOfferEntry.writeList(buffer, offers);

            ComputerJobMailEntry.writeList(buffer, jobMail);
        });
    }

    public List<ComputerJobMailEntry> jobMail() {
        return jobMail;
    }

    public String groupName() {
        return groupName;
    }

    public int level() {
        return level;
    }

    public long balance() {
        return balance;
    }

    public int activeTasks() {
        return activeTasks;
    }

    public int completedTasks() {
        return completedTasks;
    }

    public List<ComputerInboxEntry> inbox() {
        return inbox;
    }

    public List<ComputerOfferEntry> offers() {
        return offers;
    }

    public boolean hasEmail(UUID emailId) {
        return inbox.stream().anyMatch(email -> email.emailId().equals(emailId));
    }

    public boolean hasOffer(Identifier offerId) {
        return offers.stream().anyMatch(offer -> offer.id().equals(offerId));
    }

    public List<ComputerMailEntry> mail() {
        return mail;
    }

    public boolean hasMail(UUID emailId) {
        return mail.stream().anyMatch(email -> email.emailId().equals(emailId));
    }

    public long unreadMailCount() {
        return inbox.stream().filter(email -> !email.read()).count();
    }

    private static List<ComputerMailEntry> createMailEntries(DeliveryGroup group) {
        List<ComputerMailEntry> result = new ArrayList<>();

        for (GroupEmail email :
                group.emails()) {

            if (email.type() != GroupEmail.Type.CONTRACT) {
                continue;
            }

            TaskManager.getTask(email.referenceId()).ifPresent(task ->
                    result.add(ComputerMailEntry.from(email, group, task)));
        }

        Collections.reverse(result);

        return List.copyOf(result);
    }

    private static List<ComputerInboxEntry> createInboxEntries(DeliveryGroup group) {
        List<ComputerInboxEntry> result = new ArrayList<>();

        List<GroupEmail> emails = new ArrayList<>(group.emails());

        Collections.reverse(emails);

        for (GroupEmail email : emails) {
            switch (email.type()) {
                case CONTRACT -> TaskManager.getTask(email.referenceId()).ifPresent(task -> result.add(
                        new ComputerInboxEntry(email.id(), email.type(), email.read(), email.referenceId(),
                                task.name(), task.contractor().name())));
                case OFFER -> OfferManager.getOffer(email.referenceId()).ifPresent(offer -> result.add(
                        new ComputerInboxEntry(email.id(), email.type(), email.read(), email.referenceId(),
                                "Now available: " + offer.item(), "Minazon")));
                case JOB -> JobManager.getJob(email.referenceId()).ifPresent(job -> result.add(
                        new ComputerInboxEntry(email.id(), email.type(), email.read(), email.referenceId(), job.name(),
                                job.contractor().name())));
            }
        }

        return List.copyOf(result);
    }

    private static List<ComputerOfferEntry> createOfferEntries(DeliveryGroup group) {
        return OfferManager.getOffers().stream().sorted(Comparator.comparingInt(OfferDefinition::minLevel)
                .thenComparing(offer -> offer.id().toString())).map(offer ->
                ComputerOfferEntry.from(offer, group)).toList();
    }

    public boolean hasJobMail(UUID emailId) {
        return jobMail.stream().anyMatch(entry -> entry.emailId().equals(emailId));
    }

    private static List<ComputerJobMailEntry> createJobMailEntries(DeliveryGroup group) {
        List<ComputerJobMailEntry> result = new ArrayList<>();

        for (GroupEmail email : group.emails()) {
            if (email.type() != GroupEmail.Type.JOB) {
                continue;
            }

            JobManager.getJob(email.referenceId()).ifPresent(job ->
                    result.add(ComputerJobMailEntry.from(email, group, job)));
        }

        Collections.reverse(result);

        return List.copyOf(result);
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!player.level().getBlockState(blockPos).is(ModBlocks.COMPUTER.get())) {
            return false;
        }

        double x = blockPos.getX() + 0.5D;
        double y = blockPos.getY() + 0.5D;
        double z = blockPos.getZ() + 0.5D;

        return player.distanceToSqr(x, y, z) <= 64.0D;
    }
}