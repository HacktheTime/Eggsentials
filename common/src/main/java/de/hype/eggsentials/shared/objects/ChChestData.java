package de.hype.eggsentials.shared.objects;

import de.hype.eggsentials.shared.constants.ChChestItem;

public class ChChestData {
    public String finder;
    public Position coords;
    public ChChestItem[] items;

    public ChChestData(String finder, Position coords, ChChestItem[] items) {
        this.finder = finder;
        this.coords = coords;
        this.items = items;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChestLobbyData)) return false;
        return ((ChChestData) obj).coords.equals(coords);
    }
}
