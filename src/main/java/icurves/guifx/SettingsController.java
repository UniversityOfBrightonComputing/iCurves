package icurves.guifx;

import icurves.decomposition.DecompositionStrategyType;
import icurves.recomposition.RecompositionStrategyType;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class SettingsController {

    public Map<Object, Object> globalMap = new HashMap<>();

    @FXML
    private CheckBox cbTwoStep;

    public boolean isTwoStep() {
        return cbTwoStep.isSelected();
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

    @FXML
    private CheckBox cbUseCircleApprox;

    public boolean useCircleApproxCenter() {
        return cbUseCircleApprox.isSelected();
    }

    // TODO: hardcoded
    public DecompositionStrategyType getDecompType() {
        return DecompositionStrategyType.INNERMOST;
    }

    // TODO: hardcoded
    public RecompositionStrategyType getRecompType() {
        return RecompositionStrategyType.DOUBLY_PIERCED_EXTRA_ZONES;
    }
}
