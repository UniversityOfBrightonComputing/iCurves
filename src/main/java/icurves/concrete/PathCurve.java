package icurves.concrete;

import icurves.description.AbstractCurve;
import icurves.diagram.Curve;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import math.geom2d.polygon.Polygon2D;
import org.jetbrains.annotations.NotNull;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class PathCurve extends Curve {

    private final Path path;

    public PathCurve(AbstractCurve curve, Path path) {
        super(curve);
        this.path = path;
        path.getElements().addAll(new ClosePath());
        //path.setFillRule(FillRule.EVEN_ODD);
        path.setFill(Color.TRANSPARENT);
    }

    @Override
    public Shape computeShape() {
        Rectangle bbox = new Rectangle(10000, 10000);
        bbox.setTranslateX(-3000);
        bbox.setTranslateY(-3000);

        Shape shape = Shape.intersect(bbox, path);
        shape.setFill(Color.TRANSPARENT);
        shape.setStroke(Color.DARKBLUE);
        shape.setStrokeWidth(2);

        return shape;
    }

    @Override
    public Polygon2D computePolygon() {
        throw new UnsupportedOperationException();
    }
}
