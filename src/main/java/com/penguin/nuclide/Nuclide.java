package com.penguin.nuclide;

import com.penguin.nuclide.command.ContainerCommandDispatcher;
import com.penguin.nuclide.command.PollutionCommandDispatcher;
import com.penguin.nuclide.command.ReactionCommandDispatcher;
import com.penguin.nuclide.command.SpeciesCommandDispatcher;
import com.penguin.nuclide.command.TagCommandDispatcher;
import com.penguin.nuclide.nbt.NuclideDataComponents;
import com.penguin.nuclide.network.SetSpeciesFilterC2SPacket;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.gas.GasRegistry;
import com.penguin.nuclide.reaction.ReactionDataLoader;
import com.penguin.nuclide.tag.SpeciesTagDataLoader;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.content.registry.ModBlocks;
import com.penguin.nuclide.content.registry.ModItems;
import net.minecraft.registry.RegistryKey;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nuclide implements ModInitializer {
    public static final String MOD_ID = "nuclide";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final int DEFAULT_TRANSFER_RATE = 600;

    // being realistic, 1 minecraft block = 1 cubic meter, 
    // which at that scale would be around 44,600 mmols of gas at standard temperature and pressure.
    // which would just be horrible UX for users,
    // so we'll use a 1/10th-ish scale of 4,500 mmols per block for gameplay purposes.
    public static int MMOL_PER_BLOCK = 4500;

    public static final GasRegistry GASES = new GasRegistry();

    public static final RegistryKey<ItemGroup> NUCLIDE_ITEMGROUP_KEY =
            RegistryKey.of(Registries.ITEM_GROUP.getKey(), Nuclide.asIdentifier("item_group"));

    public static ItemGroup NUCLIDE_ITEMGROUP = Registry.register(
            Registries.ITEM_GROUP,
            Nuclide.asIdentifier("item_group"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.nuclide.main"))
                    .icon(() -> new ItemStack(ModItems.HYDROGEN_FURNACE_BLOCKITEM))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.ELECTROLYSER_BLOCKITEM);
                        entries.add(ModItems.BINDER_BLOCKITEM);
                        entries.add(ModItems.HYDROGEN_FURNACE_BLOCKITEM);

                        entries.add(ModItems.HAND_CRANK_BLOCKITEM);
                        entries.add(ModItems.CREATIVE_ENERGISER_BLOCKITEM);

                        entries.add(ModItems.PIPE_BLOCKITEM);
                        entries.add(ModItems.PUMP_BLOCKITEM);
                        entries.add(ModItems.CABLE);

                        entries.add(ModItems.SPECIESTANK_BLOCKITEM);
                        entries.add(ModItems.CANISTER_ITEM);

                        entries.add(ModItems.SPECIES_FILTER_ITEM);
                    })
                    .build()
    );

    @Override
    public void onInitialize() {
        NuclideDataComponents.init();

        NuclideDataLoader.register();

        SetSpeciesFilterC2SPacket.register();

        ModBlocks.register();
        ModBlockEntities.init();
        ModItems.register();

        SpeciesTagDataLoader.register();
        ReactionDataLoader.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ReactionCommandDispatcher.register(dispatcher);
            TagCommandDispatcher.register(dispatcher);
            SpeciesCommandDispatcher.register(dispatcher);
            ContainerCommandDispatcher.register(dispatcher);
            PollutionCommandDispatcher.register(dispatcher);
        });

        LOGGER.info("Nuclide initialized");
    }

    public static Identifier asIdentifier(String path) {
        return Identifier.of(MOD_ID, path);
    }
}