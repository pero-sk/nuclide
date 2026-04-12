package com.penguin.nuclide.content.items;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.nbt.NuclideDataComponents;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class SpeciesFilterItem extends Item {

    public SpeciesFilterItem(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        String selectedSpecies = getSelectedSpecies(stack);

        if (selectedSpecies == null) {
            return Text.literal("Species Filter");
        }

        SpeciesDefinition definition = NuclideDataLoader.SPECIES.getById(selectedSpecies);
        if (definition != null) {
            return Text.literal("Species Filter (" + definition.name() + ")");
        }

        return Text.literal("Species Filter (" + selectedSpecies + ")");
    }

    @Override
    public void appendTooltip(
            ItemStack stack,
            TooltipContext context,
            List<Text> tooltip,
            TooltipType type
    ) {
        String selectedSpecies = getSelectedSpecies(stack);

        if (selectedSpecies == null) {
            tooltip.add(Text.literal("No species selected"));
            return;
        }

        SpeciesDefinition definition = NuclideDataLoader.SPECIES.getById(selectedSpecies);
        if (definition != null) {
            tooltip.add(Text.literal("Species: " + definition.name()));
        } else {
            tooltip.add(Text.literal("Species: <missing>"));
        }

        tooltip.add(Text.literal("ID: " + selectedSpecies));
    }

    public static void setSelectedSpecies(ItemStack stack, @Nullable String speciesId) {
        if (speciesId == null || speciesId.isBlank()) {
            stack.remove(NuclideDataComponents.SELECTED_SPECIES);
            return;
        }

        stack.set(NuclideDataComponents.SELECTED_SPECIES, speciesId);
    }

    public static @Nullable String getSelectedSpecies(ItemStack stack) {
        return stack.get(NuclideDataComponents.SELECTED_SPECIES);
    }

    public static boolean matches(ItemStack stack, String speciesId) {
        String selected = getSelectedSpecies(stack);
        return selected != null && selected.equals(speciesId);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
    }
}