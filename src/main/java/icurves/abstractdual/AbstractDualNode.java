package icurves.abstractdual;

import icurves.description.AbstractBasicRegion;

public class AbstractDualNode {

    private final AbstractBasicRegion zone;

    AbstractDualNode(AbstractBasicRegion zone) {
        this.zone = zone;
    }

    public AbstractBasicRegion getZone() {
        return zone;
    }

    @Override
    public String toString() {
        return "Node[" + zone.toString() + "]";
    }
}
