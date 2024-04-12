package de.hype.eggsentials.client.common.client;

import de.hype.eggsentials.client.common.chat.Message;
import de.hype.eggsentials.client.common.objects.Waypoints;
import de.hype.eggsentials.shared.constants.Islands;
import de.hype.eggsentials.shared.objects.EggType;
import de.hype.eggsentials.shared.objects.Position;
import de.hype.eggsentials.shared.objects.WaypointData;

import java.util.HashMap;

public class EggWaypoint extends Waypoints {
    public final EggType type;
    public final Islands island;
    public final Position pos;
    private boolean found;


    public EggWaypoint(EggType type, Islands island, Position pos, Boolean found) {
        super(new WaypointData(pos, Message.of(type.toString()).getJson(), 1000, !found, true, type.getRenderInformation(), type.getColor(), false));
        this.type = type;
        this.found = found;
        this.island = island;
        this.pos = pos;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EggWaypoint)) return false;
        EggWaypoint point = ((EggWaypoint) obj);
        return point.pos.equals(pos) && point.type.equals(type);
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
        this.visible = !found;
    }

    /**
     * Puts itself at the right place in the map
     */
    public void register() {
        BBsentials.islandEggMap.getOrDefault(island, new HashMap<>()).put(EggType.FAIRY_SOUL, this);
    }
}
