package icurves.concrete;

import icurves.abstractdescription.AbstractCurve;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class PathContour extends Contour {

    private final Path path;

    public PathContour(AbstractCurve curve, Path path) {
        super(curve);
        this.path = path;
        path.getElements().addAll(new ClosePath());
        //path.setFillRule(FillRule.EVEN_ODD);
        path.setFill(Color.TRANSPARENT);
    }

    @Override
    public Shape getShape() {
        Rectangle bbox = new Rectangle(10000, 10000);
        bbox.setTranslateX(-3000);
        bbox.setTranslateY(-3000);

        Shape shape = Shape.intersect(bbox, path);
        shape.setFill(Color.TRANSPARENT);
        shape.setStroke(Color.DARKBLUE);
        shape.setStrokeWidth(2);

        return shape;
    }
}
