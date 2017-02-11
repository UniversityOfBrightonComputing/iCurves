package icurves.abstractdual;

import icurves.abstractdescription.AbstractCurve;

public class AbstractDualEdge {

    public final AbstractDualNode from;
    public final AbstractDualNode to;
    public final AbstractCurve curve;

    public AbstractDualEdge(AbstractDualNode from, AbstractDualNode to, AbstractCurve curve) {
        this.from = from;
        this.to = to;
        this.curve = curve;
    }

    @Override
    public String toString() {
        return "Edge[curve=" + curve + "]";
    }
}
