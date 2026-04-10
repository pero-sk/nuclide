package com.penguin.nuclide.content.blockentities.electrolyser;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.penguin.nuclide.content.blockentities.base.AbstractReactionMachineBlockEntity;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.energy.EnergyNode;
import com.penguin.nuclide.energy.SimpleEnergyStorage;
import com.penguin.nuclide.misc.IHaveHoverInformation;
import com.penguin.nuclide.reaction.ReactionDataLoader;
import com.penguin.nuclide.reaction.ReactionDefinition;
import com.penguin.nuclide.species.SpeciesContainer;
import com.penguin.nuclide.species.SpeciesKey;
import com.penguin.nuclide.species.SpeciesStack;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;

public class ElectrolyserBlockEntity extends AbstractReactionMachineBlockEntity implements EnergyNode, IHaveHoverInformation {

    // the waste product from Electrolyser are hydroxyl radicals

    private final SimpleEnergyStorage energy = new SimpleEnergyStorage(200);

    private static final String HYDROGEN_ID = "molecules:hydrogen_gas";
    private static final String OXYGEN_ID = "atoms:oxygen";
    private static final String HYDROXYL_ID = "molecules:hydroxyl";

    private static final double HYDROXYL_WASTE_FACTOR = 0.25D;
    
    private double hydroxylWasteRemainder = 0.0;

    public ElectrolyserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTROLYSER, pos, state);
    }

    @Override
    public int getBasePenaltyP() {
        return 5;
    }

    private int getWastePenaltyDivisor() {
        return 200; // mmols
    }

    private int getWastePenaltyMax() {
        return 5; // P
    }

    private int getWastePenaltyP() {
        int totalHydroxyl = countSpecies(input, HYDROXYL_ID) + countSpecies(output, HYDROXYL_ID);
        return Math.min(getWastePenaltyMax(), totalHydroxyl / getWastePenaltyDivisor());
    }

    private int countSpecies(SpeciesContainer container, String speciesId) {
        int total = 0;

        for (SpeciesKey key : container.asMap().keySet()) {
            if (speciesId.equals(key.speciesId())) {
                total += container.countOf(key);
            }
        }

        return total;
    }

    @Override
    protected List<InefficiencyEntry> getAdditionalInefficiencyEntries() {
        List<InefficiencyEntry> entries = new ArrayList<>();

        int wastePenalty = getWastePenaltyP();
        if (wastePenalty > 0) {
            entries.add(new InefficiencyEntry("Waste penalty", wastePenalty));
        }

        return entries;
    }

    public static void tick(World world, BlockPos pos, BlockState state, ElectrolyserBlockEntity be) {
        if (world.isClient) return;
        be.tickServer();
    }

    @Override
    protected Iterable<ReactionDefinition> getCandidateReactions() {
        return ReactionDataLoader.REACTIONS.values();
    }
    
    @Override
    protected boolean supportsReaction(ReactionDefinition reaction) {
        return reaction.id().equals("nuclide:electrolysis");
    }

    @Override
    public boolean canInsertEnergy(Direction side) {
        if (energy.getStored() < energy.getCapacity()) {return true;}

        return false;
    }

    @Override
    public boolean canExtractEnergy(Direction side) {
        if (energy.getStored() > 0) {return true;}

        return false;
    }

    @Override
    public int insertEnergy(Direction side, int amount, boolean simulate) {
        if (canInsertEnergy(side)) {
            return energy.insert(amount, simulate);
        } else {
            return 0;
        }
    }

    @Override
    public int extractEnergy(Direction side, int amount, boolean simulate) {
        if (canExtractEnergy(side)) {
            return energy.extract(amount, simulate);
        } else {
            return 0;
        }
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
    protected int getStepsPerTick() {
        return 25;
    }

    @Override
    protected void tickServer() {
        if (activeReaction == null || !canContinueReaction(activeReaction)) {
            activeReaction = findReaction();
            progress = 0;
        }

        if (activeReaction == null) {
            return;
        }

        final int energyCostPerTick = 20;

        if (energy.getStored() < energyCostPerTick) {
            return;
        }

        energy.extract(energyCostPerTick, false);
        super.tickServer();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("Energy", energy.getStored());
        nbt.putDouble("HydroxylWasteRemainder", hydroxylWasteRemainder);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        energy.setStored(nbt.getInt("Energy"));

        if (nbt.contains("HydroxylWasteRemainder", NbtElement.DOUBLE_TYPE)) {
            hydroxylWasteRemainder = nbt.getDouble("HydroxylWasteRemainder");
        } else {
            hydroxylWasteRemainder = 0.0;
        }
    }


    @Override
    public boolean addHoverInformation(
            World world,
            BlockHitResult hit,
            PlayerEntity player,
            List<Text> tooltip
    ) {
        tooltip.add(Text.literal("Electrolyser"));
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

    @Override
    protected void applyInefficiencyToProducedBatch(SpeciesContainer producedBatch, @Nullable ReactionDefinition reaction) {
        if (reaction == null || !supportsReaction(reaction)) {
            super.applyInefficiencyToProducedBatch(producedBatch, reaction);
            return;
        }

        double efficiency = getEfficiencyFraction();

        int hydrogenAmount = 0;
        int oxygenAmount = 0;

        int lostHydrogen = 0;
        int lostOxygen = 0;

        for (SpeciesStack stack : producedBatch.stacks()) {
            String speciesId = stack.speciesId();
            int amount = stack.count();

            if (HYDROGEN_ID.equals(speciesId)) {
                hydrogenAmount += amount;
            } else if (OXYGEN_ID.equals(speciesId)) {
                oxygenAmount += amount;
            }

            if (amount <= 0) {
                continue;
            }

            double remainder = inefficiencyRemainders.getOrDefault(speciesId, 0.0);
            double exactKept = (amount * efficiency) + remainder;

            int kept = (int) Math.floor(exactKept);
            double newRemainder = exactKept - kept;

            inefficiencyRemainders.put(speciesId, newRemainder);

            int lost = amount - kept;

            if (HYDROGEN_ID.equals(speciesId)) {
                lostHydrogen += lost;
            } else if (OXYGEN_ID.equals(speciesId)) {
                lostOxygen += lost;
            } else {
                if (kept > 0) {
                    output.add(new SpeciesStack(speciesId, kept));
                }

                if (lost > 0) {
                    handleLostOutput(speciesId, lost, reaction);
                }
            }
        }

        double exactHydroxylWaste = (Math.min(lostHydrogen, lostOxygen) * HYDROXYL_WASTE_FACTOR) + hydroxylWasteRemainder;
        int hydroxylWaste = (int) Math.floor(exactHydroxylWaste);
        hydroxylWasteRemainder = exactHydroxylWaste - hydroxylWaste;

        int hydrogenLostToOrdinaryWaste = lostHydrogen - hydroxylWaste;
        int oxygenLostToOrdinaryWaste = lostOxygen - hydroxylWaste;

        int hydrogenKept = hydrogenAmount - lostHydrogen;
        int oxygenKept = oxygenAmount - lostOxygen;

        if (hydrogenKept > 0) {
            output.add(new SpeciesStack(HYDROGEN_ID, hydrogenKept));
        }

        if (oxygenKept > 0) {
            output.add(new SpeciesStack(OXYGEN_ID, oxygenKept));
        }

        if (hydroxylWaste > 0) {
            output.add(new SpeciesStack(HYDROXYL_ID, hydroxylWaste));
        }

        if (hydrogenLostToOrdinaryWaste > 0) {
            handleLostOutput(HYDROGEN_ID, hydrogenLostToOrdinaryWaste, reaction);
        }

        if (oxygenLostToOrdinaryWaste > 0) {
            handleLostOutput(OXYGEN_ID, oxygenLostToOrdinaryWaste, reaction);
        }

        System.out.println(
            getClass().getSimpleName()
            + " hydrogenAmount=" + hydrogenAmount
            + " oxygenAmount=" + oxygenAmount
            + " lostHydrogen=" + lostHydrogen
            + " lostOxygen=" + lostOxygen
            + " hydroxylWaste=" + hydroxylWaste
            + " hydroxylWasteRemainder=" + hydroxylWasteRemainder
            + " hydrogenKept=" + hydrogenKept
            + " oxygenKept=" + oxygenKept
            + " heat=" + internalHeat
        );
    }
}