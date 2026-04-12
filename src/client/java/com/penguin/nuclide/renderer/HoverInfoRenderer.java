package com.penguin.nuclide.renderer;

import java.util.ArrayList;
import java.util.List;

import com.penguin.nuclide.misc.IHaveHoverInformation;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.minecraft.client.render.RenderTickCounter;

public final class HoverInfoRenderer {

    private HoverInfoRenderer() {}

    public static void render(DrawContext context, RenderTickCounter tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) {
            return;
        }

        if (!(client.crosshairTarget instanceof BlockHitResult hit)) {
            return;
        }

        World world = client.world;
        PlayerEntity player = client.player;

        BlockEntity blockEntity = world.getBlockEntity(hit.getBlockPos());
        if (!(blockEntity instanceof IHaveHoverInformation hoverInfo)) {
            return;
        }

        List<Text> tooltip = new ArrayList<>();
        boolean added = hoverInfo.addHoverInformation(world, hit, player, tooltip);

        if (!added || tooltip.isEmpty()) {
            return;
        }

        renderTooltipBox(context, client, tooltip);
    }

    private static void renderTooltipBox(
            DrawContext context,
            MinecraftClient client,
            List<Text> tooltip
    ) {
        int padding = 6;
        int lineSpacing = 2;

        int maxWidth = 0;
        for (Text line : tooltip) {
            maxWidth = Math.max(maxWidth, client.textRenderer.getWidth(line));
        }

        int lineHeight = client.textRenderer.fontHeight;
        int boxWidth = maxWidth + padding * 2;
        int boxHeight = tooltip.size() * lineHeight
                + Math.max(0, tooltip.size() - 1) * lineSpacing
                + padding * 2;

        int x = 8;
        int y = 8;

        int backgroundColor = 0xAA111111;
        int borderColor = 0x66FFFFFF;

        context.fill(x, y, x + boxWidth, y + boxHeight, backgroundColor);
        context.drawBorder(x, y, boxWidth, boxHeight, borderColor);

        int textY = y + padding;
        for (Text line : tooltip) {
            context.drawTextWithShadow(
                    client.textRenderer,
                    line,
                    x + padding,
                    textY,
                    0xFFFFFF
            );
            textY += lineHeight + lineSpacing;
        }
    }
}