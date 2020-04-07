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
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * http://ls4-www.cs.tu-dortmund.de/APNN-TOOLBOX/grammars/apnn.html
 *
 * @author Joachim Nöthen & Jens Einloft
 */
public class ApnnOutputHandler implements OutputHandler {

    private Map<Integer, Integer> placeIds = new HashMap<>();
    private Map<Integer, Integer> transitionIds = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger(ApnnOutputHandler.class);

    public void save(FileOutputStream fileOutputStream, PetriNet petriNet) {
        LOGGER.info("Exporting Petri net to APNN format");
        try (PrintStream formatter = new PrintStream(fileOutputStream)) {
            int pid = 0;
            for (Place place : petriNet.places()) {
                this.placeIds.put(place.id(), pid++);
            }

            int tid = 0;
            for (Transition transition : petriNet.transitions()) {
                this.transitionIds.put(transition.id(), tid++);
            }

            // header
            formatter.printf("\\beginnet{petrinet}\n");
            formatter.println();

            // places
            String name;
            int capacity;
            long marking;
            for (Place place : petriNet.places()) {
                name = place.getValueOrDefault("name", "noname");
                capacity = place.getValueOrDefault("capacity", 1);
                marking = petriNet.marking().get(place);
                formatter.printf("\\place{P_%d}{\\name{%s}\\capacity{%d}\\init{%d}}\n", placeId(place), sanitize(name), capacity, marking);
            }

            formatter.println();

            // transitions
            for (Transition transition : petriNet.transitions()) {
                name = transition.getValueOrDefault("name", "noname");
                formatter.printf("\\transition{T_%d}{\\name{%s}}\n", transitionId(transition), sanitize(name));
            }

            formatter.println();

            // arcs
            int arcId = 0;
            for (Place p : petriNet.places()) {
                for (Transition t : p.outputs()) {
                    formatter.printf("\\arc{A_%d}{\\from{P_%d}\\to{T_%d}\\weight{%d}\\type{ordinary}}\n", arcId, placeId(p), transitionId(t), petriNet.getArc(p, t).weight());
                    arcId++;
                }
            }
            for (Transition t : petriNet.transitions()) {
                for (Place p : t.outputs()) {
                    formatter.printf("\\arc{A_%d}{\\from{T_%d}\\to{P_%d}\\weight{%d}\\type{ordinary}}\n", arcId, transitionId(t), placeId(p), petriNet.getArc(t, p).weight());
                    arcId++;
                }
            }

            formatter.println();

            formatter.printf("\\endnet");
            LOGGER.info("Successfully exported Petri net to APNN format");
        }
    }

    private static String sanitize(String name) {
        return String.format("%s", name.replaceAll("[^a-zA-Z0-9]", "_"));
    }

    public boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file is in apnn format");
        return ("apnn".equalsIgnoreCase(FileUtils.getExtension(file)));
    }

    public File checkFileNameForExtension(File file) {
        if (!"apnn".equalsIgnoreCase(FileUtils.getExtension(file))) {
            file = new File(file.getAbsolutePath() + ".apnn");
        }
        return file;
    }

    public String getExtension() {
        return "apnn";
    }

    public String getDescription() {
        return "Abstract Petri Net Notation";
    }

    private int placeId(Place p) {
        return placeIds != null ? placeIds.get(p.id()) : p.id();
    }

    private int transitionId(Transition t) {
        return transitionIds != null ? transitionIds.get(t.id()) : t.id();
    }
}
