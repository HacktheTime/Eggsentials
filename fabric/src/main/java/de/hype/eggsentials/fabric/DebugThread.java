package de.hype.eggsentials.fabric;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import de.hype.eggsentials.client.common.chat.Chat;
import de.hype.eggsentials.client.common.chat.Message;
import de.hype.eggsentials.client.common.client.EggWaypoint;
import de.hype.eggsentials.client.common.client.Eggsentials;
import de.hype.eggsentials.client.common.config.constants.ClickableArmorStand;
import de.hype.eggsentials.client.common.mclibraries.EnvironmentCore;
import de.hype.eggsentials.client.common.objects.Waypoints;
import de.hype.eggsentials.shared.constants.Islands;
import de.hype.eggsentials.shared.objects.EggType;
import de.hype.eggsentials.shared.objects.Position;
import de.hype.eggsentials.shared.objects.RenderInformation;
import de.hype.eggsentials.shared.objects.WaypointData;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DebugThread extends de.hype.eggsentials.client.common.client.DebugThread {
    public static volatile AtomicInteger timerSinceLastTick = new AtomicInteger(0);
    boolean doTest = false;
    MinecraftClient minecraft;

    public void unlockCursor() {
        MinecraftClient.getInstance().mouse.unlockCursor();
    }

    @Override
    public void loop() {
        if (doTest) {
            doTest = false;
            test();
        }
    }

    public void onNumpadCode() {
        setEggWaypointsFromEntities(getEggs());
//        Eggsentials.connectToBBserver();
//        init();
        return;
    }

    Collection<CommandNode<FabricClientCommandSource>> getClientCommands() {
        return ClientCommandManager.getActiveDispatcher().getRoot().getChildren();
    }


    public <T> void triggerRenderBreakpoint(List<T> data) {
    }

//    public void replaceCommand(String name) {
//        Collection<CommandNode<FabricClientCommandSource>> clientCommandChilds = getClientCommands();
//        Collection<CommandNode<CommandSource>> commandChilds = getServerCommands().getChildren();
//
//        Iterator<CommandNode<CommandSource>> iterator = commandChilds.iterator();
//        while (iterator.hasNext()) {
//            CommandNode<CommandSource> child = iterator.next();
//            if (child.getName().equals(name)) {
//                iterator.remove(); // Safely remove the element
//            }
//        }
//
//        ClientCommandManager.getActiveDispatcher().register(
//                ClientCommandManager.literal(name)
//                        .then(ClientCommandManager.argument("playernames", StringArgumentType.greedyString())
//                                .suggests((context, builder) -> {
//                                    // Provide tab-completion options for classes
//                                    List<String> playerNames = List.of("hi", "hi2", "hi3");
//                                    // Replace with your own logic to retrieve class names
//                                    return CommandSource.suggestMatching(playerNames, builder);
//                                })
//                                .executes((context -> {
//                                    Chat.sendPrivateMessageToSelfImportantInfo("Test Success: " + StringArgumentType.getString(context, "playernames"));
//                                    return 1;
//                                }))
//                        )
//        );
//    }

    RootCommandNode<CommandSource> getServerCommands() {
        return MinecraftClient.getInstance().getNetworkHandler().getCommandDispatcher().getRoot();
    }

    public void doOnce() {
        doTest = true;
    }

    @Override
    public List<String> test() {
        return List.of("");
    }

    public void setScreen(Screen screen) {
        if (screen == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> client.setScreen(screen));
    }

    @Override
    public void init() {
        super.init();
        ClientTickEvents.START_CLIENT_TICK.register(i -> timerSinceLastTick.set(0));
        ClientTickEvents.END_CLIENT_TICK.register(i -> timerSinceLastTick.set(0));
        Eggsentials.executionService.scheduleAtFixedRate(() -> {
            timerSinceLastTick.getAndAdd(1);
            if (timerSinceLastTick.get() > 20 && MinecraftClient.getInstance().currentScreen == null) {
                timerSinceLastTick.set(0);
//                    Chat.sendPrivateMessageToSelfInfo("Cursor unlocked");
                unlockCursor();
            }

        }, 100, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onServerJoin() {
        Chat.sendPrivateMessageToSelfError("Hub Join exec");
    }

    @Override
    public void onServerLeave() {
        Chat.sendPrivateMessageToSelfInfo(EnvironmentCore.utils.getServerId());
        Chat.sendPrivateMessageToSelfError("Hub Leave exec");
    }

    public List<Entity> getEggs(){
        List<Entity> possiblyEgg = new ArrayList<>();
        MinecraftClient.getInstance().getNetworkHandler().getWorld().getEntities().forEach(
                entity -> {
                    if (entity instanceof ArmorStandEntity){
                        for (ItemStack armorItem : ((ArmorStandEntity) entity).getArmorItems()) {
                            if (armorItem.getItem()== Items.PLAYER_HEAD){
//                                if (!armorItem.getNbt().toString().contains("eyJ0ZXh")){
                                    possiblyEgg.add(entity);
//                                }
                            }
                        }
                    }
                });
        return possiblyEgg;
    }

    public void setWaypointsFromEntities(List<Entity> entities){
        Waypoints.waypoints.clear();
        for (Entity entity : entities) {
            BlockPos pos = entity.getBlockPos();
            new Waypoints(new WaypointData(new Position(pos.getX(), pos.getY(), pos.getZ()), Message.of("ddd").getJson(), 1000, true, false, List.of(new RenderInformation(null, null)), false));
        }
    }

    public void setEggWaypointsFromEntities(List<Entity> entities){
        Waypoints.waypoints.clear();
        Islands current = EnvironmentCore.utils.getCurrentIsland();
        for (Entity entity : entities) {
            if (entity instanceof ArmorStandEntity) {
                entity.getArmorItems().forEach(itemStack -> {
                    if (itemStack.getItem() == Items.PLAYER_HEAD) {
                        String texture = itemStack.getNbt().getCompound("SkullOwner").getCompound("Properties").getList("textures", NbtElement.COMPOUND_TYPE).getCompound(0).getString("Value");
                        ClickableArmorStand armorStand = ClickableArmorStand.getFromTexture(texture);
                        if (armorStand != null)
                            Chat.sendPrivateMessageToSelfSuccess(armorStand.toString() + " was clicked");
                        //TODO add code is here
                        BlockPos blockPos = entity.getBlockPos();
                        Position pos = new Position(blockPos.getX(), blockPos.getY() + 2, blockPos.getZ());
                        EggWaypoint newPoint = new EggWaypoint(armorStand.getAsEgg(), current, pos, false);
                        Eggsentials.islandEggMap.getOrDefault(current, new HashMap<>()).put(EggType.FAIRY_SOUL, newPoint);
                    }
                });
            }
        }
    }
}
