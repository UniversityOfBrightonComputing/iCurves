package icurves.concrete;

import icurves.description.AbstractCurve;
import javafx.scene.shape.Shape;
import math.geom2d.polygon.Polygon2D;

/**
 * A concrete contour.
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public abstract class Contour {

    /**
     * Abstract representation of this concrete contour.
     */
    private final AbstractCurve curve;

    public Contour(AbstractCurve curve) {
        this.curve = curve;
    }

    /**
     * @return abstract curve associated with this contour
     */
    public final AbstractCurve getCurve() {
        return curve;
    }

    public abstract Shape getShape();

    public Polygon2D toPolygon() {
        return null;
    }

    @Override
    public String toString() {
        return curve.toString();
    }
}
