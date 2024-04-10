package de.hype.eggsentials.fabric;

import de.hype.eggsentials.client.common.chat.Chat;
import de.hype.eggsentials.client.common.client.BBsentials;
import de.hype.eggsentials.client.common.config.constants.ClickableArmorStand;
import de.hype.eggsentials.client.common.mclibraries.EnvironmentCore;
import de.hype.eggsentials.shared.objects.Position;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MCEvents implements de.hype.eggsentials.client.common.mclibraries.MCEvents {
    public Utils utils = (Utils) EnvironmentCore.utils;

//    public static void renderWaypoints(WorldRenderContext context) {
//        Camera camera = context.camera();
//
//        MatrixStack matrixStack = new MatrixStack();
//
//        for (Waypoints waypoint : Waypoints.waypoints.values()) {
//            if (waypoint.visible) {
//                Vec3d waypointPosition = new Vec3d(waypoint.position.x, waypoint.position.y, waypoint.position.z);
//                double distance = camera.getPos().distanceTo(waypointPosition);
//                Vec3d transformedPosition = waypointPosition.subtract(camera.getPos());
//
//                matrixStack.push();
//                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
//                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
//                matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
//
//                Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
//                Tessellator tessellator = Tessellator.getInstance();
//                BufferBuilder buffer = tessellator.getBuffer();
//
//                float size = (float) distance / 10;
//                buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
//                buffer.vertex(positionMatrix, -size, size, 0).color(1f, 1f, 1f, 1f).texture(0f, 0f).next();
//                buffer.vertex(positionMatrix, -size, -size, 0).color(1f, 0f, 0f, 1f).texture(0f, 1f).next();
//                buffer.vertex(positionMatrix, size, -size, 0).color(0f, 1f, 0f, 1f).texture(1f, 1f).next();
//                buffer.vertex(positionMatrix, size, size, 0).color(0f, 0f, 1f, 1f).texture(1f, 0f).next();
//
//                RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
//                RenderSystem.setShaderTexture(0, new Identifier("eggsentials", "textures/item/prehistoric_egg.png"));
//                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
//                RenderSystem.disableCull();
//                RenderSystem.depthFunc(GL11.GL_ALWAYS);
//
//                tessellator.draw();
//
//                RenderSystem.depthFunc(GL11.GL_LEQUAL);
//                RenderSystem.enableCull();
//
//                matrixStack.pop();
//
////                String distanceText = String.format("%.2f blocks", distance);
////                MinecraftClient.getInstance().textRenderer.draw(distanceText, 0, 0, 0xFFFFFF);
//            }
//        }
//    }

    public void registerOverlays() {
        utils = (Utils) EnvironmentCore.utils;
        HudRenderCallback.EVENT.register((obj1, obj2) -> utils.renderOverlays(obj1, obj2));
    }

    @Override
    public void registerUseClick() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
                    BBsentials.executionService.execute(() -> {
                        onArmorstandInteraction(player, world, hand, entity, hitResult);
                    });
                    return ActionResult.PASS;
                }
        );
    }

    public void onArmorstandInteraction(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if (entity instanceof ArmorStandEntity) {
            entity.getArmorItems().forEach(itemStack -> {
                if (itemStack.getItem() == Items.PLAYER_HEAD) {
                    String texture = itemStack.getNbt().getCompound("SkullOwner").getCompound("Properties").getList("textures", NbtElement.COMPOUND_TYPE).getCompound(0).getString("Value");
                    ClickableArmorStand armorStand = ClickableArmorStand.getFromTexture(texture);
                    if (armorStand != null)
                        Chat.sendPrivateMessageToSelfSuccess(armorStand.toString() + " was clicked");
                    //TODO add code is here
                    BlockPos pos = entity.getBlockPos();
                    BBsentials.addEggToIsland(EnvironmentCore.utils.getCurrentIsland(), armorStand, new Position(pos.getX(), pos.getY(), pos.getZ()));
                }
            });
        }
    }

    @Override
    public void registerWaypoints() {
//        WorldRenderEvents.END.register(MCEvents::renderWaypoints);
    }

}
