package com.github.spacemex.deliveryquesting.menu;

import com.github.spacemex.deliveryquesting.menu.entry.BulletinBoardTaskEntry;
import com.github.spacemex.deliveryquesting.registry.ModMenus;
import com.github.spacemex.deliveryquesting.task.manager.TaskManager;
import dev.architectury.registry.menu.MenuRegistry;
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

public final class ContractMenu extends AbstractContainerMenu {

    private final BulletinBoardTaskEntry task;

    public ContractMenu(int containerId, Inventory inventory, BulletinBoardTaskEntry task) {
        super(ModMenus.CONTRACT.get(), containerId);

        this.task = task;
    }

    public static ContractMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        BulletinBoardTaskEntry task = BulletinBoardTaskEntry.read(buffer);

        return new ContractMenu(containerId, inventory, task);
    }

    public static void open(ServerPlayer player, Identifier taskId) {
        TaskManager.getTask(taskId).ifPresentOrElse(task -> {
            BulletinBoardTaskEntry entry = BulletinBoardTaskEntry.fromAvailable(task);

            SimpleMenuProvider provider = new SimpleMenuProvider(
                    (containerId, inventory, menuPlayer) ->
                            new ContractMenu(containerId, inventory, entry), Component.translatable("screen.delivery_questing.contract"));

            MenuRegistry.openExtendedMenu(player, provider, entry::write);

        }, () -> player.sendSystemMessage(Component.literal("That contract no longer exists.")));
    }

    public BulletinBoardTaskEntry task() {
        return task;
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return true;
    }
}