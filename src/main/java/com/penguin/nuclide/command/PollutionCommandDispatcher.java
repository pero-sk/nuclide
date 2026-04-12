package com.penguin.nuclide.command;

import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.penguin.nuclide.pollution.PollutionManager;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class PollutionCommandDispatcher {
    private PollutionCommandDispatcher() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("nuclide")
                .then(literal("pollution")
                    .executes(context -> {
                        int amount = PollutionManager.getPollution(context.getSource().getWorld(), context.getSource().getPlayer().getBlockPos());
                        context.getSource().getPlayer().sendMessage(Text.literal("Pollution: "+amount));
                        return 1;
                    })
            )
        );
    }
}
