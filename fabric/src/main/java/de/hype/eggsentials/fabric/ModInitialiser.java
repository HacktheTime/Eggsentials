package de.hype.eggsentials.fabric;

import de.hype.eggsentials.client.common.client.Eggsentials;
import de.hype.eggsentials.client.common.client.objects.ServerSwitchTask;
import de.hype.eggsentials.client.common.mclibraries.EnvironmentCore;
import de.hype.eggsentials.fabric.numpad.NumPadCodes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import static de.hype.eggsentials.client.common.client.Eggsentials.generalConfig;

public class ModInitialiser implements ClientModInitializer {
    public static NumPadCodes codes;

    {
    } // KeyBinds

    @Override
    public void onInitializeClient() {
        System.out.println("Eggsentials : onInit called");
        EnvironmentCore core = EnvironmentCore.fabric(new Utils(), new MCEvents(), new FabricChat(), new Commands(), new Options(), new DebugThread());
        if (generalConfig.useNumCodes) codes = new NumPadCodes();
        Eggsentials.init();
        EnvironmentCore.mcevents.registerUseClick();
        if (generalConfig.hasBBRoles("dev")) {
            ServerSwitchTask.onServerJoinTask(() -> EnvironmentCore.debug.onServerJoin(), true);
            ServerSwitchTask.onServerLeaveTask(() -> EnvironmentCore.debug.onServerLeave(), true);
        }
        ClientPlayConnectionEvents.JOIN.register((a, b, c) -> {
            Eggsentials.onServerJoin();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((a, b) -> {
            Eggsentials.onServerLeave();
        });
    }
}
