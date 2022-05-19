/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa;
//Test
import com.pagosoft.plaf.PgsLookAndFeel;
import java.util.Locale;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.logging.log4j.*;
import monalisa.gui.MainDialog;

/**
 * Application entry point class for MonaLisa.
 *
 * @author Konrad Rudolph
 */
public final class MonaLisa implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(MonaLisa.class);
    private static final String OS_NAME = System.getProperty("os.name");
    public static final boolean IS_MACOSX = OS_NAME.toLowerCase(Locale.ENGLISH).startsWith("mac os x");
    public static final String APPLICATION_TITLE = "MonaLisa";

    private static MainDialog applicationMainWindow;

    /**
     * Returns a reference to the application's main window.
     */
    public static MainDialog appMainWindow() {
        return applicationMainWindow;
    }

    private MonaLisa() {
    }

    /**
     * Application entry point
     *
     * @param args
     * @throws UnsupportedLookAndFeelException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        LOGGER.info("Logging Start");
        try {
            if (IS_MACOSX) {
                // Set system properties for correct styling under OS X.
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", APPLICATION_TITLE);
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }

            UIManager.setLookAndFeel(new PgsLookAndFeel());

            SwingUtilities.invokeLater(new MonaLisa());

            // First start of MonaLisa? Then write the config file for the settings
            if (!Settings.load()) {
                LOGGER.warn("No config file found. Creating new config file.");
                Settings.createDefaultConfigFile();
            }
            ToolTipManager.sharedInstance().setDismissDelay(12000);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            LOGGER.error("Error during startup", e.getMessage());
        }

    }

    @Override
    public void run() {
        MonaLisa.applicationMainWindow = new MainDialog();
    }
}
