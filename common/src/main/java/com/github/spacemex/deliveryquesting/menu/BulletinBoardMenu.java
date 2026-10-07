package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.menu.entry.BulletinBoardTaskEntry;
import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.DeliveryQuestingSavedData;
import com.github.spacemex.deliveryquesting.registry.ModBlocks;
import com.github.spacemex.deliveryquesting.registry.ModMenus;
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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class BulletinBoardMenu extends AbstractContainerMenu {

    private final BlockPos blockPos;

    private final List<BulletinBoardTaskEntry> availableTasks;
    private final List<BulletinBoardTaskEntry> activeTasks;

    private final double groupLevel;

    public BulletinBoardMenu(int containerId, Inventory inventory, BlockPos blockPos, double groupLevel,
                             List<BulletinBoardTaskEntry> availableTasks, List<BulletinBoardTaskEntry> activeTasks) {
        super(ModMenus.BULLETIN_BOARD.get(), containerId);

        this.blockPos = blockPos;

        this.groupLevel = groupLevel;

        this.availableTasks = List.copyOf(availableTasks);

        this.activeTasks = List.copyOf(activeTasks);
    }

    public static BulletinBoardMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos blockPos = buffer.readBlockPos();

        double groupLevel = buffer.readDouble();

        List<BulletinBoardTaskEntry> availableTasks = BulletinBoardTaskEntry.readList(buffer);

        List<BulletinBoardTaskEntry> activeTasks = BulletinBoardTaskEntry.readList(buffer);

        return new BulletinBoardMenu(containerId, inventory, blockPos, groupLevel, availableTasks, activeTasks);
    }

    public static void open(ServerPlayer player, BlockPos pos) {
        DeliveryQuestingSavedData data = DeliveryQuestingSavedData.get(player.level().getServer());

        Optional<DeliveryGroup> optionalGroup = data.getGroupForPlayer(player.getUUID());

        if (optionalGroup.isEmpty()) {
            player.sendSystemMessage
                    (Component.literal("You must be in a delivery group to use the bulletin board."));

            return;
        }

        DeliveryGroup group = optionalGroup.get();

        List<BulletinBoardTaskEntry> availableEntries = createAvailableEntries(group);
        List<BulletinBoardTaskEntry> activeEntries = createActiveEntries(group);

        double groupLevel = group.level();

        SimpleMenuProvider provider = new SimpleMenuProvider((containerId, inventory, menuPlayer) ->
                new BulletinBoardMenu(containerId, inventory, pos, groupLevel, availableEntries, activeEntries),
                Component.translatable("screen.delivery_questing.bulletin_board"));

        MenuRegistry.openExtendedMenu(player, provider, buffer -> {
            buffer.writeBlockPos(pos);

            buffer.writeDouble(groupLevel);

            BulletinBoardTaskEntry.writeList(buffer, availableEntries);

            BulletinBoardTaskEntry.writeList(buffer, activeEntries);
        });
    }

    public double groupLevel() {
        return groupLevel;
    }

    private static List<BulletinBoardTaskEntry> createAvailableEntries(@NonNull DeliveryGroup group) {
        return List.of();
    }

    private static List<BulletinBoardTaskEntry> createActiveEntries(DeliveryGroup group) {
        return group.activeTasks()
                .stream().map(progress -> TaskManager.getTask(progress.taskId())
                        .map(task -> BulletinBoardTaskEntry.fromActive(task, progress)))
                .flatMap(Optional::stream).sorted(Comparator.comparing(BulletinBoardTaskEntry::name)).toList();
    }

    public BlockPos getPos() {
        return blockPos;
    }

    public List<BulletinBoardTaskEntry> availableTasks() {
        return availableTasks;
    }

    public List<BulletinBoardTaskEntry> activeTasks() {
        return activeTasks;
    }

    public boolean hasAvailableTask(Identifier taskId) {
        return availableTasks.stream().anyMatch(task -> task.id().equals(taskId));
    }

    public boolean hasActiveTask(Identifier taskId) {
        return activeTasks.stream().anyMatch(task -> task.id().equals(taskId));
    }

    @Override
    public @NonNull ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!player.level().getBlockState(blockPos).is(ModBlocks.BULLETIN_BOARD.get())) {

            return false;
        }

        double x = blockPos.getX() + 0.5D;
        double y = blockPos.getY() + 0.5D;
        double z = blockPos.getZ() + 0.5D;

        return player.distanceToSqr(x, y, z) <= 64.0D;
    }
}