package com.penguin.nuclide.model;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.content.blocks.pipe.PipeConnections;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
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
public final class PipeModel implements BakedModel {
    private static final Identifier CORE_FRONT_TEXTURE_ID = Nuclide.asIdentifier("block/pipe/core_front");
    private static final Identifier CORE_SIDE_TEXTURE_ID  = Nuclide.asIdentifier("block/pipe/core_side");
    private static final Identifier CORE_TOP_TEXTURE_ID   = Nuclide.asIdentifier("block/pipe/core_top");

    private static final Identifier ARM_FRONT_TEXTURE_ID  = Nuclide.asIdentifier("block/pipe/arm_front");
    private static final Identifier ARM_SIDE_TEXTURE_ID   = Nuclide.asIdentifier("block/pipe/arm_side");
    private static final Identifier ARM_TOP_TEXTURE_ID    = Nuclide.asIdentifier("block/pipe/arm_top");
    private static final Identifier ARM_BOTTOM_TEXTURE_ID = Nuclide.asIdentifier("block/pipe/arm_bottom");

    // Core: [5,5,5] -> [11,11,11]
    private static final float CORE_MIN_X = 5f / 16f;
    private static final float CORE_MIN_Y = 5f / 16f;
    private static final float CORE_MIN_Z = 5f / 16f;
    private static final float CORE_MAX_X = 11f / 16f;
    private static final float CORE_MAX_Y = 11f / 16f;
    private static final float CORE_MAX_Z = 11f / 16f;

    // North arm: [5.5,5,0] -> [10.5,10,8]
    private static final float ARM_MIN_X = 5.5f / 16f;
    private static final float ARM_MIN_Y = 5f / 16f;
    private static final float ARM_MIN_Z = 0f / 16f;
    private static final float ARM_MAX_X = 10.5f / 16f;
    private static final float ARM_MAX_Y = 10f / 16f;
    private static final float ARM_MAX_Z = 5f / 16f;

    private static RenderMaterial solidMaterial() {
        return RendererAccess.INSTANCE.getRenderer()
                .materialFinder()
                .blendMode(BlendMode.SOLID)
                .find();
    }

    private static Sprite sprite(Identifier id) {
        return MinecraftClient.getInstance()
                .getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
                .apply(id);
    }

    public PipeModel() {}

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
        emitCore(context);

        for (Direction direction : Direction.values()) {
            if (PipeConnections.client_connectsTo(blockView, pos, direction)) {
                emitArm(context, direction);
            }
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        emitCore(context);
        emitArm(context, Direction.UP);
    }

    private void emitCore(RenderContext context) {
        Sprite coreFront = sprite(CORE_FRONT_TEXTURE_ID);
        Sprite coreSide = sprite(CORE_SIDE_TEXTURE_ID);
        Sprite coreTop = sprite(CORE_TOP_TEXTURE_ID);

        emitFaceWorld(
                context, coreFront, Direction.NORTH,
                CORE_MIN_X, CORE_MIN_Y, CORE_MIN_Z,
                CORE_MAX_X, CORE_MAX_Y, CORE_MIN_Z
        );

        emitFaceWorld(
                context, coreSide, Direction.EAST,
                CORE_MAX_X, CORE_MIN_Y, CORE_MIN_Z,
                CORE_MAX_X, CORE_MAX_Y, CORE_MAX_Z
        );

        emitFaceWorld(
                context, coreFront, Direction.SOUTH,
                CORE_MIN_X, CORE_MIN_Y, CORE_MAX_Z,
                CORE_MAX_X, CORE_MAX_Y, CORE_MAX_Z
        );

        emitFaceWorld(
                context, coreSide, Direction.WEST,
                CORE_MIN_X, CORE_MIN_Y, CORE_MIN_Z,
                CORE_MIN_X, CORE_MAX_Y, CORE_MAX_Z
        );

        emitFaceWorld(
                context, coreTop, Direction.UP,
                CORE_MIN_X, CORE_MAX_Y, CORE_MIN_Z,
                CORE_MAX_X, CORE_MAX_Y, CORE_MAX_Z
        );

        emitFaceWorld(
                context, coreTop, Direction.DOWN,
                CORE_MIN_X, CORE_MIN_Y, CORE_MIN_Z,
                CORE_MAX_X, CORE_MIN_Y, CORE_MAX_Z
        );
    }

    private void emitArm(RenderContext context, Direction armDirection) {
        Sprite armFront = sprite(ARM_FRONT_TEXTURE_ID);
        Sprite armSide = sprite(ARM_SIDE_TEXTURE_ID);
        Sprite armTop = sprite(ARM_TOP_TEXTURE_ID);
        Sprite armBottom = sprite(ARM_BOTTOM_TEXTURE_ID);

        // North-oriented source arm.
        emitFaceOriented(
                context, armFront, armDirection, Direction.NORTH,
                ARM_MIN_X, ARM_MIN_Y, ARM_MIN_Z,
                ARM_MAX_X, ARM_MAX_Y, ARM_MIN_Z
        );

        emitFaceOriented(
                context, armSide, armDirection, Direction.EAST,
                ARM_MAX_X, ARM_MIN_Y, ARM_MIN_Z,
                ARM_MAX_X, ARM_MAX_Y, ARM_MAX_Z
        );

        emitFaceOriented(
                context, armFront, armDirection, Direction.SOUTH,
                ARM_MIN_X, ARM_MIN_Y, ARM_MAX_Z,
                ARM_MAX_X, ARM_MAX_Y, ARM_MAX_Z
        );

        emitFaceOriented(
                context, armSide, armDirection, Direction.WEST,
                ARM_MIN_X, ARM_MIN_Y, ARM_MIN_Z,
                ARM_MIN_X, ARM_MAX_Y, ARM_MAX_Z
        );

        emitFaceOriented(
                context, armTop, armDirection, Direction.UP,
                ARM_MIN_X, ARM_MAX_Y, ARM_MIN_Z,
                ARM_MAX_X, ARM_MAX_Y, ARM_MAX_Z
        );

        emitFaceOriented(
                context, armBottom, armDirection, Direction.DOWN,
                ARM_MIN_X, ARM_MIN_Y, ARM_MIN_Z,
                ARM_MAX_X, ARM_MIN_Y, ARM_MAX_Z
        );
    }

    private void emitFaceWorld(
            RenderContext context,
            Sprite sprite,
            Direction face,
            float x1, float y1, float z1,
            float x2, float y2, float z2
    ) {
        var emitter = context.getEmitter();
        emitter.material(solidMaterial());

        switch (face) {
            case NORTH -> {
                emitter.pos(0, x1, y1, z1);
                emitter.pos(1, x1, y2, z1);
                emitter.pos(2, x2, y2, z1);
                emitter.pos(3, x2, y1, z1);
            }
            case SOUTH -> {
                emitter.pos(0, x2, y1, z2);
                emitter.pos(1, x2, y2, z2);
                emitter.pos(2, x1, y2, z2);
                emitter.pos(3, x1, y1, z2);
            }
            case WEST -> {
                emitter.pos(0, x1, y1, z2);
                emitter.pos(1, x1, y2, z2);
                emitter.pos(2, x1, y2, z1);
                emitter.pos(3, x1, y1, z1);
            }
            case EAST -> {
                emitter.pos(0, x2, y1, z1);
                emitter.pos(1, x2, y2, z1);
                emitter.pos(2, x2, y2, z2);
                emitter.pos(3, x2, y1, z2);
            }
            case DOWN -> {
                emitter.pos(0, x1, y1, z2);
                emitter.pos(1, x1, y1, z1);
                emitter.pos(2, x2, y1, z1);
                emitter.pos(3, x2, y1, z2);
            }
            case UP -> {
                emitter.pos(0, x1, y2, z1);
                emitter.pos(1, x1, y2, z2);
                emitter.pos(2, x2, y2, z2);
                emitter.pos(3, x2, y2, z1);
            }
        }

        emitter.uv(0, sprite.getMinU(), sprite.getMaxV());
        emitter.uv(1, sprite.getMinU(), sprite.getMinV());
        emitter.uv(2, sprite.getMaxU(), sprite.getMinV());
        emitter.uv(3, sprite.getMaxU(), sprite.getMaxV());

        emitter.nominalFace(face);
        emitter.color(-1, -1, -1, -1);
        emitter.emit();
    }

    private void emitFaceOriented(
            RenderContext context,
            Sprite sprite,
            Direction armDirection,
            Direction localFace,
            float x1, float y1, float z1,
            float x2, float y2, float z2
    ) {
        var emitter = context.getEmitter();
        emitter.material(solidMaterial());

        float[][] verts = switch (localFace) {
            case NORTH -> new float[][] {
                    {x1, y1, z1},
                    {x1, y2, z1},
                    {x2, y2, z1},
                    {x2, y1, z1}
            };
            case SOUTH -> new float[][] {
                    {x2, y1, z2},
                    {x2, y2, z2},
                    {x1, y2, z2},
                    {x1, y1, z2}
            };
            case WEST -> new float[][] {
                    {x1, y1, z2},
                    {x1, y2, z2},
                    {x1, y2, z1},
                    {x1, y1, z1}
            };
            case EAST -> new float[][] {
                    {x2, y1, z1},
                    {x2, y2, z1},
                    {x2, y2, z2},
                    {x2, y1, z2}
            };
            case DOWN -> new float[][] {
                    {x1, y1, z2},
                    {x1, y1, z1},
                    {x2, y1, z1},
                    {x2, y1, z2}
            };
            case UP -> new float[][] {
                    {x1, y2, z1},
                    {x1, y2, z2},
                    {x2, y2, z2},
                    {x2, y2, z1}
            };
        };

        for (int i = 0; i < 4; i++) {
            float[] p = rotatePoint(verts[i][0], verts[i][1], verts[i][2], armDirection);
            emitter.pos(i, p[0], p[1], p[2]);
        }

        emitter.uv(0, sprite.getMinU(), sprite.getMaxV());
        emitter.uv(1, sprite.getMinU(), sprite.getMinV());
        emitter.uv(2, sprite.getMaxU(), sprite.getMinV());
        emitter.uv(3, sprite.getMaxU(), sprite.getMaxV());

        emitter.nominalFace(rotateFace(localFace, armDirection));
        emitter.color(-1, -1, -1, -1);
        emitter.emit();
    }

    private static float[] rotatePoint(float x, float y, float z, Direction direction) {
        final float pivotX = 8f / 16f;
        final float pivotY = 7.5f / 16f;
        final float pivotZ = 8f / 16f;

        float cx = x - pivotX;
        float cy = y - pivotY;
        float cz = z - pivotZ;

        float nx = cx;
        float ny = cy;
        float nz = cz;

        switch (direction) {
            case NORTH -> {
                nx = cx;
                ny = cy;
                nz = cz;
            }
            case SOUTH -> {
                nx = -cx;
                ny = cy;
                nz = -cz;
            }
            case EAST -> {
                nx = -cz;
                ny = cy;
                nz = cx;
            }
            case WEST -> {
                nx = cz;
                ny = cy;
                nz = -cx;
            }
            case UP -> {
                nx = cx;
                ny = -cz;
                nz = cy;
            }
            case DOWN -> {
                nx = cx;
                ny = cz;
                nz = -cy;
            }
        }

        float outX = nx + pivotX;
        float outY = ny + pivotY;
        float outZ = nz + pivotZ;

        // I am NOT doing the maths on why this is needed, but it is so suck it up and deal with it.
        if (direction == Direction.DOWN || direction == Direction.UP) {
            outY += 0.5f / 16f;
        }

        return new float[] { outX, outY, outZ };
    }

    private static Direction rotateFace(Direction original, Direction direction) {
        float x = original.getOffsetX();
        float y = original.getOffsetY();
        float z = original.getOffsetZ();

        float nx = x;
        float ny = y;
        float nz = z;

        switch (direction) {
            case NORTH -> {
                nx = x;
                ny = y;
                nz = z;
            }
            case SOUTH -> {
                nx = -x;
                ny = y;
                nz = -z;
            }
            case EAST -> {
                nx = -z;
                ny = y;
                nz = x;
            }
            case WEST -> {
                nx = z;
                ny = y;
                nz = -x;
            }
            case UP -> {
                nx = x;
                ny = -z;
                nz = y;
            }
            case DOWN -> {
                nx = x;
                ny = z;
                nz = -y;
            }
        }

        return Direction.getFacing(nx, ny, nz);
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
        return sprite(CORE_FRONT_TEXTURE_ID);
    }

    @Override
    public ModelTransformation getTransformation() {
        return ModelHelper.MODEL_TRANSFORM_BLOCK;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }
}