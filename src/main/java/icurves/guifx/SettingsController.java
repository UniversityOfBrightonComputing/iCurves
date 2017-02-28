package icurves.guifx;

import icurves.decomposition.DecompositionStrategyType;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.shape.Rectangle;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.SimplePolygon2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class SettingsController {

    public Map<Object, Object> globalMap = new HashMap<>();

    public List<Point2D> debugPoints = new ArrayList<>();

    public Polygon2D geomBBox = new SimplePolygon2D(new math.geom2d.Point2D(-10000.0, -10000.0),
            new math.geom2d.Point2D(10000.0, -10000.0),
            new math.geom2d.Point2D(10000.0, 10000.0),
            new math.geom2d.Point2D(-10000.0, 10000.0));

    public Rectangle fxBBox = createBBox();

    private Rectangle createBBox() {
        Rectangle r = new Rectangle(20000, 20000);
        r.setTranslateX(-10000);
        r.setTranslateY(-10000);
        return r;
    }

    @FXML
    private CheckBox cbParallel;

    public boolean isParallel() {
        return cbParallel.isSelected();
    }

    @FXML
    private TextField fieldCurveRadius;

    public double getCurveRadius() {
        return Double.parseDouble(fieldCurveRadius.getText());
    }

    @FXML
    private CheckBox cbSmooth;

    public boolean useSmooth() {
        return cbSmooth.isSelected();
    }

    @FXML
    private TextField fieldSmoothFactor;

    public int getSmoothFactor() {
        return Integer.parseInt(fieldSmoothFactor.getText());
    }

    @FXML
    private TextField fieldMEDSize;

    public double getMEDSize() {
        return Double.parseDouble(fieldMEDSize.getText());
    }

    @FXML
    private CheckBox cbShowMED;

    public boolean showMED() {
        return cbShowMED.isSelected();
    }

    // TODO: hardcoded
    public DecompositionStrategyType getDecompType() {
        return DecompositionStrategyType.INNERMOST;
    }
}
