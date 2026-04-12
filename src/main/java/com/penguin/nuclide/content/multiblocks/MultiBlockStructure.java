package com.penguin.nuclide.content.multiblocks;

import java.util.List;
import java.util.Objects;

import net.minecraft.util.math.BlockPos;

public final class MultiBlockStructure {
    private final BlockPos origin;
    private final int width;
    private final int height;
    private final int depth;
    private final List<BlockPos> members;

    public MultiBlockStructure(
            BlockPos origin,
            int width,
            int height,
            int depth,
            List<BlockPos> members
    ) {
        this.origin = Objects.requireNonNull(origin, "origin").toImmutable();
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.members = List.copyOf(Objects.requireNonNull(members, "members"));

        if (width <= 0) {
            throw new IllegalArgumentException("width must be > 0");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be > 0");
        }
        if (depth <= 0) {
            throw new IllegalArgumentException("depth must be > 0");
        }
        if (members.isEmpty()) {
            throw new IllegalArgumentException("members cannot be empty");
        }
    }

    public BlockPos origin() {
        return origin;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int depth() {
        return depth;
    }

    public List<BlockPos> members() {
        return members;
    }

    public boolean contains(BlockPos pos) {
        return members.contains(pos);
    }

    public BlockPos maxPos() {
        return origin.add(width - 1, height - 1, depth - 1);
    }

    public int volume() {
        return width * height * depth;
    }

    @Override
    public String toString() {
        return "MultiBlockStructure{" +
                "origin=" + origin +
                ", width=" + width +
                ", height=" + height +
                ", depth=" + depth +
                ", members=" + members.size() +
                '}';
    }
}