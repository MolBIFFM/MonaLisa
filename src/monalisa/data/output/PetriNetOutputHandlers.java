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
import monalisa.data.pn.PetriNet;

/**
 *
 * @author Jens Einloft
 */
public final class PetriNetOutputHandlers {
    private static final List<OutputHandler> handlers = new ArrayList<>();

    static {
        handlers.add(new SbmlOutputHandler(3,1));        
        handlers.add(new SbmlOutputHandler(2,4)); 
        handlers.add(new PntOutputHandler());        
        handlers.add(new Pipe3OutputHandler());
        handlers.add(new Pipe4OutputHandler());
        handlers.add(new MetaToolOutputHandler());
        handlers.add(new ApnnOutputHandler());
        handlers.add(new PlainOutputHandler());
    }

    private PetriNetOutputHandlers() { }

    public static void save(File file, PetriNet pn) throws IOException {
        save(file, autoDetectHandler(file), pn);
    }

    public static void save(File file, OutputHandler handler, PetriNet pn) throws IOException {
        handler.save(new FileOutputStream(file), pn);
    }

    public static List<OutputHandler> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }

    public static boolean isKnownFile(File file) throws IOException {
        for (OutputHandler handler : getHandlers())
            if (handler.isKnownFile(file))
                return true;

        return false;
    }

    public static OutputHandler autoDetectHandler(File file) throws IOException {
        // See <URL:http://forums.sun.com/thread.jspa?messageID=9791669#9791669>

        for (OutputHandler handler : getHandlers())
            if (handler.isKnownFile(file))
                return handler;

        throw new InvalidClassException("No handler for this file found.");
    }

}
