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
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;

import monalisa.data.pn.PetriNet;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public final class InvCalcOutputHandler {
    private final Map<Integer, Integer> placeIds;
    private final Map<Integer, Integer> transitionIds;
    private static final Logger LOGGER = LogManager.getLogger(InvCalcOutputHandler.class);
    /**
     * For internal use only.
     */
    public InvCalcOutputHandler(Map<Integer, Integer> placeIds,
            Map<Integer, Integer> transitionIds, String InvType) {
        this.placeIds = placeIds;
        this.transitionIds = transitionIds;
    }

    public void save(PetriNetFacade petriNet, OutputStream out) {

        LOGGER.info("Exporting Petri net to calculate Invariants");
        PrintStream formatter = new PrintStream(out);

        formatter.printf("P   M   PRE,POST  NETZ %s",
            petriNet.getValueOrDefault("id", 0));
        if (petriNet.hasProperty("name"))
            formatter.printf(":%s", petriNet.getProperty("name"));
        formatter.println();

        // Net structure section.

        for (Entry<Place, Long> mark : petriNet.marking().entrySet()) {
            Place place = mark.getKey();
            formatter.printf("  %d %d ", placeId(place), mark.getValue());

            // List of output arcs.
            for(Transition transition : place.inputs()) {
                formatter.print(transitionId(transition));
                int weight = petriNet.getArc(transition, place).weight();
                if (weight != 1)
                    formatter.printf(": %d", weight);
                formatter.print(' ');
            }

            // List of input arcs.

            if (!place.outputs().isEmpty())
                formatter.print(", ");

            for (Transition transition : place.outputs()) {
                formatter.print(transitionId(transition));
                int weight = petriNet.getArc(place, transition).weight();
                if (weight != 1)
                    formatter.printf(": %d", weight);
                formatter.print(' ');
            }

            formatter.println();
        }

        formatter.println("@");

        // Place data section.

        formatter.println("place nr.             name capacity time");

        for (Place place : petriNet.places()) {
            formatter.printf("       %d: %s", placeId(place),
                sanitize(place.getValueOrDefault("name", "")));

            int capacity = place.getValueOrDefault("capacity", -1);
            if (capacity == -1)
                formatter.print(" oo");
            else
                formatter.printf(" %d", capacity);

            if (place.hasProperty("time")){
                formatter.printf(" %d", place.getValueOrDefault("time", 0));}
            else
                formatter.print(" 0");
            formatter.println();
        }

        formatter.println("@");

        // Transition data section.

        formatter.println("trans nr.             name priority time");

        for (Transition transition : petriNet.transitions()) {
            formatter.printf("       %d: %s", transitionId(transition),
                sanitize(transition.<String>getValueOrDefault("name", "")));
            if (transition.hasProperty("priority"))
                formatter.printf(" %d", transition.getValueOrDefault("priority", 0));
            else
                formatter.print(" 0");
            if (transition.hasProperty("time"))
                formatter.printf(" %d", transition.getValueOrDefault("time", 0));
            else
                formatter.print(" 0");
            formatter.println();
        }

        formatter.println("@");
        LOGGER.info("Successfully exported Petri net to calculate Invariants");
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
