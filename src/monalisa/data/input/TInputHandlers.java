/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.data.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import monalisa.data.pn.PetriNet;
import monalisa.results.Result;

/**
 * @author Jens Einloft
 */
public final class TInputHandlers {
    private static final List<TInputHandler> handlers = new ArrayList<>();
    
    static {
        handlers.add(new ResTInputHandler());
    }

    private TInputHandlers() { }
    
    public static Result load(File file, PetriNet petriNet) throws IOException {
        return load(file, autoDetectHandler(file), petriNet);
    }
    
    public static Result load(File file, TInputHandler handler, PetriNet petriNet) throws IOException {
        return handler.load(new FileInputStream(file), petriNet);
    }
    
    public static List<TInputHandler> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }
    
    public static boolean isKnownFile(File file) throws IOException {
        for (TInputHandler handler : getHandlers())
            if (handler.isKnownFile(file))
                return true;

        return false;
    }
    
    public static TInputHandler autoDetectHandler(File file) throws IOException {
        // TODO This should enumerate all classes found in this package.
        // See <URL:http://forums.sun.com/thread.jspa?messageID=9791669#9791669>
        
        for (TInputHandler handler : getHandlers())
            if (handler.isKnownFile(file))
                return handler;
        
        throw new InvalidClassException("No handler for this file found.");
    }
}
