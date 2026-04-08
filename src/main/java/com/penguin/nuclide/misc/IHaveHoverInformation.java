package com.penguin.nuclide.misc;

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public interface IHaveHoverInformation {

    boolean addHoverInformation(
            World world,
            BlockHitResult hit,
            PlayerEntity player,
            List<Text> tooltip
    );
}