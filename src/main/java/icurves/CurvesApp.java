package icurves;

import icurves.guifx.SettingsController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.core.config.Configurator;

public class CurvesApp extends Application {

    private static CurvesApp instance;

    public static CurvesApp getInstance() {
        return instance;
    }

    private SettingsController settings;

    public SettingsController getSettings() {
        return settings;
    }

    // we'll settle for this hack for now
    public void setSettings(SettingsController settings) {
        this.settings = settings;
    }

    @Override
    public void start(Stage stage) throws Exception {
        instance = this;

        Parent root = FXMLLoader.load(getClass().getResource("ui_main.fxml"));

        stage.setScene(new Scene(root));
        stage.setTitle("iCurves");
        stage.show();
    }

    public static void main(String args[]) {
        Configurator.initialize("default", CurvesApp.class.getResource("/icurves/log4j2.xml").toExternalForm());
        launch(args);
    }
}
