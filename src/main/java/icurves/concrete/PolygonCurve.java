package icurves.concrete;

import icurves.description.AbstractCurve;
import icurves.diagram.Curve;
import icurves.util.Converter;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import math.geom2d.polygon.Polygon2D;

import java.util.List;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class PolygonCurve extends Curve {

    private Polygon polygonFX;

    public PolygonCurve(AbstractCurve curve, List<Point2D> points) {
        super(curve);

        polygonFX = Converter.toPolygonFX(points);
    }

    @Override
    public Shape computeShape() {
        Rectangle bbox = new Rectangle(10000, 10000);
        bbox.setTranslateX(-3000);
        bbox.setTranslateY(-3000);

        Shape shape = Shape.intersect(bbox, polygonFX);
        shape.setFill(Color.TRANSPARENT);
        shape.setStroke(Color.DARKBLUE);
        shape.setStrokeWidth(2);

        return shape;
    }

    @Override
    public Polygon2D computePolygon() {
        return Converter.toPolygon2D(polygonFX);
    }
}
