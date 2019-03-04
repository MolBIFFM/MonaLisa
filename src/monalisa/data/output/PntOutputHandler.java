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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.FileUtils;

/**
 *
 * @author Jens Einloft
 */
public class PntOutputHandler implements OutputHandler {

    private Map<Integer, Integer> placeIds = new HashMap<>();
    private Map<Integer, Integer> transitionIds = new HashMap<>();

    public void save(FileOutputStream fileOutputStream, PetriNet petriNet) {
        int pid = 0;
        for (Place place : petriNet.places())
            this.placeIds.put(place.id(), pid++);

        int tid = 0;
        for (Transition transition : petriNet.transitions())
            this.transitionIds.put(transition.id(), tid++);
        try (PrintStream formatter = new PrintStream(fileOutputStream)) {
            formatter.printf("P   M   PRE,POST  NETZ %s",
                petriNet.getValueOrDefault("id", 0));
            if (petriNet.hasProperty("name"))
                formatter.printf(":%s", petriNet.getProperty("name"));
            formatter.println();

            // Net structure section.

            for (Entry<Place, Long> mark : petriNet.marking().entrySet()) {
                Place place = mark.getKey();
                formatter.printf(" %d %d ", placeId(place), mark.getValue());

                // List of output arcs.

                int transitionCounter = 0;
                for (Transition transition : place.inputs()) {
                    formatter.print(transitionId(transition));
                    int weight = petriNet.getArc(transition, place).weight();
                    if (weight != 1)
                        formatter.printf(": %d", weight);
                    if(transitionCounter+1 < place.inputs().size())
                        formatter.print(' ');
                    transitionCounter++;
                }

                // List of input arcs.

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

            String name;
            for (Place place : petriNet.places()) {
                name = place.getValueOrDefault("name", "");
                formatter.printf("       %d: %s", placeId(place), sanitize(name));

                int capacity = place.getValueOrDefault("capacity", -1);
                if (capacity == -1)
                    formatter.print(" oo");
                else
                    formatter.printf(" %d", capacity);

                if (place.hasProperty("time"))
                    formatter.printf(" %d", place.getProperty("time"));
                else
                    formatter.print(" 0");
                formatter.println();
            }

            formatter.println("@");

            // Transition data section.

            formatter.println("trans nr.             name priority time");

            for (Transition transition : petriNet.transitions()) {
                name = transition.getValueOrDefault("name", "");
                formatter.printf("       %d: %s", transitionId(transition),
                    sanitize(name));
                if (transition.hasProperty("priority"))
                    formatter.printf(" %d", transition.getProperty("priority"));
                else
                    formatter.print(" 0");
                if (transition.hasProperty("time"))
                    formatter.printf(" %d", transition.getProperty("time"));
                else
                    formatter.print(" 0");
                formatter.println();
            }

            formatter.println("@");
        }
    }

    public boolean isKnownFile(File file) throws IOException {
        return ("pnt".equalsIgnoreCase(FileUtils.getExtension(file)));
    }

    public File checkFileNameForExtension(File file) {
        if(!"pnt".equalsIgnoreCase(FileUtils.getExtension(file)))
            file = new File(file.getAbsolutePath()+".pnt");
        return file;
    }

    public String getExtension() {
        return "pnt";
    }

    public String getDescription() {
        return "PNT";
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

}
