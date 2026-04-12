package com.penguin.nuclide.content.items;

import java.util.List;

import com.penguin.nuclide.nbt.NuclideDataComponents;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.species.SpeciesContainer;
import com.penguin.nuclide.species.SpeciesStack;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

public class CanisterItem extends Item {

    public CanisterItem(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("nuclide.canister");
    }

    @Override
    public void appendTooltip(
            ItemStack stack,
            TooltipContext context,
            List<Text> tooltip,
            TooltipType type
    ) {
        SpeciesContainer container = getContainer(stack);

        if (container.isEmpty()) {
            tooltip.add(Text.translatable("nuclide.canister.empty.tooltip"));
            return;
        }

        tooltip.add(Text.translatable("nuclide.canister.full.tooltip"));

        for (SpeciesStack speciesStack : container.stacks()) {
            SpeciesDefinition definition =
                    NuclideDataLoader.SPECIES.getById(speciesStack.speciesId());

            String displayName = definition != null
                    ? definition.name()
                    : speciesStack.speciesId();

            tooltip.add(Text.literal("- " + displayName + ": " + speciesStack.count() + " mmol" + " (" + speciesStack.definition().state().toString().toLowerCase() + ")"));
        }
    }

    private SpeciesContainer getContainer(ItemStack stack) {
        return stack.getOrDefault(
                NuclideDataComponents.SPECIES_CONTAINER,
                new SpeciesContainer()
        );
    }

    public static void setContainer(ItemStack stack, SpeciesContainer container) {
        stack.set(NuclideDataComponents.SPECIES_CONTAINER, container.copy());
    }
}