package com.penguin.nuclide.content.blockentities.hydrogen_furnace;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.penguin.nuclide.content.blockentities.base.AbstractReactionMachineBlockEntity;
import com.penguin.nuclide.content.blockentities.base.EnergyPusher;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.energy.SimpleEnergyStorage;
import com.penguin.nuclide.misc.IHaveHoverInformation;
import com.penguin.nuclide.pollution.PollutionManager;
import com.penguin.nuclide.reaction.ReactionDataLoader;
import com.penguin.nuclide.reaction.ReactionDefinition;
import com.penguin.nuclide.species.SpeciesStack;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class HydrogenFurnaceBlockEntity extends AbstractReactionMachineBlockEntity implements EnergyPusher, IHaveHoverInformation {

    private static final int ENERGY_CAPACITY = 600;
    private static final int MAX_TRANSFER_PER_TICK = ENERGY_CAPACITY / 10;
    private static final int BURN_ENERGY_AMOUNT = 200;

    private final SimpleEnergyStorage energy = new SimpleEnergyStorage(ENERGY_CAPACITY);
    private final Set<Direction> recentlyReceivedEnergySides = EnumSet.noneOf(Direction.class);

    public HydrogenFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HYDROGEN_FURNACE, pos, state);
    }

    @Override
    public int getBasePenaltyP() {
        return 8;
    }

    @Override
    public boolean canInsertEnergy(Direction side) {
        return false;
    }

    @Override
    public boolean canExtractEnergy(Direction side) {
        return energy.getStored() > 0;
    }

    @Override
    public int insertSpecies(Direction side, SpeciesStack stack, boolean simulate) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        String id = stack.speciesId();
        if (!"molecules:hydrogen_gas".equals(id) && !"molecules:oxygen_gas".equals(id)) {
            return 0;
        }

        return super.insertSpecies(side, stack, simulate);
    }

    @Override
    public int insertEnergy(Direction side, int amount, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(Direction side, int amount, boolean simulate) {
        return canExtractEnergy(side) ? energy.extract(amount, simulate) : 0;
    }

    @Override
    public int getStoredEnergy() {
        return energy.getStored();
    }

    @Override
    public int getEnergyCapacity() {
        return energy.getCapacity();
    }

    @Override
    protected void handleLostOutput(String speciesId, int lostAmount, @Nullable ReactionDefinition reaction) {
        if (lostAmount <= 0 || world == null) {
            return;
        }

        int heatAdded = lostAmount * 4;
        int pollutionAdded = lostAmount;

        addHeat(heatAdded);
        PollutionManager.addPollution(world, pos, pollutionAdded);
    }

    @Override
    protected Iterable<ReactionDefinition> getCandidateReactions() {
        return ReactionDataLoader.REACTIONS.values();
    }

    @Override
    protected void onReactionCompleted(ReactionDefinition reaction) {
        if (!"nuclide:hydrogen_burn".equals(reaction.id())) {
            return;
        }

        int inserted = energy.insert(BURN_ENERGY_AMOUNT, false);
        if (inserted > 0) {
            sync();

            if (world != null) {
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
            }
        }
    }

    @Override
    protected boolean supportsReaction(ReactionDefinition reaction) {
        return "nuclide:hydrogen_burn".equals(reaction.id());
    }

    public static void tick(World world, BlockPos pos, BlockState state, HydrogenFurnaceBlockEntity furnace) {
        if (world.isClient) {
            return;
        }

        furnace.tickServer();
        furnace.pushEnergyToNeighbors();
    }

    @Override
    public int getMaxTransferPerTick() {
        return MAX_TRANSFER_PER_TICK;
    }

    @Override
    public Set<Direction> getRecentlyReceivedEnergySides() {
        return recentlyReceivedEnergySides;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("Energy", energy.getStored());
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        energy.setStored(nbt.getInt("Energy"));
    }

    @Override
    public boolean addHoverInformation(
            World world,
            BlockHitResult hit,
            PlayerEntity player,
            List<Text> tooltip
    ) {
        tooltip.add(Text.literal("Hydrogen Furnace"));
        tooltip.add(Text.literal("Energy: " + getStoredEnergy() + " / " + getEnergyCapacity()));

        if (getActiveReaction() != null) {
            tooltip.add(Text.literal("Reaction: " + getActiveReaction().name()));
            tooltip.add(Text.literal("Progress: " + getProgress() + " / " + getActiveReaction().durationTicks()));
        } else {
            tooltip.add(Text.literal("Reaction: idle"));
        }

        if (getInputContainer().isEmpty()) {
            tooltip.add(Text.literal("Input: empty"));
        } else {
            tooltip.add(Text.literal("Input:"));
            for (SpeciesStack stack : getInputContainer().stacks()) {
                SpeciesDefinition definition = NuclideDataLoader.SPECIES.getById(stack.speciesId());
                String name = definition != null ? definition.name() : stack.speciesId();
                tooltip.add(Text.literal("- " + name + ": " + stack.count() + " mmol"));
            }
        }

        if (getOutputContainer().isEmpty()) {
            tooltip.add(Text.literal("Output: empty"));
        } else {
            tooltip.add(Text.literal("Output:"));
            for (SpeciesStack stack : getOutputContainer().stacks()) {
                SpeciesDefinition definition = NuclideDataLoader.SPECIES.getById(stack.speciesId());
                String name = definition != null ? definition.name() : stack.speciesId();
                tooltip.add(Text.literal("- " + name + ": " + stack.count() + " mmol"));
            }
        }

        for (String line : getInefficiencyBreakdownLines()) {
            tooltip.add(Text.literal(line));
        }

        return true;
    }

    private int getRatioPenaltyP() {
        SpeciesDefinition hh_def = NuclideDataLoader.SPECIES.getById("molecules:hydrogen_gas");
        SpeciesDefinition oo_def = NuclideDataLoader.SPECIES.getById("molecules:oxygen_gas");
        int hh = getInputContainer().countOf("molecules:hydrogen_gas", hh_def.state());
        int oo = getInputContainer().countOf("molecules:oxygen_gas", oo_def.state());

        if (hh <= 0 || oo <= 0) {
            return 0;
        }

        int possiblePairsFromH = hh / 2;
        int possiblePairsFromO = oo;
        int paired = Math.min(possiblePairsFromH, possiblePairsFromO);

        int usedH = paired * 2;
        int usedO = paired;

        int excessH = hh - usedH;
        int excessO = oo - usedO;

        int totalRelevant = hh + oo;
        int totalExcess = excessH + excessO;

        if (totalRelevant <= 0) {
            return 0;
        }

        double imbalanceFraction = (double) totalExcess / (double) totalRelevant;

        if (imbalanceFraction <= 0.10) {
            return 0;
        }
        if (imbalanceFraction <= 0.25) {
            return 1;
        }
        if (imbalanceFraction <= 0.45) {
            return 2;
        }
        return 3;
    }

    @Override
    protected List<InefficiencyEntry> getAdditionalInefficiencyEntries() {
        List<InefficiencyEntry> entries = new ArrayList<>();

        int ratioPenalty = getRatioPenaltyP();
        if (ratioPenalty > 0) {
            entries.add(new InefficiencyEntry("Ratio penalty", ratioPenalty));
        }

        return entries;
    }
}