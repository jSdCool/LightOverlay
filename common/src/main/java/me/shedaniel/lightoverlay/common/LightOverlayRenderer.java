package me.shedaniel.lightoverlay.common;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import static net.minecraft.client.renderer.RenderPipelines.MATRICES_PROJECTION_SNIPPET;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Consumer;
import java.util.function.Function;

public class LightOverlayRenderer implements Consumer<PoseStack> {
    private static final RenderPipeline LINE_PIPELINE = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
            .withLocation("pipeline/debug_line_strip")
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES)
            .build();
    private static final Function<Object, RenderType> LINE = Util.memoize(
            double_ -> RenderType.create("light_overlay_lines",
                    RenderSetup.builder(LINE_PIPELINE).createRenderSetup()
            )
            /*
            256,
                    ,
                    RenderType.CompositeState.builder()
                            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(double_)))
                            .createCompositeState(false)
            */

    );
    

    public Frustum frustum;
    public LightOverlayTicker ticker;
    
    public LightOverlayRenderer(LightOverlayTicker ticker) {
        this.ticker = ticker;
    }
    
    @Override
    public void accept(PoseStack poses) {//this parameter is not necessary anymore
        Minecraft minecraft = Minecraft.getInstance();
        if (LightOverlay.enabled) {
            LocalPlayer playerEntity = minecraft.player;
            BlockPos playerPos = new BlockPos(playerEntity.getBlockX(), playerEntity.getBlockY(), playerEntity.getBlockZ());
            int playerPosX = playerPos.getX() >> 4;
            int playerPosY = playerPos.getY() >> 5;
            int playerPosZ = playerPos.getZ() >> 4;
            CollisionContext collisionContext = CollisionContext.of(playerEntity);
            int chunkRange = LightOverlay.getChunkRange();
            
            if (LightOverlay.showNumber) {
                renderLevels(playerPos, playerPosX, playerPosY, playerPosZ, chunkRange);
            } else {
                renderCrosses(playerPos, playerPosX, playerPosY, playerPosZ, chunkRange, collisionContext);
            }
            minecraft.renderBuffers().bufferSource().endLastBatch();
        }
    }
    
    private void renderLevels(BlockPos playerPos, int playerPosX, int playerPosY, int playerPosZ, int chunkRange) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos downMutable = new BlockPos.MutableBlockPos();
        for (Map.Entry<CubicChunkPos, Long2ByteMap> entry : ticker.CHUNK_MAP.entrySet()) {
            CubicChunkPos chunkPos = entry.getKey();
            if (LightOverlay.caching && (Mth.abs(chunkPos.x - playerPosX) > chunkRange || Mth.abs(chunkPos.y - playerPosY) > Math.max(1, chunkRange >> 1) || Mth.abs(chunkPos.z - playerPosZ) > chunkRange)) {
                continue;
            }
            for (Long2ByteMap.Entry objectEntry : entry.getValue().long2ByteEntrySet()) {
                mutable.set(objectEntry.getLongKey());
                if (mutable.closerThan(playerPos, LightOverlay.reach)) {
                    if (isFrustumVisible(mutable.getX(), mutable.getY(), mutable.getZ(), mutable.getX() + 1, mutable.getX() + 1, mutable.getX() + 1)) {
                        downMutable.set(mutable.getX(), mutable.getY() - 1, mutable.getZ());
                        renderLevel(downMutable, objectEntry.getByteValue());
                    }
                }
            }
        }
    }
    
    public void renderLevel(BlockPos down, byte level) {
        String text = String.valueOf(level);
        int color = level > LightOverlay.higherCrossLevel ? 0xff042404 : (LightOverlay.lowerCrossLevel >= 0 && level > LightOverlay.lowerCrossLevel ? 0xff0066ff : 0xff731111);
        Gizmos.billboardText(text,Vec3.atLowerCornerWithOffset(down, 0.5, 1.3 + 1 * 0.2, 0.5), TextGizmo.Style.forColorAndCentered(color).withScale(1));
//        Gizmos.billboardTextOverBlock(text,down,1,color,1);

    }
    
    private void renderCrosses(BlockPos playerPos, int playerPosX, int playerPosY, int playerPosZ, int chunkRange, CollisionContext collisionContext) {
        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource source = minecraft.renderBuffers().bufferSource();
        VertexConsumer buffer = source.getBuffer(LINE.apply((double) LightOverlay.lineWidth));
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        
        for (Map.Entry<CubicChunkPos, Long2ByteMap> entry : ticker.CHUNK_MAP.entrySet()) {
            CubicChunkPos chunkPos = entry.getKey();
            if (LightOverlay.caching && (Mth.abs(chunkPos.x - playerPosX) > chunkRange || Mth.abs(chunkPos.y - playerPosY) > Math.max(1, chunkRange >> 1) || Mth.abs(chunkPos.z - playerPosZ) > chunkRange)) {
                continue;
            }
            
            for (Long2ByteMap.Entry objectEntry : entry.getValue().long2ByteEntrySet()) {
                byte crossType = objectEntry.getByteValue();
                mutable.set(objectEntry.getLongKey());
                if (mutable.closerThan(playerPos, LightOverlay.reach)) {
                    if (isFrustumVisible(mutable.getX(), mutable.getY(), mutable.getZ(), mutable.getX() + 1, mutable.getX() + 1, mutable.getX() + 1)) {
                        int color = switch (crossType) {
                            case LightOverlay.CROSS_RED -> LightOverlay.redColor;
                            case LightOverlay.CROSS_YELLOW -> LightOverlay.yellowColor;
                            default -> LightOverlay.secondaryColor;
                        };
                        renderCross(minecraft.level, mutable, color, collisionContext);
                    }
                }
            }
        }
    }
    
    public void renderCross(Level world, BlockPos pos, int color, CollisionContext collisionContext) {
        float blockOffset = 0;
        VoxelShape upperOutlineShape = world.getBlockState(pos).getShape(world, pos, collisionContext);
        if (!upperOutlineShape.isEmpty()) {
            blockOffset += (float) upperOutlineShape.max(Direction.Axis.Y);
        }
        color |= 0xFF000000;//set the alpha value to full
//        System.out.println(Integer.toHexString(color));
        float x = pos.getX()/* - cameraX*/;
        float y = pos.getY()/* - cameraY*/ + blockOffset;
        float z = pos.getZ()/* - cameraZ*/;
        Gizmos.line(new Vec3(x + .01f, y, z + .01f),new Vec3(x + .99f, y, z + .99f),color,LightOverlay.lineWidth);
        Gizmos.line(new Vec3(x + .99f, y, z + .01f),new Vec3(x + .01f, y, z + .99f),color,LightOverlay.lineWidth);
    }
    
    public boolean isFrustumVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return frustum.isVisible(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
    }
}
