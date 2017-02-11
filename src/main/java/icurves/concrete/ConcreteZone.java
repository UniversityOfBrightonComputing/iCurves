package icurves.concrete;

import icurves.CurvesApp;
import icurves.description.AbstractBasicRegion;
import icurves.description.AbstractCurve;
import icurves.diagram.Curve;
import icurves.diagram.DiagramCreator;
import icurves.guifx.SettingsController;
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

    private static final Logger log = LogManager.getLogger(ConcreteZone.class);

    private static final double RADIUS_STEP = DiagramCreator.BASE_CURVE_RADIUS / 20;
    private static final int SCAN_STEP = (int) RADIUS_STEP / 4;

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

    private SettingsController settings;

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

        settings = CurvesApp.getInstance().getSettings();

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

        settings = CurvesApp.getInstance().getSettings();

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

    // TODO: cache shape
    //private Shape shape = null;
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

    // Examples:

    // b h ab ac bd bj cf de fh hi hk hq ik abc
    // b h l m s ab ac ar bc bd bp cl cn cq cx de hk hq ik mn rz abc bfg

    // Finding zones:

    // a b c d ab ac ad bc abc abd bcd abcd

    /**
     * Scans the zone using a smaller radius circle each time
     * until the circle is completely within the zone.
     * Once found, returns the center of that circle.
     *
     * @return zone center
     */
    private Point2D computeCenter() {
        Point2D centroid = computeCentroid();
        if (centroid != Point2D.ZERO) {
            System.out.println("Computed centroid: " + centroid);
            return centroid;
        }




        //Profiler.INSTANCE.start("Computing center: " + zone);

        Shape shape = getShape();
        shape.setFill(Color.RED);

        double minX = shape.getLayoutBounds().getMinX();
        double minY = shape.getLayoutBounds().getMinY();
        double width = shape.getLayoutBounds().getWidth();
        double height = shape.getLayoutBounds().getHeight();

        // radius is width/height * 0.5
        // we use 0.45 for heuristics
        int radius = (int) ((width < height ? width : height) * 0.45);

        // limit max radius
        if (radius > DiagramCreator.BASE_CURVE_RADIUS) {
            radius = (int) DiagramCreator.BASE_CURVE_RADIUS;
        }

        int scanStep = SCAN_STEP;

        // TODO: we might be better off by computing the approx area
        // of the part that is outside of this, if large -> then skip the whole line?

        while (true) {
            if (settings.useCircleApproxCenter()) {
                Circle circle = new Circle(radius, radius, radius);
                circle.setStroke(Color.BLACK);

                for (int y = (int) minY + radius; y < minY + height - radius; y += scanStep) {
                    circle.setCenterY(y);

                    for (int x = (int) minX + radius; x < minX + width - radius; x += scanStep) {
                        circle.setCenterX(x);

                        // if circle is completely enclosed by this zone
                        if (Shape.subtract(circle, shape).getLayoutBounds().isEmpty()) {

                            //Profiler.INSTANCE.end("Computing center: " + zone);

                            return new Point2D(x, y);
                        }
                    }
                }
            } else {

                if (settings.isParallel()) {
                    final int scanForThisStep = scanStep;
                    final int radiusForThisStep = radius;

                    Optional<Point2D> maybeCenter = IntStream.range(0, (int)((minY + height - radiusForThisStep*2) / scanForThisStep))
                            .parallel()
                            .mapToObj(dy -> {
                                int y = (int)minY + dy * scanForThisStep;

                                Rectangle rect = new Rectangle(radiusForThisStep*2, radiusForThisStep*2);
                                rect.setStroke(Color.BLACK);
                                rect.setY(y);

                                for (int x = (int) minX; x < minX + width - radiusForThisStep*2; x += scanForThisStep) {
                                    rect.setX(x);

                                    // if square is completely enclosed by this zone
                                    if (Shape.subtract(rect, shape).getLayoutBounds().isEmpty()) {

                                        return new Point2D(x + radiusForThisStep, y + radiusForThisStep);
                                    }
                                }

                                return Point2D.ZERO;
                            })
                            .filter(p -> p != Point2D.ZERO)
                            .findAny();


                    if (maybeCenter.isPresent()) {
                        return maybeCenter.get();
                    }
                } else {

                    Rectangle rect = new Rectangle(radius*2, radius*2);
                    rect.setStroke(Color.BLACK);

                    for (int y = (int) minY; y < minY + height - radius*2; y += scanStep) {
                        rect.setY(y);

                        for (int x = (int) minX; x < minX + width - radius*2; x += scanStep) {
                            rect.setX(x);

                            // if square is completely enclosed by this zone
                            if (Shape.subtract(rect, shape).getLayoutBounds().isEmpty()) {

                                System.out.println("Computed visual: " + new Point2D(x + radius, y + radius));
                                //Profiler.INSTANCE.end("Computing center: " + zone);

                                return new Point2D(x + radius, y + radius);
                            }
                        }
                    }
                }

            }

            if (radius <= RADIUS_STEP) {
                radius -= 5;

                if (scanStep > 10) {
                    scanStep -= 5;
                }

                //System.out.println("Checking:" + radius + " scan step: " + scanStep);

            } else {
                radius -= RADIUS_STEP;
            }

            if (radius <= 0) {
                throw new RuntimeException("Cannot find zone center: " + zone);
            }
        }
    }

    private Polygon2D polygonShape = null;

    private Point2D computeCentroid() {
        if (polygonShape == null)
            polygonShape = getPolygonShape();

        math.geom2d.Point2D centroid = polygonShape.centroid();

        try {
            if (polygonShape.contains(centroid) && !Double.isNaN(centroid.x()) && !Double.isNaN(centroid.y())) {
                return new Point2D(centroid.x(), centroid.y());
            } else {
                return Point2D.ZERO;
            }
        } catch (Exception e) {
            return Point2D.ZERO;
        }
    }

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
