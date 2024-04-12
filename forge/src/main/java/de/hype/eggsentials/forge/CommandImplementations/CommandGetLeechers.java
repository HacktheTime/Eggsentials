package de.hype.eggsentials.forge.CommandImplementations;

import de.hype.eggsentials.client.common.chat.Chat;
import de.hype.eggsentials.client.common.client.Eggsentials;
import de.hype.eggsentials.client.common.mclibraries.EnvironmentCore;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import java.util.concurrent.TimeUnit;

public class CommandGetLeechers extends CommandBase {

    @Override
    public String getCommandName() {
        return "getLeechers";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/getLeechers";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        UpdateListenerManager.splashStatusUpdateListener.showOverlay = true;
        Chat.sendPrivateMessageToSelfDebug("Leechers: " + String.join(", ", EnvironmentCore.utils.getSplashLeechingPlayers()));
        Eggsentials.executionService.schedule(() -> UpdateListenerManager.splashStatusUpdateListener.showOverlay = false, 2, TimeUnit.MINUTES);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
