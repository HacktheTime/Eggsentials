package de.hype.eggsentials.shared.packets.network;

import de.hype.eggsentials.client.common.client.updatelisteners.EggType;
import de.hype.eggsentials.shared.constants.Islands;
import de.hype.eggsentials.shared.objects.Position;

import java.util.Map;

public class WelcomeEggPacket {
    public final boolean success;
    public final Map<Islands, Map<EggType, Position>> islandEggMap;

    public WelcomeEggPacket(boolean success, Map<Islands, Map<EggType, Position>> islandEggMap) {
        this.success = success;
        this.islandEggMap = islandEggMap;
    }
}
