package com.penguin.nuclide.content.blocks.gas;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import com.penguin.nuclide.gas.GasType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class GasBlock extends Block {
    public static final IntProperty LEVEL = IntProperty.of("level", 0, 15);

    private final GasType gasType;

    public GasBlock(Settings settings, GasType gasType) {
        super(settings
                .noCollision()
                .replaceable()
                .strength(0.1f)
                .ticksRandomly());

        if (gasType == null) {
            throw new IllegalArgumentException("gasType cannot be null");
        }

        this.gasType = gasType;
        this.setDefaultState(this.stateManager.getDefaultState().with(LEVEL, 15));
    }

    public GasType getGasType() {
        return gasType;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return false;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        spread(state, world, pos, random);

        if (!gasType.isFlammable()) {
            return;
        }

        for (Direction dir : Direction.values()) {
            if (world.getBlockState(pos.offset(dir)).isOf(Blocks.FIRE)) {
                chainExplode(world, pos);
                return;
            }
        }
    }

    protected void spread(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int concentration = state.get(LEVEL);

        if (concentration <= 1) {
            world.removeBlock(pos, false);
            return;
        }

        float density = gasType.getDensity();
        float dissipation = gasType.getDissipationRate();

        Direction vertical =
                density > 1.0f ? Direction.DOWN :
                density < 1.0f ? Direction.UP :
                null;

        int attempts = Math.max(1, (int) (4 * dissipation));
        int remaining = concentration;

        if (vertical != null && random.nextFloat() < 0.8f) {
            remaining = equalize(world, pos, vertical, remaining);
        }

        for (int i = 0; i < attempts; i++) {
            Direction dir = Direction.Type.HORIZONTAL.random(random);
            remaining = equalize(world, pos, dir, remaining);
        }

        if (remaining <= 0) {
            world.removeBlock(pos, false);
        } else {
            world.setBlockState(pos, state.with(LEVEL, MathHelper.clamp(remaining, 0, 15)), Block.NOTIFY_ALL);
            scheduleNextTick(world, pos);
        }
    }

    private int equalize(ServerWorld world, BlockPos pos, Direction dir, int available) {
        if (available <= 1) {
            return available;
        }

        BlockPos targetPos = pos.offset(dir);
        BlockState targetState = world.getBlockState(targetPos);

        if (!targetState.isAir() && !(targetState.getBlock() instanceof GasBlock)) {
            return available;
        }

        int sourceLevel = available;
        int targetLevel = 0;

        if (targetState.getBlock() instanceof GasBlock gas &&
                gas.getGasType() == this.gasType) {
            targetLevel = targetState.get(LEVEL);
        }

        int total = sourceLevel + targetLevel;
        int newSource = total / 2;
        int newTarget = total - newSource;

        if (newTarget > 0) {
            world.setBlockState(
                    targetPos,
                    this.getDefaultState().with(LEVEL, MathHelper.clamp(newTarget, 0, 15)),
                    Block.NOTIFY_ALL
            );
            scheduleNextTick(world, targetPos);
        }

        return newSource;
    }

    public void chainExplode(World world, BlockPos origin) {
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> stack = new ArrayDeque<>();
        stack.push(origin);

        while (!stack.isEmpty()) {
            BlockPos pos = stack.pop();
            if (!visited.add(pos)) {
                continue;
            }

            BlockState state = world.getBlockState(pos);
            if (!(state.getBlock() instanceof GasBlock gas)) {
                continue;
            }
            if (gas.getGasType().getFlammability() <= 0f) {
                continue;
            }

            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.offset(dir);
                if (visited.contains(neighborPos)) {
                    continue;
                }

                BlockState neighborState = world.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof GasBlock neighborGas &&
                        neighborGas.getGasType().getFlammability() > 0f) {
                    stack.push(neighborPos);
                }
            }
        }

        float totalStrength = 0f;
        for (BlockPos pos : visited) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof GasBlock gas) {
                totalStrength += (state.get(LEVEL) / 15f) * gas.getGasType().getFlammability();
            }
        }

        totalStrength *= 5f;

        if (totalStrength > 0f) {
            world.createExplosion(
                    null,
                    origin.getX() + 0.5,
                    origin.getY() + 0.5,
                    origin.getZ() + 0.5,
                    totalStrength,
                    World.ExplosionSourceType.BLOCK
            );
        }

        for (BlockPos pos : visited) {
            world.removeBlock(pos, false);
        }
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);

        if (!world.isClient) {
            scheduleNextTick((ServerWorld) world, pos);
        }
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);

        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        living.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 20, 1, false, false));

        if (!gasType.isToxic()) {
            return;
        }

        int strength = state.get(LEVEL) / 4;
        if (strength <= 0) {
            return;
        }

        float damage = strength * gasType.getToxicity();
        living.damage(world.getDamageSources().drown(), damage);
    }

    private void scheduleNextTick(ServerWorld world, BlockPos pos) {
        int delay = Math.max(1, (int) (10f / Math.max(0.001f, gasType.getDissipationRate())));
        world.scheduleBlockTick(pos, this, delay);
    }
}