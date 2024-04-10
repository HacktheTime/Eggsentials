package de.hype.eggsentials.shared.packets.network;

import de.hype.eggsentials.client.common.client.updatelisteners.EggType;
import de.hype.eggsentials.shared.constants.Islands;
import de.hype.eggsentials.shared.objects.Position;

public class EggFoundPacket {
    public final String finder;
    public final Islands island;
    public final Position coords;
    public final EggType type;

    public EggFoundPacket(String finder, Islands island, Position coords, EggType type) {
        this.finder = finder;
        this.island = island;
        this.coords = coords;
        this.type = type;
    }
}
