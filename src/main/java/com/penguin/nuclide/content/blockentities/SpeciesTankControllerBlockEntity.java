package com.penguin.nuclide.content.blockentities;

import java.util.Objects;

import com.mojang.serialization.DataResult;
import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.content.blocks.SpeciesTankCasingBlock;
import com.penguin.nuclide.content.blocks.SpeciesTankControllerBlock;
import com.penguin.nuclide.content.multiblocks.MultiBlockStructure;
import com.penguin.nuclide.content.multiblocks.VerticalTankFinder;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.content.registry.ModBlocks;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.misc.TankHalfX;
import com.penguin.nuclide.misc.TankHalfZ;
import com.penguin.nuclide.misc.TankLayer;
import com.penguin.nuclide.species.SpeciesContainer;
import com.penguin.nuclide.species.SpeciesStack;
import com.penguin.nuclide.transport.SpeciesFilter;
import com.penguin.nuclide.transport.SpeciesTransportNode;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class SpeciesTankControllerBlockEntity extends BlockEntity implements SpeciesTransportNode {

    private static final String CONTAINER_KEY = "container";

    public static final VerticalTankFinder TANK_FINDER =
            new VerticalTankFinder(
                    ModBlocks.SPECIES_TANK_CONTROLLER,
                    ModBlocks.SPECIES_TANK_CASING,
                    1,
                    6
            );

    private SpeciesContainer container = new SpeciesContainer();

    private boolean formed = false;
    private int height = 0;
    private int capacity = 0;
    private MultiBlockStructure structure = null;

    public SpeciesTankControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPECIES_TANK_CONTROLLER, pos, state);
    }

    @Override
    public boolean canInsert(Direction side) {
        return formed;
    }

    @Override
    public boolean canExtract(Direction side) {
        return formed && !container.isEmpty();
    }

    @Override
    public int insertSpecies(Direction side, SpeciesStack stack, boolean simulate) {
        if (!formed || stack == null || stack.isEmpty()) {
            return 0;
        }

        int space = capacity - container.totalAmount();
        if (space <= 0) {
            return 0;
        }

        int inserted = Math.min(stack.count(), space);

        if (!simulate && inserted > 0) {
            container.add(stack.speciesId(), inserted);
            markDirtyAndSync();
        }

        return inserted;
    }

    @Override
    public SpeciesStack extractSpecies(Direction side, SpeciesFilter filter, int maxAmount, boolean simulate) {
        if (!formed || container.isEmpty() || maxAmount <= 0) {
            return null;
        }

        SpeciesFilter actualFilter = filter != null ? filter : SpeciesFilter.any();

        for (SpeciesStack stack : container.stacks()) {
            if (!actualFilter.test(stack)) {
                continue;
            }

            int extracted = Math.min(stack.count(), maxAmount);
            if (extracted <= 0) {
                continue;
            }

            SpeciesStack result = new SpeciesStack(stack.speciesId(), extracted);

            if (!simulate) {
                container.remove(stack.speciesId(), extracted);
                markDirtyAndSync();
            }

            return result;
        }

        return null;
    }


    public SpeciesContainer getContainer() {
        return container;
    }

    public void setContainer(SpeciesContainer container) {
        this.container = Objects.requireNonNull(container, "container");
        clampContainerToCapacity();
        markDirtyAndSync();
    }

    public boolean isFormed() {
        return formed;
    }

    public int getTankHeight() {
        return height;
    }

    public int getCapacity() {
        return capacity;
    }

    public MultiBlockStructure getStructure() {
        return structure;
    }

    public static boolean tryFormFromAnyMember(World world, BlockPos pos) {
        if (world.isClient) return false;

        MultiBlockStructure structure = TANK_FINDER.findFromAnyMember(world, pos);

        if (structure == null) {
            return false;
        }

        applyStructureRoles(world, structure);

        world.getServer().execute(() -> {

            BlockEntity be = world.getBlockEntity(structure.origin());

            if (!(be instanceof SpeciesTankControllerBlockEntity controller)) {
                System.out.println("FAILED: controller BE missing");
                return;
            }

            controller.structure = structure;
            controller.formed = true;
            controller.height = structure.height();
            controller.capacity = controller.calculateCapacity(controller.height);

            controller.linkStructureToCasings(structure);

            controller.clampContainerToCapacity();
            controller.markDirtyAndSync();
        });

        return true;
    }

    public void revalidateStructure() {
        if (world == null || world.isClient) {
            return;
        }

        MultiBlockStructure oldStructure = structure;
        MultiBlockStructure validated = TANK_FINDER.findFromAnyMember(world, pos);

        if (oldStructure != null) {
            unlinkStructureFromCasings(oldStructure);
        }

        structure = validated;

        if (validated == null || !pos.equals(validated.origin())) {
            formed = false;
            height = 0;
            capacity = 0;
            clampContainerToCapacity();
            markDirtyAndSync();
            revertControllerToCasing();
            return;
        }

        applyStructureRoles(world, validated);

        formed = true;
        height = validated.height();
        capacity = calculateCapacity(height);

        linkStructureToCasings(validated);
        clampContainerToCapacity();
        markDirtyAndSync();
    }

    private int calculateCapacity(int height) {
        return height * (4 * Nuclide.MMOL_PER_BLOCK);
    }

    private void clampContainerToCapacity() {
        container.clampToCapacity(capacity);
    }

    private void linkStructureToCasings(MultiBlockStructure structure) {
        if (world == null) return;

        for (BlockPos member : structure.members()) {
            if (member.equals(pos)) continue; // skip controller

            BlockEntity be = world.getBlockEntity(member);

            if (be instanceof SpeciesTankCasingBlockEntity casing) {
                casing.setControllerPos(pos);
            }
        }
    }

    private void unlinkStructureFromCasings(MultiBlockStructure structure) {
        if (world == null || structure == null) return;

        for (BlockPos member : structure.members()) {
            if (member.equals(pos)) continue;

            BlockEntity be = world.getBlockEntity(member);
            if (be instanceof SpeciesTankCasingBlockEntity casing) {
                if (pos.equals(casing.getControllerPos())) {
                    casing.clearControllerPos();
                }
            }
        }
    }

    public static void applyStructureRoles(World world, MultiBlockStructure structure) {
        BlockPos origin = structure.origin();
        int totalHeight = structure.height();

        for (int y = 0; y < totalHeight; y++) {
            for (int dx = 0; dx < 2; dx++) {
                for (int dz = 0; dz < 2; dz++) {
                    BlockPos current = origin.add(dx, y, dz);
                    BlockState oldState = world.getBlockState(current);

                    boolean isController = (dx == 0 && y == 0 && dz == 0);

                    TankHalfX halfX =
                            dx == 0
                                    ? TankHalfX.WEST
                                    : TankHalfX.EAST;

                    TankHalfZ halfZ =
                            dz == 0
                                    ? TankHalfZ.NORTH
                                    : TankHalfZ.SOUTH;

                    TankLayer layer;
                    if (totalHeight == 1) {
                        layer = TankLayer.SINGLE;
                    } else if (y == 0) {
                        layer = TankLayer.BOTTOM;
                    } else if (y == totalHeight - 1) {
                        layer = TankLayer.TOP;
                    } else {
                        layer = TankLayer.MIDDLE;
                    }

                    boolean pulse = false;

                    if (oldState.isOf(ModBlocks.SPECIES_TANK_CONTROLLER)
                            && oldState.contains(SpeciesTankControllerBlock.RENDER_PULSE)) {
                        pulse = oldState.get(SpeciesTankControllerBlock.RENDER_PULSE);
                    } else if (oldState.isOf(ModBlocks.SPECIES_TANK_CASING)
                            && oldState.contains(SpeciesTankCasingBlock.RENDER_PULSE)) {
                        pulse = oldState.get(SpeciesTankCasingBlock.RENDER_PULSE);
                    }

                    BlockState newState;
                    if (isController) {
                        newState = ModBlocks.SPECIES_TANK_CONTROLLER.getDefaultState()
                                .with(SpeciesTankControllerBlock.HALF_X, halfX)
                                .with(SpeciesTankControllerBlock.HALF_Z, halfZ)
                                .with(SpeciesTankControllerBlock.LAYER, layer)
                                .with(SpeciesTankControllerBlock.RENDER_PULSE, pulse);
                    } else {
                        newState = ModBlocks.SPECIES_TANK_CASING.getDefaultState()
                                .with(SpeciesTankCasingBlock.HALF_X, halfX)
                                .with(SpeciesTankCasingBlock.HALF_Z, halfZ)
                                .with(SpeciesTankCasingBlock.LAYER, layer)
                                .with(SpeciesTankCasingBlock.RENDER_PULSE, pulse);
                    }

                    world.setBlockState(current, newState, 3);
                }
            }
        }
    }

    private void revertControllerToCasing() {
        if (world == null || world.isClient) return;
        world.setBlockState(pos, ModBlocks.SPECIES_TANK_CASING.getDefaultState(), 3);
    }

    private void markDirtyAndSync() {
        markDirty();
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().markForUpdate(pos);
        }

        requestStructureRerender();
    }

    public static void tick(World world, BlockPos pos, BlockState state, SpeciesTankControllerBlockEntity be) {
        if (world.isClient) return;
        if (world.getTime() % 20 == 0) {
            be.revalidateStructure();
            // be.requestStructureRerender();
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("capacity", capacity);
        nbt.putInt("height", height);
        nbt.putBoolean("formed", formed);

        DataResult<NbtElement> encoded =
                SpeciesContainer.CODEC.encodeStart(NbtOps.INSTANCE, container);

        encoded.resultOrPartial(err ->
                Nuclide.LOGGER.error("Failed to encode container: {}", err)
        ).ifPresent(el -> nbt.put(CONTAINER_KEY, el));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        capacity = nbt.getInt("capacity");
        height = nbt.getInt("height");
        formed = nbt.getBoolean("formed");
        if (nbt.contains(CONTAINER_KEY)) {
            container = SpeciesContainer.CODEC.parse(NbtOps.INSTANCE, nbt.get(CONTAINER_KEY))
                    .resultOrPartial(err ->
                            Nuclide.LOGGER.error("Failed to decode container: {}", err)
                    ).orElseGet(SpeciesContainer::new);
        } else {
            container = new SpeciesContainer();
        }

        structure = null;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public MutableText getDisplayText() {
        if (!formed) {
            return Text.translatable("nuclide.tank.not_formed");
        }

        if (container.isEmpty()) {
            return Text.translatable("nuclide.tank.empty", capacity);
        }

        MutableText text = Text.translatable("nuclide.tank.header", container.totalAmount(), capacity);

        for (var stack : container.stacks()) {
            SpeciesDefinition def = NuclideDataLoader.SPECIES.getById(stack.speciesId());
            String name = def != null ? def.name() : stack.speciesId();
            text = text.append(Text.translatable("nuclide.tank.entry", name, stack.count()));
        }

        return text;
    }

    public int insertFromContainer(SpeciesContainer input) {
        if (!formed || input.isEmpty()) {
            return 0;
        }

        int insertedTotal = 0;

        for (SpeciesStack stack : input.stacks()) {
            int space = capacity - container.totalAmount();
            if (space <= 0) break;

            int toInsert = Math.min(stack.count(), space);

            container.add(stack.speciesId(), toInsert);
            input.remove(stack.speciesId(), toInsert);

            insertedTotal += toInsert;
        }

        if (insertedTotal > 0) {
            markDirtyAndSync();
        }

        return insertedTotal;
    }

    private void requestStructureRerender() {
        if (world == null || world.isClient) {
            return;
        }

        if (structure == null) {
            toggleRenderPulse(pos);
            return;
        }

        for (BlockPos memberPos : structure.members()) {
            toggleRenderPulse(memberPos);
        }
    }

    private void toggleRenderPulse(BlockPos targetPos) {
        if (world == null) {
            return;
        }

        BlockState state = world.getBlockState(targetPos);

        if (state.isOf(ModBlocks.SPECIES_TANK_CONTROLLER) && state.contains(SpeciesTankControllerBlock.RENDER_PULSE)) {
            world.setBlockState(
                    targetPos,
                    state.with(
                            SpeciesTankControllerBlock.RENDER_PULSE,
                            !state.get(SpeciesTankControllerBlock.RENDER_PULSE)
                    ),
                    3
            );
            return;
        }

        if (state.isOf(ModBlocks.SPECIES_TANK_CASING) && state.contains(SpeciesTankCasingBlock.RENDER_PULSE)) {
            world.setBlockState(
                    targetPos,
                    state.with(
                            SpeciesTankCasingBlock.RENDER_PULSE,
                            !state.get(SpeciesTankCasingBlock.RENDER_PULSE)
                    ),
                    3
            );
        }
    }
}