package de.hype.eggsentials.forge;

import de.hype.eggsentials.client.common.chat.Chat;
import de.hype.eggsentials.client.common.client.Eggsentials;
import de.hype.eggsentials.client.common.mclibraries.EnvironmentCore;
import de.hype.eggsentials.client.common.mclibraries.MCCommand;
import de.hype.eggsentials.environment.packetconfig.AbstractPacket;
import de.hype.eggsentials.forge.CommandImplementations.*;
import de.hype.eggsentials.shared.objects.SplashData;
import de.hype.eggsentials.shared.packets.function.SplashNotifyPacket;
import net.minecraftforge.client.ClientCommandHandler;

public class Commands implements MCCommand {
    public static <T extends AbstractPacket> void sendPacket(T packet) {
        Eggsentials.connection.sendPacket(packet);
    }

    public void registerMain() {
        ClientCommandHandler.instance.registerCommand(new CommandBBI());
        ClientCommandHandler.instance.registerCommand(new CommandGoblinRaid());
        ClientCommandHandler.instance.registerCommand(new Command2xPowder());
        ClientCommandHandler.instance.registerCommand(new CommandBetterTogether());
        ClientCommandHandler.instance.registerCommand(new CommandRaffle());
        ClientCommandHandler.instance.registerCommand(new CommandGoneWithTheWind());
        ClientCommandHandler.instance.registerCommand(new CommandChChest());
        ClientCommandHandler.instance.registerCommand(new CommandBC());
        ClientCommandHandler.instance.registerCommand(new CommandBingoChat());
        ClientCommandHandler.instance.registerCommand(new CommandOpenConfig());
    }

    public void registerRoleRequired(boolean hasDev, boolean hasAdmin, boolean hasMod, boolean hasSplasher, boolean hasBeta, boolean hasMiningEvents, boolean hasChChest) {
        if (hasMod) {
            ClientCommandHandler.instance.registerCommand(new CommandBAnnounce());
//            ClientCommandHandler.instance.registerCommand(new CommandBMute());
//            ClientCommandHandler.instance.registerCommand(new CommandBBan());
        }
        if (hasSplasher) {
            ClientCommandHandler.instance.registerCommand(new CommandSplashAnnounce());
            ClientCommandHandler.instance.registerCommand(new CommandGetLeechers());
        }
    }

    public void splashAnnounce(int hubNumber, String locationInHub, String extramessage, boolean lessWaste) {
        try {
            sendPacket(new SplashNotifyPacket(new SplashData(Eggsentials.generalConfig.getUsername(), hubNumber, locationInHub, EnvironmentCore.utils.getCurrentIsland(), extramessage, lessWaste)));
        } catch (Exception e) {
            Chat.sendPrivateMessageToSelfError(e.getMessage());
        }
    }
}
