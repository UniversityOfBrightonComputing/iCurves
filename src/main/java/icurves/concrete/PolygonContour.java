package icurves.concrete;

import icurves.abstractdescription.AbstractCurve;
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
public class PolygonContour extends Contour {

    private Polygon polygonFX;

    public PolygonContour(AbstractCurve curve, List<Point2D> points) {
        super(curve);

        polygonFX = Converter.toPolygonFX(points);
    }

    @Override
    public Shape getShape() {
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
    public Polygon2D toPolygon() {
        return Converter.toPolygon2D(polygonFX);
    }
}
