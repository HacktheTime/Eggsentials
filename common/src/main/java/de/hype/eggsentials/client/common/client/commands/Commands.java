package de.hype.eggsentials.client.common.client.commands;

import de.hype.eggsentials.client.common.client.BBsentials;
import de.hype.eggsentials.client.common.config.GeneralConfig;
import de.hype.eggsentials.client.common.mclibraries.EnvironmentCore;

public class Commands {
    public Commands() {
        EnvironmentCore.commands.registerMain();
        GeneralConfig configManager = BBsentials.generalConfig;
        if (configManager.bbsentialsRoles != null) {
            EnvironmentCore.commands.registerRoleRequired(
                    configManager.hasBBRoles("dev"),
                    configManager.hasBBRoles("admin"),
                    configManager.hasBBRoles("mod"),
                    configManager.hasBBRoles("splasher"),
                    configManager.hasBBRoles("beta"),
                    configManager.hasBBRoles("mining_events"),
                    configManager.hasBBRoles("mining_events")
                    );
        }
    }
}