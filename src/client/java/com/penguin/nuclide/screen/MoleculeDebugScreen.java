package com.penguin.nuclide.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.penguin.nuclide.atomic.Molecule;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.nowns.NownsParser;
import com.penguin.nuclide.renderer.MoleculeLayout;
import com.penguin.nuclide.renderer.MoleculeLayouter;
import com.penguin.nuclide.renderer.MoleculeRenderer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class MoleculeDebugScreen extends Screen {

    private static final int TOP_MARGIN = 40;
    private static final int LEFT_LABEL_X = 40;
    private static final int ENTRY_HEIGHT = 90;
    private static final int MOLECULE_CENTER_Y_OFFSET = 48;
    private static final double SCROLL_STEP = 24.0;
    private static final double PAGE_SCROLL_STEP = ENTRY_HEIGHT * 3.0;

    private final List<Entry> entries = new ArrayList<>();

    private double scrollAmount = 0.0;
    private int contentHeight = 0;

    public MoleculeDebugScreen() {
        super(Text.literal("Nuclide Molecule Debug"));
        loadAllSpecies();
    }

    @Override
    protected void init() {
        super.init();
        clampScroll();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int visibleTop = TOP_MARGIN;
        int visibleBottom = this.height - 16;

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                centerX,
                15,
                0xFFFFFF
        );

        context.drawTextWithShadow(
                this.textRenderer,
                Text.literal("Species: " + entries.size()),
                10,
                10,
                0xAAAAAA
        );

        int y = TOP_MARGIN - (int) Math.round(scrollAmount);

        for (Entry entry : entries) {
            int entryTop = y;
            int entryBottom = y + ENTRY_HEIGHT;

            if (entryBottom >= visibleTop && entryTop <= visibleBottom) {
                drawEntry(context, entry, centerX, y);
            }

            y += ENTRY_HEIGHT;
        }

        drawScrollBar(context);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawEntry(DrawContext context, Entry entry, int centerX, int y) {
        int textColor = entry.failed() ? 0xFF7777 : 0xFFFFFF;
        int subTextColor = 0xAAAAAA;

        context.drawTextWithShadow(
                this.textRenderer,
                Text.literal(entry.displayName()),
                LEFT_LABEL_X,
                y + 4,
                textColor
        );

        context.drawTextWithShadow(
                this.textRenderer,
                Text.literal(entry.id()),
                LEFT_LABEL_X,
                y + 16,
                subTextColor
        );

        if (entry.layout() != null) {
            MoleculeRenderer.render(
                    context,
                    this.textRenderer,
                    entry.layout(),
                    centerX,
                    y + MOLECULE_CENTER_Y_OFFSET
            );
        } else {
            String reason = entry.errorMessage() == null ? "Failed to load" : entry.errorMessage();
            context.drawTextWithShadow(
                    this.textRenderer,
                    Text.literal(reason),
                    LEFT_LABEL_X,
                    y + 38,
                    0xFF7777
            );
        }
    }

    private void drawScrollBar(DrawContext context) {
        int visibleHeight = this.height - TOP_MARGIN - 16;
        if (contentHeight <= visibleHeight || visibleHeight <= 0) {
            return;
        }

        int barLeft = this.width - 8;
        int barRight = this.width - 4;
        int trackTop = TOP_MARGIN;
        int trackBottom = this.height - 16;
        int trackHeight = trackBottom - trackTop;

        context.fill(barLeft, trackTop, barRight, trackBottom, 0x66000000);

        int thumbHeight = Math.max(20, (int) ((visibleHeight / (double) contentHeight) * trackHeight));
        int maxScroll = Math.max(0, contentHeight - visibleHeight);
        int maxThumbTravel = trackHeight - thumbHeight;

        int thumbTop = trackTop;
        if (maxScroll > 0 && maxThumbTravel > 0) {
            thumbTop += (int) Math.round((scrollAmount / maxScroll) * maxThumbTravel);
        }

        context.fill(barLeft, thumbTop, barRight, thumbTop + thumbHeight, 0xCCAAAAAA);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount != 0.0) {
            scrollAmount -= verticalAmount * SCROLL_STEP;
            clampScroll();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 264 -> { // down
                scrollAmount += SCROLL_STEP;
                clampScroll();
                return true;
            }
            case 265 -> { // up
                scrollAmount -= SCROLL_STEP;
                clampScroll();
                return true;
            }
            case 267 -> { // page down
                scrollAmount += PAGE_SCROLL_STEP;
                clampScroll();
                return true;
            }
            case 266 -> { // page up
                scrollAmount -= PAGE_SCROLL_STEP;
                clampScroll();
                return true;
            }
            case 268 -> { // home
                scrollAmount = 0.0;
                clampScroll();
                return true;
            }
            case 269 -> { // end
                scrollAmount = getMaxScroll();
                clampScroll();
                return true;
            }
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    private void clampScroll() {
        scrollAmount = MathHelper.clamp(scrollAmount, 0.0, getMaxScroll());
    }

    private int getMaxScroll() {
        int visibleHeight = this.height - TOP_MARGIN - 16;
        return Math.max(0, contentHeight - visibleHeight);
    }

    private void loadAllSpecies() {
        entries.clear();

        List<SpeciesDefinition> speciesList = new ArrayList<>(NuclideDataLoader.SPECIES.values());
        speciesList.sort(Comparator.comparing(SpeciesDefinition::id));

        for (SpeciesDefinition species : speciesList) {
            entries.add(loadEntry(species));
        }

        contentHeight = entries.size() * ENTRY_HEIGHT;
    }

    private Entry loadEntry(SpeciesDefinition species) {
        String id = species.id();
        String displayName = species.name();

        try {
            String nowns = species.normalizedNowns();
            if (nowns == null || nowns.isBlank()) {
                return new Entry(id, displayName, null, true, "No NOWNS");
            }

            Molecule molecule = NownsParser.parse(nowns).molecule();
            MoleculeLayout layout = MoleculeLayouter.layout(molecule);
            return new Entry(id, displayName, layout, false, null);
        } catch (Exception e) {
            return new Entry(id, displayName, null, true, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private record Entry(
            String id,
            String displayName,
            @Nullable MoleculeLayout layout,
            boolean failed,
            @Nullable String errorMessage
    ) {}
}