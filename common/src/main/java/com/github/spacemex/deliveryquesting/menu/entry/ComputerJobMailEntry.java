package com.github.spacemex.deliveryquesting.menu.entry;

import com.github.spacemex.deliveryquesting.job.JobDefinition;
import com.github.spacemex.deliveryquesting.progression.DeliveryGroup;
import com.github.spacemex.deliveryquesting.progression.GroupEmail;
import com.github.spacemex.deliveryquesting.progression.JobRuntimeManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ComputerJobMailEntry(UUID emailId, boolean canAccept, BulletinBoardTaskEntry job) {

    private static final int MAX_JOBS = 1024;

    public static ComputerJobMailEntry from(GroupEmail email, DeliveryGroup group, JobDefinition job) {
        return new ComputerJobMailEntry(email.id(), JobRuntimeManager.getAcceptanceFailure(group, job).isEmpty(),
                BulletinBoardTaskEntry.fromJob(job));
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeLong(emailId.getMostSignificantBits());

        buffer.writeLong(emailId.getLeastSignificantBits());

        buffer.writeBoolean(canAccept);

        job.write(buffer);
    }

    public static ComputerJobMailEntry read(FriendlyByteBuf buffer) {
        UUID emailId = new UUID(buffer.readLong(), buffer.readLong());

        boolean canAccept = buffer.readBoolean();

        BulletinBoardTaskEntry job = BulletinBoardTaskEntry.read(buffer);

        return new ComputerJobMailEntry(emailId, canAccept, job);
    }

    public static void writeList(FriendlyByteBuf buffer, List<ComputerJobMailEntry> jobs) {
        if (jobs.size() > MAX_JOBS) {
            throw new IllegalArgumentException("Too many Computer job emails: " + jobs.size());
        }

        buffer.writeVarInt(jobs.size());

        for (ComputerJobMailEntry job : jobs) {
            job.write(buffer);
        }
    }

    public static List<ComputerJobMailEntry> readList(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();

        if (size < 0 || size > MAX_JOBS) {
            throw new IllegalStateException("Invalid Computer job email count: " + size);
        }

        List<ComputerJobMailEntry> result = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            result.add(read(buffer));
        }

        return List.copyOf(result);
    }
}