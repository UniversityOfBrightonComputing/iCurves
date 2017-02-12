package icurves.concrete;

import icurves.CurvesApp;
import icurves.description.AbstractBasicRegion;
import icurves.description.AbstractCurve;
import icurves.diagram.Curve;
import icurves.diagram.DiagramCreator;
import icurves.guifx.SettingsController;
import icurves.util.Polylabel;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Concrete form of AbstractBasicRegion.
 */
public class ConcreteZone {

    /**
     * The abstract basic region of this concrete zone.
     */
    private final AbstractBasicRegion zone;

    /**
     * Contours within this zone.
     */
    private final List<Curve> containingCurves;

    /**
     * Contours outside of this zone.
     */
    private final List<Curve> excludingCurves;

    public Shape bbox = null;

    /**
     * Constructs a concrete zone from abstract zone given containing and excluding contours.
     *
     * @param zone abstract zone
     * @param containingCurves containing contours
     * @param excludingCurves   excluding contours
     */
    public ConcreteZone(AbstractBasicRegion zone, List<Curve> containingCurves, List<Curve> excludingCurves) {
        this.zone = zone;
        this.containingCurves = containingCurves;
        this.excludingCurves = excludingCurves;

        // TODO: make global bbox
        bbox = new Rectangle(10000.0, 10000.0);
        bbox.setTranslateX(-3000);
        bbox.setTranslateY(-3000);
    }

    public ConcreteZone(AbstractBasicRegion zone, Map<AbstractCurve, Curve> curveToContour) {
        this.zone = zone;

        containingCurves = new ArrayList<>();
        excludingCurves = new ArrayList<>(curveToContour.values());

        for (AbstractCurve curve : zone.getInSet()) {
            Curve contour = curveToContour.get(curve);

            excludingCurves.remove(contour);
            containingCurves.add(contour);
        }

        // TODO: make global bbox
        bbox = new Rectangle(10000.0, 10000.0);
        bbox.setTranslateX(-3000);
        bbox.setTranslateY(-3000);
    }

    public AbstractBasicRegion getAbstractZone() {
        return zone;
    }

    /**
     * @return contours within this zone
     */
    public List<Curve> getContainingCurves() {
        return containingCurves;
    }

    /**
     * @return contours outside of this zone
     */
    public List<Curve> getExcludingCurves() {
        return excludingCurves;
    }

    public Shape getShape() {
        Shape shape = bbox;

        for (Curve curve : getContainingCurves()) {
            shape = Shape.intersect(shape, curve.getShape());
        }

        for (Curve curve : getExcludingCurves()) {
            shape = Shape.subtract(shape, curve.getShape());
        }

        return shape;
    }

    public boolean intersects(Shape shape) {
        return !Shape.intersect(getShape(), shape).getLayoutBounds().isEmpty();
    }

    public boolean intersects(ConcreteZone other) {
        return intersects(other.getShape());
    }

    private Point2D center = null;

    /**
     * @return center point of this concrete zone in 2D space
     */
    public Point2D getCenter() {
        if (center == null) {
            center = computeCenter();
        }

        return center;
    }

    /**
     * Scans the zone using a smaller radius circle each time
     * until the circle is completely within the zone.
     * Once found, returns the center of that circle.
     *
     * @return zone center
     */
    private Point2D computeCenter() {
        return computeVisualCentre();
    }

    private Point2D computeVisualCentre() {
        if (polygonShape == null)
            polygonShape = getPolygonShape();

        return Polylabel.findCenter(polygonShape);
    }

    private Polygon2D polygonShape = null;

    public Polygon2D getPolygonShape() {
        polygonShape = new SimplePolygon2D(new math.geom2d.Point2D(0, 0),
                new math.geom2d.Point2D(10000, 0),
                new math.geom2d.Point2D(10000, 10000),
                new math.geom2d.Point2D(0, 10000));

        containingCurves.stream().map(c -> c.getPolygon()).forEach(p -> {
            polygonShape = Polygons2D.intersection(polygonShape, p);
        });

        excludingCurves.stream().map(c -> c.getPolygon()).forEach(p -> {
            polygonShape = Polygons2D.difference(polygonShape, p);
        });

        return polygonShape;
    }

    public boolean isTopologicallyAdjacent(ConcreteZone other) {
        if (zone.getStraddledContour(other.zone).isPresent()) {
            Shape shape2 = other.getShape();

            shape2.setTranslateX(shape2.getTranslateX() - 5);

            if (intersects(shape2)) {
                // put it back
                shape2.setTranslateX(shape2.getTranslateX() + 5);
                return true;
            }

            shape2.setTranslateX(shape2.getTranslateX() + 10);

            if (intersects(shape2)) {
                shape2.setTranslateX(shape2.getTranslateX() - 5);
                return true;
            }

            shape2.setTranslateX(shape2.getTranslateX() - 5);
            shape2.setTranslateY(shape2.getTranslateY() - 5);

            if (intersects(shape2)) {
                shape2.setTranslateY(shape2.getTranslateY() + 5);
                return true;
            }

            shape2.setTranslateY(shape2.getTranslateY() + 10);

            if (intersects(shape2)) {
                shape2.setTranslateY(shape2.getTranslateY() - 5);
                return true;
            }
        }

        return false;
    }

    public String toDebugString() {
        return "ConcreteZone:[zone=" + zone + "\n"
                + "containing: " + containingCurves.toString() + "\n"
                + "excluding:  " + excludingCurves.toString() + "]";
    }

    @Override
    public String toString() {
        return zone.toString();
    }
}
