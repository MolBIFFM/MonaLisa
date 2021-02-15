/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.data.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import monalisa.addons.netviewer.NetViewer;
import monalisa.data.pn.PetriNet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jens Einloft
 */
public final class PetriNetOutputHandlers {

    private static final List<OutputHandler> handlers = new ArrayList<>();
    private static final Logger LOGGER = LogManager.getLogger(PetriNetOutputHandlers.class);
    private static NetViewer netViewer;
    static {
        handlers.add(new SbmlOutputHandler(3, 1));
        handlers.add(new SbmlOutputHandler(2, 4));
        handlers.add(new PntOutputHandler());
        handlers.add(new Pipe3OutputHandler());
        handlers.add(new Pipe4OutputHandler());
        handlers.add(new MetaToolOutputHandler());
        handlers.add(new ApnnOutputHandler());
        handlers.add(new PlainOutputHandler());
    }

    private PetriNetOutputHandlers() {
    }

    public static void save(File file, PetriNet pn) throws IOException {
        save(file, autoDetectHandler(file), pn, netViewer);
    }

    public static void save(File file, OutputHandler handler, PetriNet pn, NetViewer netViewer) throws IOException {
        LOGGER.info("Exporting Petri net to file '" + file.getName() + "'");
        handler.save(new FileOutputStream(file), pn, file, netViewer);
        LOGGER.info("Successfully exported Petri net to file '" + file.getName() + "'");
    }

    public static List<OutputHandler> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }

    public static boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file '" + file.getName() + "' is in supported format");
        for (OutputHandler handler : getHandlers()) {
            if (handler.isKnownFile(file)) {
                LOGGER.debug("File '" + file.getName() + "' is in supported format");
                return true;
            }
        }
        LOGGER.debug("File '" + file.getName() + "' is not in supported format");
        return false;
    }

    public static OutputHandler autoDetectHandler(File file) throws IOException {
        // See <URL:http://forums.sun.com/thread.jspa?messageID=9791669#9791669>
        LOGGER.debug("Detecting handler for file '" + file.getName() + "'");
        for (OutputHandler handler : getHandlers()) {
            if (handler.isKnownFile(file)) {
                LOGGER.debug("Successfully detected handler for file '" + file.getName() + "'");
                return handler;
            }
        }
        LOGGER.error("No handler for file '" + file.getName() + "' found");
        throw new InvalidClassException("No handler for this file found.");
    }

}
