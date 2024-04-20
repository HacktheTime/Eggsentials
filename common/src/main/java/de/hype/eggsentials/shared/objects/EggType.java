package de.hype.eggsentials.shared.objects;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public enum EggType {
    EVENING(Color.GREEN, new RenderInformation("bbsentials", "waypoints/evening_egg.png")),
    LUNCH(Color.BLUE, new RenderInformation("bbsentials", "waypoints/lunch_egg.png")),
    MORNING(Color.ORANGE, new RenderInformation("bbsentials", "waypoints/morning_egg.png")),
    FAIRY_SOUL(Color.PINK, new RenderInformation("bbsentials", "waypoints/fairysoul.png"));

    public Color color;
    public RenderInformation renderInf;

    EggType(Color color, RenderInformation renderInf) {
        this.color = color;
        this.renderInf = renderInf;
    }

    public Color getColor() {
        return color;
    }

    public List<RenderInformation> getRenderInformation() {
        List<RenderInformation> inf = new ArrayList<>();
        inf.add(renderInf);
        return inf;
    }
}
