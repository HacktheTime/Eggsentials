package de.hype.eggsentials.client.common.client.commands;

import de.hype.eggsentials.client.common.client.Eggsentials;
import de.hype.eggsentials.client.common.config.GeneralConfig;
import de.hype.eggsentials.client.common.mclibraries.EnvironmentCore;

public class Commands {
    public Commands() {
        EnvironmentCore.commands.registerMain();
        GeneralConfig configManager = Eggsentials.generalConfig;
        if (configManager.bbsentialsRoles != null) {

        }
    }
}