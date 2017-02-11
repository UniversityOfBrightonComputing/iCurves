package icurves.concrete;

import icurves.description.AbstractBasicRegion;
import icurves.description.AbstractCurve;
import icurves.description.Description;
import icurves.diagram.Curve;
import icurves.geometry.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a diagram at the concrete level.
 * Technically, this is a concrete form of Description.
 */
public class ConcreteDiagram {

    private static final Logger log = LogManager.getLogger(ConcreteDiagram.class);

    private final Rectangle box;
    private final List<CircleCurve> circles;
    private final List<PathCurve> contours;
    private final List<ConcreteZone> shadedZones, allZones;

    private final Description original, actual;
    private final Map<AbstractCurve, Curve> curveToContour;

    //public final List<Shape> shapes = new ArrayList<>();

    ConcreteDiagram(Description original, Description actual,
                    List<CircleCurve> circles,
                    Map<AbstractCurve, Curve> curveToContour, int size, PathCurve... contours) {
        this.original = original;
        this.actual = actual;
        this.box = new Rectangle(0, 0, size, size);
        this.curveToContour = curveToContour;
        this.circles = circles;
        this.contours = Arrays.asList(contours);

        setSize(size);

        log.info("Initial diagram: " + original);
        log.info("Final diagram  : " + actual);

        this.shadedZones = createShadedZones();
        this.allZones = actual.getZones()
                .stream()
                .map(this::makeConcreteZone)
                .collect(Collectors.toList());

        log.info("Concrete zones : " + allZones);
    }

    /**
     * Creates shaded (extra) zones based on the difference
     * between the initial diagram and final diagram.
     * In other words, finds which zones in final diagram were not in initial diagram.
     *
     * @return list of shaded zones
     */
    private List<ConcreteZone> createShadedZones() {
        List<ConcreteZone> result = actual.getZones()
                .stream()
                .filter(zone -> !original.includesZone(zone))
                .map(this::makeConcreteZone)
                .collect(Collectors.toList());

        log.info("Extra zones: " + result);

        return result;
    }

    /**
     * Creates a concrete zone out of an abstract zone.
     *
     * @param zone the abstract zone
     * @return the concrete zone
     */
    private ConcreteZone makeConcreteZone(AbstractBasicRegion zone) {
        List<Curve> includingCircles = new ArrayList<>();
        List<Curve> excludingCircles = new ArrayList<>(circles);
        excludingCircles.addAll(contours);

        for (AbstractCurve curve : zone.getInSet()) {
            Curve contour = curveToContour.get(curve);

            excludingCircles.remove(contour);
            includingCircles.add(contour);
        }

        ConcreteZone cz = new ConcreteZone(zone, includingCircles, excludingCircles);
        cz.bbox = new javafx.scene.shape.Rectangle(box.getWidth(), box.getHeight());

        return cz;
    }

    /**
     * @return bounding box of the whole diagram
     */
    public Rectangle getBoundingBox() {
        return box;
    }

    public Map<AbstractCurve, Curve> getCurveToContour() {
        return curveToContour;
    }

    /**
     * @return diagram circle contours
     */
    public List<CircleCurve> getCircles() {
        return circles;
    }

    /**
     * @return diagram arbitrary shape contours
     */
    public List<PathCurve> getContours() {
        return contours;
    }

    public List<Curve> getAllContours() {
        List<Curve> contoursAll = new ArrayList<>();
        contoursAll.addAll(circles);
        contoursAll.addAll(contours);
        return contoursAll;
    }

    /**
     * @return extra zones
     */
    public List<ConcreteZone> getShadedZones() {
        return shadedZones;
    }

    /**
     * Returns original abstract description, i.e. the one that was requested.
     *
     * @return original abstract description
     */
    public Description getOriginalDescription() {
        return original;
    }

    /**
     * Returns actual abstract description, i.e. the one that was generated.
     *
     * @return actual abstract description
     */
    public Description getActualDescription() {
        return actual;
    }

    /**
     * @return all zones this concrete diagram has
     */
    public List<ConcreteZone> getAllZones() {
        return allZones;
    }

    public List<ConcreteZone> getNormalZones() {
        List<ConcreteZone> zones = new ArrayList<>(allZones);
        zones.removeAll(shadedZones);
        return zones;
    }

    public ConcreteZone getOutsideZone() {
        return allZones.stream()
                .filter(z -> z.getAbstractZone() == AbstractBasicRegion.OUTSIDE)
                .findAny()
                .get();
    }

    public void setSize(int size) {
        // work out a suitable size
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (CircleCurve cc : circles) {
            if (cc.getMinX() < minX) {
                minX = cc.getMinX();
            }
            if (cc.getMinY() < minY) {
                minY = cc.getMinY();
            }
            if (cc.getMaxX() > maxX) {
                maxX = cc.getMaxX();
            }
            if (cc.getMaxY() > maxY) {
                maxY = cc.getMaxY();
            }
        }

        double midX = (minX + maxX) * 0.5;
        double midY = (minY + maxY) * 0.5;
        for (CircleCurve cc : circles) {
            cc.shift(-midX, -midY);
        }

        double width = maxX - minX;
        double height = maxY - minY;
        double biggest_HW = Math.max(height, width);
        double scale = (size * 0.95) / biggest_HW;
        for (CircleCurve cc : circles) {
            cc.scaleAboutZero(scale);
        }

        for (CircleCurve cc : circles) {
            cc.shift(size * 0.5, size * 0.5);
        }

        // TODO: also scale path contours
    }

    /**
     * Returns zones in the drawn diagram that contain the given contour.
     *
     * @param contour the contour
     * @return zones containing contour
     */
    public List<ConcreteZone> getZonesContainingContour(CircleCurve contour) {
        return allZones.stream()
                .filter(zone -> zone.getContainingCurves().contains(contour))
                .collect(Collectors.toList());
    }

    public String toDebugString() {
        return "ConcreteDiagram[box=" + box + "\n"
                + "contours: " + circles + "\n"
                + "shaded zones: " + shadedZones + "]";
    }

    @Override
    public String toString() {
        return toDebugString();
    }
}
