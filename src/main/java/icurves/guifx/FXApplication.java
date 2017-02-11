package icurves.guifx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class FXApplication extends Application {

    private static FXApplication instance;

    public static FXApplication getInstance() {
        return instance;
    }

    @Override
    public void start(Stage stage) throws Exception {
        instance = this;

        Parent root = FXMLLoader.load(getClass().getResource("ui_main.fxml"));

        stage.setScene(new Scene(root));
        stage.setTitle("iCirclesFX");
        stage.show();
    }

    private SettingsController settings;

    public SettingsController getSettings() {
        return settings;
    }

    // we'll settle for this hack for now
    public void setSettings(SettingsController settings) {
        this.settings = settings;
    }
}
