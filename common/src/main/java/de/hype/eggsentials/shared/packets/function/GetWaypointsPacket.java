package de.hype.eggsentials.shared.packets.function;

import de.hype.eggsentials.environment.packetconfig.AbstractPacket;
import de.hype.eggsentials.shared.objects.ClientWaypointData;

import java.util.List;

/**
 * Used to tell the addon what message came in.
 */
public class GetWaypointsPacket extends AbstractPacket {
    public final List<ClientWaypointData> waypoints;

    public GetWaypointsPacket(List<ClientWaypointData> waypoints) {
        super(1, 1); //Min and Max supported Version
        this.waypoints = waypoints;
    }
}
