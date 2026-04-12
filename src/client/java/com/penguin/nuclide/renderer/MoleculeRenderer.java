package com.penguin.nuclide.renderer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class MoleculeRenderer {

    private static final int ATOM_SIZE = 14;

    private MoleculeRenderer() {}

    public static void render(
            DrawContext context,
            TextRenderer textRenderer,
            MoleculeLayout layout,
            int centerX,
            int centerY
    ) {
        renderBonds(context, layout, centerX, centerY);
        renderAtoms(context, textRenderer, layout, centerX, centerY);
    }

    private static void renderBonds(
            DrawContext context,
            MoleculeLayout layout,
            int centerX,
            int centerY
    ) {
        for (MoleculeLayout.LayoutBond bond : layout.bonds()) {
            MoleculeLayout.LayoutAtom a = layout.atoms().get(bond.fromAtomIndex());
            MoleculeLayout.LayoutAtom b = layout.atoms().get(bond.toAtomIndex());

            float x1 = centerX + a.x();
            float y1 = centerY + a.y();
            float x2 = centerX + b.x();
            float y2 = centerY + b.y();

            drawBond(context, x1, y1, x2, y2, bond.order());
        }
    }

    private static void renderAtoms(
            DrawContext context,
            TextRenderer textRenderer,
            MoleculeLayout layout,
            int centerX,
            int centerY
    ) {
        for (MoleculeLayout.LayoutAtom atom : layout.atoms()) {
            int x = Math.round(centerX + atom.x());
            int y = Math.round(centerY + atom.y());

            int color = getAtomColor(atom.symbol());

            int left = x - ATOM_SIZE / 2;
            int top = y - ATOM_SIZE / 2;
            int right = left + ATOM_SIZE;
            int bottom = top + ATOM_SIZE;

            context.fill(left, top, right, bottom, color);
            context.drawBorder(left, top, ATOM_SIZE, ATOM_SIZE, 0xFF000000);

            String label = atom.symbol();
            int textWidth = textRenderer.getWidth(label);
            int textX = x - textWidth / 2;
            int textY = y - textRenderer.fontHeight / 2;

            context.drawText(textRenderer, label, textX, textY, 0xFFFFFFFF, false);
        }
    }

    private static void drawBond(DrawContext context, float x1, float y1, float x2, float y2, int order) {
        if (order <= 1) {
            drawLine(context, x1, y1, x2, y2, 0xFFCCCCCC);
            return;
        }

        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);

        if (len <= 0.001f) {
            return;
        }

        float nx = -dy / len;
        float ny = dx / len;

        float spacing = 2.5f;

        if (order == 2) {
            drawLine(context, x1 + nx * spacing, y1 + ny * spacing, x2 + nx * spacing, y2 + ny * spacing, 0xFFCCCCCC);
            drawLine(context, x1 - nx * spacing, y1 - ny * spacing, x2 - nx * spacing, y2 - ny * spacing, 0xFFCCCCCC);
            return;
        }

        drawLine(context, x1, y1, x2, y2, 0xFFCCCCCC);
        drawLine(context, x1 + nx * spacing * 2.0f, y1 + ny * spacing * 2.0f, x2 + nx * spacing * 2.0f, y2 + ny * spacing * 2.0f, 0xFFCCCCCC);
        drawLine(context, x1 - nx * spacing * 2.0f, y1 - ny * spacing * 2.0f, x2 - nx * spacing * 2.0f, y2 - ny * spacing * 2.0f, 0xFFCCCCCC);
    }

    private static void drawLine(DrawContext context, float x1, float y1, float x2, float y2, int color) {
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1))));

        for (int i = 0; i <= steps; i++) {
            float t = (float) i / (float) steps;
            int x = Math.round(x1 + (x2 - x1) * t);
            int y = Math.round(y1 + (y2 - y1) * t);
            context.fill(x, y, x + 1, y + 1, color);
        }
    }

    private static int getAtomColor(String symbol) {
        return switch (symbol) {
            case "H" -> 0xFFE6E6E6;
            case "O" -> 0xFFD94B4B;
            case "C" -> 0xFF444444;
            case "N" -> 0xFF4B6ED9;
            case "S" -> 0xFFD9C44B;
            case "Cl" -> 0xFF4BD96A;
            case "Na" -> 0xFF8A7DFF;
            case "U" -> 0xFF5C8A3A;
            default -> 0xFF888888;
        };
    }
}