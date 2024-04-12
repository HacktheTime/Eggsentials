package de.hype.eggsentials.shared.objects;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public enum EggType {
    MORNING(Color.GREEN, new RenderInformation(null, null)),
    LUNCH(Color.BLUE, new RenderInformation(null, null)),
    EVENING(Color.ORANGE, new RenderInformation(null, null)),
    FAIRY_SOUL(Color.PINK, new RenderInformation(null, null));

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
