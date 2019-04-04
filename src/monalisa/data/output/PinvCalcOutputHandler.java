/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.data.output;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PinvCalcOutputHandler {
    private final Map<Integer, Integer> placeIds;
    private final Map<Integer, Integer> transitionIds;
    private static final Logger LOGGER = LogManager.getLogger(PinvCalcOutputHandler.class);

    /**
     * For internal use only.
     */
    public PinvCalcOutputHandler(Map<Integer, Integer> placeIds, Map<Integer, Integer> transitionIds) {
        this.placeIds = placeIds;
        this.transitionIds = transitionIds;
    }

    public void save(PetriNetFacade petriNet, OutputStream out) {
        LOGGER.info("Exporting Petri net to calculate P-Invariants");
        int edgeCounter = 0;
        for(Transition transition : petriNet.transitions()) {
            edgeCounter+=transition.inputs().size();
            edgeCounter+=transition.outputs().size();
        }

        PrintStream formatter = new PrintStream(out);

        formatter.printf("P   M   PRE,POST  NETZ %s",
            petriNet.getValueOrDefault("id", 0));
        if (petriNet.hasProperty("name"))
            formatter.printf(":%s", petriNet.getProperty("name"));
        formatter.println();

        // Net structure section.

        for (Transition transition : petriNet.transitions()) {
            formatter.printf("  %d %d ", transitionId(transition), 1);

            // List of output arcs.
            for(Place place : transition.inputs()) {
                formatter.print(placeId(place));
                int weight = petriNet.getArc(place, transition).weight();
                if (weight != 1)
                    formatter.printf(": %d", weight);
                formatter.print(' ');
            }

            // List of input arcs.

            if (!transition.outputs().isEmpty())
                formatter.print(", ");

            for (Place place : transition.outputs()) {
                formatter.print(placeId(place));
                int weight = petriNet.getArc(transition, place).weight();
                if (weight != 1)
                    formatter.printf(": %d", weight);
                formatter.print(' ');
            }

            formatter.println();
        }

        formatter.println("@");

        // Place data section.

        formatter.println("place nr.             name capacity time");

        for (Transition transition : petriNet.transitions()) {
            formatter.printf("       %d: %s", transitionId(transition),
                sanitize(transition.getValueOrDefault("name", "")));

            int capacity = transition.getValueOrDefault("capacity", -1);
            if (capacity == -1)
                formatter.print(" oo");
            else
                formatter.printf(" %d", capacity);

            if (transition.hasProperty("time"))
                formatter.printf(" %d", transition.getProperty("time"));
            else
                formatter.print(" 0");
            formatter.println();
        }

        formatter.println("@");

        // Transition data section.

        formatter.println("trans nr.             name priority time");

        for (Place place : petriNet.places()) {
            formatter.printf("       %d: %s", placeId(place),
                sanitize(place.<String>getValueOrDefault("name", "")));
            if (place.hasProperty("priority"))
                formatter.printf(" %d", place.getProperty("priority"));
            else
                formatter.print(" 0");
            if (place.hasProperty("time"))
                formatter.printf(" %d", place.getProperty("time"));
            else
                formatter.print(" 0");
            formatter.println();
        }

        formatter.println("@");
        LOGGER.info("Successfully exported Petri net to calculate P-Invariants");
    }

    private static String sanitize(String name) {
        return String.format("%-16s", name.replaceAll("[^a-zA-Z0-9]", "_"));
    }

    private int placeId(Place p) {
        return placeIds != null ? placeIds.get(p.id()) : p.id();
    }

    private int transitionId(Transition t) {
        return transitionIds != null ? transitionIds.get(t.id()) : t.id();
    }

    public boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file is in pnt format");
        return "pnt".equalsIgnoreCase(FileUtils.getExtension(file));
    }

    public File checkFileNameForExtension(File file) {
        if(!"pnt".equalsIgnoreCase(FileUtils.getExtension(file)))
            file = new File(file.getAbsolutePath()+".pnt");
        return file;
    }
}
