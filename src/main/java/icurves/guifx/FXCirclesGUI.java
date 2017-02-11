package icurves.guifx;

import icurves.gui.CirclesGUI;
import javafx.application.Application;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class FXCirclesGUI implements CirclesGUI {
    @Override
    public void launchGUI(String[] args) {
        Application.launch(FXApplication.class, args);
    }
}
