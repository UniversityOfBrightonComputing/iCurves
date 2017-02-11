package icurves;

import icurves.gui.CirclesGUI;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Main entry class of iCircles.
 */
public class CurvesMain {

    private static Logger log;

    public static void main(String args[]) {
        Configurator.initialize("default", CurvesMain.class.getResource("/icurves/log4j2.xml").toExternalForm());

        log = LogManager.getLogger(CurvesMain.class);

        if (args.length == 0) {
            boolean guiOK = launchFXGUI(args);

            if (!guiOK) {
                log.warn("No GUI available");
            }
            return;
        }

        parse(args);
    }

    /**
     * Parses commands given to iCircles.
     *
     * @param args cmd line args
     */
    private static void parse(String[] args) {
        Options options = new Options();
        options.addOption("help", "Prints this help message");
        options.addOption("version", "Prints version");

        options.addOption(Option.builder("gui")
                .hasArg()
                .argName("GUI")
                .desc("Launches with specified GUI, where GUI can be one of the following: fx, none")
                .build());

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("help")) {
                usage(options);
                return;
            }

            execute(line);
        } catch (Exception e) {
            log.warn("Error during program execution: " + e.getMessage());
            usage(options);
        }
    }

    /**
     * Executes commands given to iCircles.
     *
     * @param line cmd line
     */
    private static void execute(CommandLine line) {
        if (line.hasOption("version")) {
            ResourceBundle resources = ResourceBundle.getBundle("icurves.lib", Locale.UK);
            log.info(resources.getString("version"));
            return;
        }

        if (line.hasOption("gui")) {
            String gui = line.getOptionValue("gui");
            switch (gui) {
                case "fx":
                    launchFXGUI(line.getArgs());
                    break;
                case "none":
                    break;
                default:
                    log.warn("Unknown GUI value: " + gui);
                    break;
            }
        }
    }

    /**
     * Prints help/usage message.
     *
     * @param options cmd line options
     */
    private static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar iCircles-x.y.jar", options);

        log.info("Typical usage: java -jar iCircles-x.y.jar -gui fx");
    }

    /**
     * Launch iCircles using JavaFX GUI.
     *
     * @param args cmd line args
     * @return true if successful
     */
    private static boolean launchFXGUI(String[] args) {
        log.info("Launching FX GUI...");
        try {
            Class.forName("javafx.application.Application");

            CirclesGUI gui = (CirclesGUI) Class.forName("icurves.guifx.FXCirclesGUI").newInstance();

            // note this is a blocking call
            gui.launchGUI(args);
            return true;
        } catch (Exception e) {
            log.error("Failed to launch FX GUI: " + e.getClass().getSimpleName() + ":" + e.getMessage());
            log.info("Try running with gui swing");
            return false;
        }
    }
}
