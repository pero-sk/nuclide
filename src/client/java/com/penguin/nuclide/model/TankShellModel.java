package com.penguin.nuclide.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.penguin.nuclide.atomic.StateType;
import com.penguin.nuclide.content.blockentities.species_tank.SpeciesTankCasingBlockEntity;
import com.penguin.nuclide.content.blockentities.species_tank.SpeciesTankControllerBlockEntity;
import com.penguin.nuclide.content.registry.ModBlocks;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.species.SpeciesKey;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

@Environment(EnvType.CLIENT)
public final class TankShellModel implements BakedModel, FabricBakedModel {

    private static final Identifier CAP_ID = Identifier.of("nuclide", "block/tank/cap");

    private static final Identifier SIDE_SINGLE_ID = Identifier.of("nuclide", "block/tank/side_single");
    private static final Identifier SIDE_BOTTOM_ID = Identifier.of("nuclide", "block/tank/side_bottom_connected");
    private static final Identifier SIDE_MIDDLE_ID = Identifier.of("nuclide", "block/tank/side_middle");
    private static final Identifier SIDE_TOP_ID = Identifier.of("nuclide", "block/tank/side_top_connected");

    private static final Identifier WINDOW_LEFT_ID = Identifier.of("nuclide", "block/tank/window_left");
    private static final Identifier WINDOW_RIGHT_ID = Identifier.of("nuclide", "block/tank/window_right");

    private static final Identifier WINDOW_UP_LEFT_ID = Identifier.of("nuclide", "block/tank/window_up_left");
    private static final Identifier WINDOW_UP_RIGHT_ID = Identifier.of("nuclide", "block/tank/window_up_right");

    private static final Identifier WINDOW_DOWN_LEFT_ID = Identifier.of("nuclide", "block/tank/window_down_left");
    private static final Identifier WINDOW_DOWN_RIGHT_ID = Identifier.of("nuclide", "block/tank/window_down_right");

    private static final Identifier WINDOW_SINGLE_LEFT_ID = Identifier.of("nuclide", "block/tank/window_single_left");
    private static final Identifier WINDOW_SINGLE_RIGHT_ID = Identifier.of("nuclide", "block/tank/window_single_right");

    private static final Identifier WHITE_ID = Identifier.of("minecraft", "block/white_concrete");

    private record FaceAppearance(Identifier textureId, RenderMaterial material, boolean isWindow) {}

    private static RenderMaterial solidMaterial() {
        return RendererAccess.INSTANCE.getRenderer()
                .materialFinder()
                .blendMode(0, BlendMode.SOLID)
                .find();
    }

    private static RenderMaterial cutoutMaterial() {
        return RendererAccess.INSTANCE.getRenderer()
                .materialFinder()
                .blendMode(0, BlendMode.CUTOUT)
                .find();
    }

    private static Sprite sprite(Identifier id) {
        return MinecraftClient.getInstance()
                .getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
                .apply(id);
    }

    private static Direction faceLeft(Direction face) {
        return switch (face) {
            case NORTH -> Direction.WEST;
            case SOUTH -> Direction.EAST;
            case WEST -> Direction.SOUTH;
            case EAST -> Direction.NORTH;
            default -> throw new IllegalArgumentException("Invalid side face: " + face);
        };
    }

    private static Direction faceRight(Direction face) {
        return switch (face) {
            case NORTH -> Direction.EAST;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            case EAST -> Direction.SOUTH;
            default -> throw new IllegalArgumentException("Invalid side face: " + face);
        };
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(
            BlockRenderView blockView,
            BlockState state,
            BlockPos pos,
            Supplier<Random> randomSupplier,
            RenderContext context
    ) {
        emitShell(blockView, pos, context);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        emitFace(context, Direction.NORTH, sprite(SIDE_SINGLE_ID), solidMaterial());
        emitFace(context, Direction.SOUTH, sprite(SIDE_SINGLE_ID), solidMaterial());
        emitFace(context, Direction.EAST, sprite(SIDE_SINGLE_ID), solidMaterial());
        emitFace(context, Direction.WEST, sprite(SIDE_SINGLE_ID), solidMaterial());
        emitFace(context, Direction.UP, sprite(CAP_ID), solidMaterial());
        emitFace(context, Direction.DOWN, sprite(CAP_ID), solidMaterial());
    }

    private void emitShell(BlockRenderView world, BlockPos pos, RenderContext context) {
        for (Direction face : Direction.values()) {
            if (connectsToTank(world, pos.offset(face))) {
                continue;
            }

            if (face == Direction.UP || face == Direction.DOWN) {
                emitFace(context, face, sprite(CAP_ID), solidMaterial());
                continue;
            }

            FaceAppearance appearance = pickSideAppearance(world, pos, face);

            if (appearance.isWindow()) {
                emitWindowFill(context, face, world, pos);
            }

            emitFace(context, face, sprite(appearance.textureId()), appearance.material());
        }
    }

    private boolean connectsToTank(BlockRenderView world, BlockPos pos) {
        BlockState neighbor = world.getBlockState(pos);
        return neighbor.isOf(ModBlocks.SPECIES_TANK_CASING)
                || neighbor.isOf(ModBlocks.SPECIES_TANK_CONTROLLER);
    }

    private FaceAppearance pickSideAppearance(BlockRenderView world, BlockPos pos, Direction face) {
        boolean hasTop = connectsToTank(world, pos.up());
        boolean hasBottom = connectsToTank(world, pos.down());

        boolean hasLeftNeighbor = connectsToTank(world, pos.offset(faceLeft(face)));
        boolean hasRightNeighbor = connectsToTank(world, pos.offset(faceRight(face)));

        if (!hasTop && !hasBottom) {
            if (hasRightNeighbor) return new FaceAppearance(WINDOW_SINGLE_LEFT_ID, cutoutMaterial(), true);
            if (hasLeftNeighbor) return new FaceAppearance(WINDOW_SINGLE_RIGHT_ID, cutoutMaterial(), true);
            return new FaceAppearance(SIDE_SINGLE_ID, solidMaterial(), false);
        }

        if (!hasBottom) {
            if (hasRightNeighbor) return new FaceAppearance(WINDOW_UP_LEFT_ID, cutoutMaterial(), true);
            if (hasLeftNeighbor) return new FaceAppearance(WINDOW_UP_RIGHT_ID, cutoutMaterial(), true);
            return new FaceAppearance(SIDE_BOTTOM_ID, solidMaterial(), false);
        }

        if (!hasTop) {
            if (hasRightNeighbor) return new FaceAppearance(WINDOW_DOWN_LEFT_ID, cutoutMaterial(), true);
            if (hasLeftNeighbor) return new FaceAppearance(WINDOW_DOWN_RIGHT_ID, cutoutMaterial(), true);
            return new FaceAppearance(SIDE_TOP_ID, solidMaterial(), false);
        }

        if (hasRightNeighbor) return new FaceAppearance(WINDOW_LEFT_ID, cutoutMaterial(), true);
        if (hasLeftNeighbor) return new FaceAppearance(WINDOW_RIGHT_ID, cutoutMaterial(), true);

        return new FaceAppearance(SIDE_MIDDLE_ID, solidMaterial(), false);
    }

    // Returns the corresponding non-window solid texture ID for a given window face appearance.
    private Identifier solidTextureFor(BlockRenderView world, BlockPos pos, Direction face) {
        boolean hasTop    = connectsToTank(world, pos.up());
        boolean hasBottom = connectsToTank(world, pos.down());

        if (!hasTop && !hasBottom) return SIDE_SINGLE_ID;
        if (!hasBottom)            return SIDE_BOTTOM_ID;
        if (!hasTop)               return SIDE_TOP_ID;
        return SIDE_MIDDLE_ID;
    }

    private void emitFace(RenderContext context, Direction face, Sprite sprite, RenderMaterial material) {
        var emitter = context.getEmitter();
        emitter.material(material);
        emitter.square(face, 0, 0, 1, 1, 0);
        emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);
        emitter.color(-1, -1, -1, -1);
        emitter.emit();
    }

    private void emitWindowFill(RenderContext context, Direction face, BlockRenderView world, BlockPos pos) {
        // Resolve controller — block might be a casing OR the controller itself
        SpeciesTankControllerBlockEntity controller = null;
        BlockPos controllerPos = pos;

        if (world.getBlockEntity(pos) instanceof SpeciesTankCasingBlockEntity tank) {
            BlockPos c = tank.getControllerPos();
            if (c != null && world.getBlockEntity(c) instanceof SpeciesTankControllerBlockEntity ctrl) {
                controller = ctrl;
                controllerPos = c;
            }
        } else if (world.getBlockEntity(pos) instanceof SpeciesTankControllerBlockEntity ctrl) {
            controller = ctrl;
            controllerPos = pos;
        }

        if (controller == null || !controller.isFormed()) {
            // No controller or not formed — fill entire window with solid texture
            Identifier solidTex = solidTextureFor(world, pos, face);
            emitPartialSolidQuad(context, face, 0.0f, sprite(solidTex));
            return;
        }

        int totalHeight = controller.getTankHeight();
        if (totalHeight <= 0) return;

        // Which layer is this block? 0 = bottom, totalHeight-1 = top
        int layerIndex = pos.getY() - controllerPos.getY();

        float fillAmount = getWindowFillAmount(world, pos);
        float filledBlocks = fillAmount * totalHeight;

        // This block's local fill fraction: fully filled, partial, or empty
        float localFill;
        if (layerIndex < (int) filledBlocks) {
            localFill = 1.0f;
        } else if (layerIndex == (int) filledBlocks) {
            localFill = filledBlocks - (int) filledBlocks;
        } else {
            localFill = 0.0f;
        }

        int argb = getWindowFillColor(world, pos);

        // Emit colored fill quad for the filled portion (bottom)
        if (localFill > 0.0001f) {
            emitWindowFillQuad(context, face, localFill, argb);
        }

        // Emit solid texture background for the empty portion (top)
        if (localFill < 0.9999f) {
            Identifier solidTex = solidTextureFor(world, pos, face);
            emitPartialSolidQuad(context, face, localFill, sprite(solidTex));
        }
    }

    private void emitWindowFillQuad(RenderContext context, Direction face, float fillAmount, int argb) {
        float clampedFill = clamp01(fillAmount);
        if (clampedFill <= 0.0001f) return;

        float left   = 0.0f;
        float right  = 1.0f;
        float bottom = 0.0f;
        float top    = clampedFill;
        float depth  = 0.02f;

        Sprite white = sprite(WHITE_ID);
        float u = white.getFrameU(0.5f);
        float v = white.getFrameV(0.5f);

        var emitter = context.getEmitter();
        emitter.material(solidMaterial());
        emitter.square(face, left, bottom, right, top, depth);
        emitter.uv(0, u, v);
        emitter.uv(1, u, v);
        emitter.uv(2, u, v);
        emitter.uv(3, u, v);
        emitter.color(argb, argb, argb, argb);
        emitter.emit();
    }

    private void emitPartialSolidQuad(RenderContext context, Direction face, float fillFrom, Sprite sprite) {
        float left   = 0.0f;
        float right  = 1.0f;
        float bottom = fillFrom;
        float top    = 1.0f;
        float depth  = 0.02f;

        int tint = 0xFF6a6a6a; //RRGGBBAA

        var emitter = context.getEmitter();
        emitter.material(solidMaterial());
        emitter.square(face, left, bottom, right, top, depth);
        emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);
        emitter.color(tint, tint, tint, tint);
        emitter.emit();
    }

    private int getWindowFillColor(BlockRenderView world, BlockPos pos) {
        PhaseFractions f = getPhaseFractions(world, pos);
        float solid  = f.solid();
        float liquid = f.liquid();
        float gas    = f.gas();

        float total = solid + liquid + gas;
        if (total <= 0.0001f) {
            return 0xFF202020;
        }

        solid  /= total;
        liquid /= total;
        gas    /= total;

        float r = solid * 160.0f + liquid *  64.0f + gas * 180.0f;
        float g = solid * 110.0f + liquid * 140.0f + gas * 220.0f;
        float b = solid *  70.0f + liquid * 255.0f + gas * 120.0f;

        return 0xFF000000 | (clamp255(r) << 16) | (clamp255(g) << 8) | clamp255(b);
    }

    private int clamp255(float value) {
        int i = Math.round(value);
        if (i < 0)   return 0;
        if (i > 255) return 255;
        return i;
    }

    private float getWindowFillAmount(BlockRenderView world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof SpeciesTankCasingBlockEntity tank) {
            BlockPos c = tank.getControllerPos();
            if (c == null) {
                return 0f;
            }
            if (world.getBlockEntity(c) instanceof SpeciesTankControllerBlockEntity controller) {
                int total    = controller.getContainer().asMap().values().stream().mapToInt(Integer::intValue).sum();
                int capacity = controller.getCapacity();
                if (capacity > 0) return clamp01((float) total / capacity);
            }
        } else if (world.getBlockEntity(pos) instanceof SpeciesTankControllerBlockEntity controller) {
            int total    = controller.getContainer().asMap().values().stream().mapToInt(Integer::intValue).sum();
            int capacity = controller.getCapacity();
            if (capacity > 0) return clamp01((float) total / capacity);
        }
        return 0f;
    }

    private record PhaseFractions(float solid, float liquid, float gas) {}

    private PhaseFractions getPhaseFractions(BlockRenderView world, BlockPos pos) {
        Map<SpeciesKey, Integer> m = null;

        if (world.getBlockEntity(pos) instanceof SpeciesTankCasingBlockEntity tank) {
            BlockPos c = tank.getControllerPos();
            if (c == null) {
                return new PhaseFractions(0f, 0f, 0f);
            }
            if (world.getBlockEntity(c) instanceof SpeciesTankControllerBlockEntity controller) {
                m = controller.getContainer().asMap();
            }
        } else if (world.getBlockEntity(pos) instanceof SpeciesTankControllerBlockEntity controller) {
            m = controller.getContainer().asMap();
        }

        if (m == null) return new PhaseFractions(0f, 0f, 0f);

        int grandTotal = m.values().stream().mapToInt(Integer::intValue).sum();
        if (grandTotal <= 0) return new PhaseFractions(0f, 0f, 0f);

        return new PhaseFractions(
            fractionOf(m, grandTotal, StateType.SOLID),
            fractionOf(m, grandTotal, StateType.LIQUID),
            fractionOf(m, grandTotal, StateType.GAS)
        );
    }

    private float fractionOf(Map<SpeciesKey, Integer> m, int grandTotal, StateType target) {
        int sum = m.entrySet().stream()
            .filter(e -> {
                SpeciesDefinition def = NuclideDataLoader.SPECIES.getById(e.getKey().speciesId());
                return def != null && def.state() == target;
            })
            .mapToInt(Map.Entry::getValue)
            .sum();
        return (float) sum / grandTotal;
    }

    private float clamp01(float value) {
        if (value < 0.0f) return 0.0f;
        if (value > 1.0f) return 1.0f;
        return value;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, Random random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean hasDepth() {
        return true;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return sprite(SIDE_SINGLE_ID);
    }

    @Override
    public ModelTransformation getTransformation() {
        return ModelTransformation.NONE;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }
}