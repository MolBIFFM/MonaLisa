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

public final class PetriNetInputHandlers {
    private static final List<InputHandler> handlers = new ArrayList<>();
    
    static {
        handlers.add(new SbmlInputHandler());
        handlers.add(new PntInputHandler());
        handlers.add(new KeggInputHandler());
        handlers.add(new SppedInputHandler());
        handlers.add(new Pipe2InputHandler());
        handlers.add(new Pipe3InputHandler());
        handlers.add(new Pipe4InputHandler());        
        handlers.add(new MetaToolInputHandler());
        handlers.add(new ApnnInputHandler());
    }

    private PetriNetInputHandlers() { }
    
    public static PetriNet load(File file) throws IOException {
        return load(file, autoDetectHandler(file));
    }
    
    public static PetriNet load(File file, InputHandler handler) throws IOException {
        return handler.load(new FileInputStream(file));
    }
    
    public static List<InputHandler> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }
    
    public static boolean isKnownFile(File file) throws IOException {
        for (InputHandler handler : getHandlers())
            if (handler.isKnownFile(file))
                return true;

        return false;
    }
    
    public static InputHandler autoDetectHandler(File file) throws IOException {
        // TODO This should enumerate all classes found in this package.
        // See <URL:http://forums.sun.com/thread.jspa?messageID=9791669#9791669>
        
        for (InputHandler handler : getHandlers())
            if (handler.isKnownFile(file))
                return handler;
        
        throw new InvalidClassException("No handler for this file found.");
    }
}
