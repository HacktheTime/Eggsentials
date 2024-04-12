package de.hype.eggsentials.fabric;

import com.mojang.brigadier.CommandDispatcher;
import de.hype.eggsentials.client.common.client.Eggsentials;
import de.hype.eggsentials.client.common.mclibraries.MCCommand;
import de.hype.eggsentials.environment.packetconfig.AbstractPacket;
import de.hype.eggsentials.shared.packets.network.InternalCommandPacket;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.event.Event;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Commands implements MCCommand {
    Event<ClientCommandRegistrationCallback> event = ClientCommandRegistrationCallback.EVENT;

    private static void simpleCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, String commandName, String[] parameters) {
        dispatcher.register(
                literal(commandName)
                        .executes((context) -> {
                            sendPacket(new InternalCommandPacket(commandName, parameters));
                            return 1;
                        })
        );
    }

    public static <T extends AbstractPacket> void sendPacket(T packet) {
        Eggsentials.connection.sendPacket(packet);
    }

    public void registerMain() {

    }
}
