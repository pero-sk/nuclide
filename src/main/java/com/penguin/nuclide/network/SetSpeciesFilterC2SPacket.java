package com.penguin.nuclide.network;

import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.content.items.SpeciesFilterItem;
import com.penguin.nuclide.data.NuclideDataLoader;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public record SetSpeciesFilterC2SPacket(Hand hand, String speciesId) implements CustomPayload {

    public static final Id<SetSpeciesFilterC2SPacket> ID =
            new Id<>(Identifier.of(Nuclide.MOD_ID, "set_species_filter"));

    public static final PacketCodec<RegistryByteBuf, SetSpeciesFilterC2SPacket> CODEC =
            CustomPayload.codecOf(SetSpeciesFilterC2SPacket::write, SetSpeciesFilterC2SPacket::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private void write(PacketByteBuf buf) {
        buf.writeEnumConstant(hand);
        buf.writeString(speciesId);
    }

    private static SetSpeciesFilterC2SPacket read(PacketByteBuf buf) {
        Hand hand = buf.readEnumConstant(Hand.class);
        String speciesId = buf.readString();
        return new SetSpeciesFilterC2SPacket(hand, speciesId);
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            context.server().execute(() -> {
                if (payload.speciesId() == null || payload.speciesId().isBlank()) {
                    return;
                }

                if (NuclideDataLoader.SPECIES.getById(payload.speciesId()) == null) {
                    return;
                }

                ItemStack stack = context.player().getStackInHand(payload.hand());
                if (!(stack.getItem() instanceof SpeciesFilterItem)) {
                    return;
                }

                SpeciesFilterItem.setSelectedSpecies(stack, payload.speciesId());
            });
        });
    }
}