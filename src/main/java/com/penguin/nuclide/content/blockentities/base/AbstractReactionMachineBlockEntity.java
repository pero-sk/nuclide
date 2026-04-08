package com.penguin.nuclide.content.blockentities.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.penguin.nuclide.inefficiency.InefficiencyProvider;
import com.penguin.nuclide.reaction.ReactionContext;
import com.penguin.nuclide.reaction.ReactionDataLoader;
import com.penguin.nuclide.reaction.ReactionDefinition;
import com.penguin.nuclide.reaction.ReactionExecutor;
import com.penguin.nuclide.reaction.ReactionParticipant;
import com.penguin.nuclide.reaction.ReactionSearcher;
import com.penguin.nuclide.species.SpeciesContainer;
import com.penguin.nuclide.species.SpeciesStack;
import com.penguin.nuclide.transport.SpeciesFilter;
import com.penguin.nuclide.transport.SpeciesTransportNode;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class AbstractReactionMachineBlockEntity extends BlockEntity implements SpeciesTransportNode, InefficiencyProvider {

    protected final SpeciesContainer input = new SpeciesContainer();
    protected final SpeciesContainer output = new SpeciesContainer();
    protected final Map<String, Double> inefficiencyRemainders = new HashMap<>();

    protected @Nullable ReactionDefinition activeReaction = null;
    protected int progress = 0;
    protected int completedOperations = 0;

    protected AbstractReactionMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected int getStepsPerTick() {
        return 50;
    }

    protected abstract Iterable<ReactionDefinition> getCandidateReactions();

    protected ReactionContext getReactionContext() {
        return ReactionContext.DEFAULT;
    }

    protected boolean supportsReaction(ReactionDefinition reaction) {
        return true;
    }

    protected void onReactionCompleted(ReactionDefinition reaction) {
    }

    protected int getAgePenaltyDivisor() {
        return 2000;
    }

    protected int getMaxAgePenaltyP() {
        return 10;
    }

    protected int getPollutionPenaltyP() {
        return 0;
    }

    protected List<InefficiencyEntry> getAdditionalInefficiencyEntries() {
        return List.of();
    }

    protected int getAgePenaltyP() {
        int divisor = getAgePenaltyDivisor();
        if (divisor <= 0) {
            return 0;
        }

        return Math.min(getMaxAgePenaltyP(), completedOperations / divisor);
    }

    protected int applyInefficiencyToCost(int amount) {
        if (amount <= 0) {
            return 0;
        }

        return (int) Math.ceil(amount * (1.0 + getInefficiencyFraction()));
    }

    protected void handleLostOutput(String speciesId, int lostAmount, ReactionDefinition reaction) {
    }

    @Override
    public List<InefficiencyEntry> getInefficiencyEntries() {
        List<InefficiencyEntry> entries = new ArrayList<>();

        int agePenalty = getAgePenaltyP();
        if (agePenalty > 0) {
            entries.add(new InefficiencyEntry("Age penalty", agePenalty));
        }

        int pollutionPenalty = getPollutionPenaltyP();
        if (pollutionPenalty > 0) {
            entries.add(new InefficiencyEntry("Pollution penalty", pollutionPenalty));
        }

        entries.addAll(getAdditionalInefficiencyEntries());
        return entries;
    }

    @Override
    public List<SpeciesStack> getAvailableSpecies(Direction side) {
        return output.stacks();
    }

    protected void tickServer() {
        if (activeReaction == null || !canContinueReaction(activeReaction)) {
            activeReaction = findReaction();
            progress = 0;
        }

        if (activeReaction == null) {
            return;
        }

        SpeciesContainer producedThisTick = new SpeciesContainer();
        boolean changed = false;

        for (int i = 0; i < getStepsPerTick(); i++) {
            if (activeReaction == null || !canContinueReaction(activeReaction)) {
                activeReaction = findReaction();
                progress = 0;
                break;
            }

            progress++;

            int duration = activeReaction.durationTicks();

            if (progress >= duration) {
                SpeciesContainer produced = executeReactionExact(activeReaction);

                for (SpeciesStack stack : produced.stacks()) {
                    producedThisTick.add(stack);
                }

                progress = 0;
                activeReaction = findReaction();
                changed = true;

                if (activeReaction == null) {
                    break;
                }
            }
        }

        if (!producedThisTick.isEmpty()) {
            applyInefficiencyToProducedBatch(producedThisTick, activeReaction);
            changed = true;
        }

        if (changed) {
            markDirty();
        }
    }

    protected @Nullable ReactionDefinition findReaction() {
        List<ReactionDefinition> matches = ReactionSearcher.findMatches(
                getCandidateReactions(),
                input,
                getReactionContext()
        );

        for (ReactionDefinition reaction : matches) {
            if (supportsReaction(reaction)) {
                return reaction;
            }
        }

        return null;
    }

    protected boolean canContinueReaction(ReactionDefinition reaction) {
        if (reaction == null) {
            return false;
        }

        List<ReactionDefinition> matches = ReactionSearcher.findMatches(
                getCandidateReactions(),
                input,
                getReactionContext()
        );

        for (ReactionDefinition match : matches) {
            if (match.id().equals(reaction.id()) && supportsReaction(match)) {
                return true;
            }
        }

        return false;
    }

    protected SpeciesContainer executeReactionExact(ReactionDefinition reaction) {
        SpeciesContainer before = input.copy();
        SpeciesContainer after = ReactionExecutor.execute(
                reaction,
                input,
                getReactionContext()
        );

        SpeciesContainer produced = new SpeciesContainer();

        input.clear();

        for (SpeciesStack afterStack : after.stacks()) {
            String speciesId = afterStack.speciesId();
            int afterCount = afterStack.count();
            int beforeCount = before.countOf(speciesId, afterStack.state());

            if (isReactionOutputSpecies(reaction, speciesId)) {
                int declaredProducedAmount = getDeclaredOutputAmount(reaction, speciesId);
                int actualIncrease = Math.max(0, afterCount - beforeCount);
                int producedAmount = Math.min(declaredProducedAmount, actualIncrease);
                int leftoverAmount = afterCount - producedAmount;

                if (leftoverAmount > 0) {
                    input.add(new SpeciesStack(speciesId, leftoverAmount));
                }

                if (producedAmount > 0) {
                    produced.add(new SpeciesStack(speciesId, producedAmount));
                }
            } else {
                input.add(afterStack);
            }
        }

        completedOperations++;
        onReactionCompleted(reaction);
        return produced;
    }

    protected void applyInefficiencyToProducedBatch(SpeciesContainer producedBatch, @Nullable ReactionDefinition reaction) {
        double efficiency = getEfficiencyFraction();

        for (SpeciesStack stack : producedBatch.stacks()) {
            String speciesId = stack.speciesId();
            int amount = stack.count();

            if (amount <= 0) {
                continue;
            }

            double remainder = inefficiencyRemainders.getOrDefault(speciesId, 0.0);
            double exactKept = (amount * efficiency) + remainder;

            int kept = (int) Math.floor(exactKept);
            double newRemainder = exactKept - kept;

            inefficiencyRemainders.put(speciesId, newRemainder);

            if (kept > 0) {
                output.add(new SpeciesStack(speciesId, kept));
            }

            int lost = amount - kept;
            if (lost > 0) {
                handleLostOutput(speciesId, lost, reaction);
            }
        }
    }

    protected boolean isReactionOutputSpecies(ReactionDefinition reaction, String speciesId) {
        for (ReactionParticipant participant : reaction.outputs()) {
            if (participant.isSpecies() && participant.speciesId().equals(speciesId)) {
                return true;
            }
        }

        return false;
    }

    protected int getDeclaredOutputAmount(ReactionDefinition reaction, String speciesId) {
        int total = 0;

        for (ReactionParticipant participant : reaction.outputs()) {
            if (participant.isSpecies() && participant.speciesId().equals(speciesId)) {
                total += participant.count();
            }
        }

        return total;
    }

    public SpeciesContainer getInputContainer() {
        return input;
    }

    public SpeciesContainer getOutputContainer() {
        return output;
    }

    public @Nullable ReactionDefinition getActiveReaction() {
        return activeReaction;
    }

    public int getProgress() {
        return progress;
    }

    public int getCompletedOperations() {
        return completedOperations;
    }

    @Override
    public boolean canInsert(Direction side) {
        return true;
    }

    @Override
    public boolean canExtract(Direction side) {
        return !output.isEmpty();
    }

    @Override
    public int insertSpecies(Direction side, SpeciesStack stack, boolean simulate) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        int inserted = stack.count();

        if (!simulate) {
            input.add(stack);
            markDirty();
        }

        return inserted;
    }

    @Override
    public SpeciesStack extractSpecies(Direction side, SpeciesFilter filter, int maxAmount, boolean simulate) {
        if (output.isEmpty() || maxAmount <= 0) {
            return null;
        }

        SpeciesFilter actual = filter != null ? filter : SpeciesFilter.any();

        for (SpeciesStack stack : output.stacks()) {
            if (!actual.test(stack)) {
                continue;
            }

            int extracted = Math.min(stack.count(), maxAmount);
            SpeciesStack result = new SpeciesStack(stack.speciesId(), extracted);

            if (!simulate) {
                output.remove(stack.key(), extracted);
                markDirty();
            }

            return result;
        }

        return null;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        nbt.put("Input", input.toNbtList());
        nbt.put("Output", output.toNbtList());
        nbt.putInt("Progress", progress);
        nbt.putInt("CompletedOperations", completedOperations);

        NbtList remainderList = new NbtList();
        for (Map.Entry<String, Double> entry : inefficiencyRemainders.entrySet()) {
            NbtCompound tag = new NbtCompound();
            tag.putString("SpeciesId", entry.getKey());
            tag.putDouble("Remainder", entry.getValue());
            remainderList.add(tag);
        }
        nbt.put("InefficiencyRemainders", remainderList);

        if (activeReaction != null) {
            nbt.putString("ActiveReactionId", activeReaction.id());
        } else {
            nbt.remove("ActiveReactionId");
        }
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if (nbt.contains("Input", NbtElement.LIST_TYPE)) {
            input.fromNbtList(nbt.getList("Input", NbtElement.COMPOUND_TYPE));
        } else {
            input.clear();
        }

        if (nbt.contains("Output", NbtElement.LIST_TYPE)) {
            output.fromNbtList(nbt.getList("Output", NbtElement.COMPOUND_TYPE));
        } else {
            output.clear();
        }

        progress = nbt.getInt("Progress");
        completedOperations = nbt.getInt("CompletedOperations");

        inefficiencyRemainders.clear();
        if (nbt.contains("InefficiencyRemainders", NbtElement.LIST_TYPE)) {
            NbtList remainderList = nbt.getList("InefficiencyRemainders", NbtElement.COMPOUND_TYPE);

            for (int i = 0; i < remainderList.size(); i++) {
                NbtCompound tag = remainderList.getCompound(i);
                String speciesId = tag.getString("SpeciesId");
                double remainder = tag.getDouble("Remainder");

                if (!speciesId.isBlank() && remainder > 0.0) {
                    inefficiencyRemainders.put(speciesId, remainder);
                }
            }
        }

        if (nbt.contains("ActiveReactionId", NbtElement.STRING_TYPE)) {
            String reactionId = nbt.getString("ActiveReactionId");
            activeReaction = ReactionDataLoader.REACTIONS.getById(reactionId);
        } else {
            activeReaction = null;
        }
    }
}