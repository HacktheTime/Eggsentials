package de.hype.eggsentials.shared.packets.network;


import de.hype.eggsentials.environment.packetconfig.AbstractPacket;
import de.hype.eggsentials.shared.constants.Islands;
import de.hype.eggsentials.shared.objects.EggType;
import de.hype.eggsentials.shared.objects.Position;

public class EggFoundPacket extends AbstractPacket {
    public String finder;
    public final Islands island;
    public final Position coords;
    public final EggType type;

    public EggFoundPacket(String finder, Islands island, Position coords, EggType type) {
        super(1, 1);
        this.finder = finder;
        this.island = island;
        this.coords = coords;
        this.type = type;
    }
}
