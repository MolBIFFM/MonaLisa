/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.results;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import monalisa.Project;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Knockout implements Result {

    private static final long serialVersionUID = 3512064024064281251L;
    private final Map<List<String>, List<String>> knockouts;
    private static final Logger LOGGER = LogManager.getLogger(Knockout.class);

    public Knockout(Map<List<String>, List<String>> knockouts) {
        this.knockouts = knockouts;
    }

    @Override
    public void export(File path, Configuration config, Project project) throws IOException {
        try (PrintWriter printer = new PrintWriter(path)) {
            LOGGER.info("Exporting Knockout results");

            Map<String, Integer> transitionMap = new HashMap<>();
            Map<String, Integer> placeMap = new HashMap<>();
            StringBuilder sb = new StringBuilder();

            String knockedOutString;
            Map<String, Integer> knockedOutMap;

            // List of all transitions
            sb.append("# reaction_id:name\n");
            int i = 1;
            for (Transition t : project.getPetriNet().transitions()) {
                transitionMap.put((String) t.getProperty("name"), i);
                sb.append(i++);
                sb.append(":");
                sb.append(t.<String>getProperty("name"));
                sb.append("\n");
            }

            // List of all Places
            sb.append("\n# species_id:name\n");
            i = 1;
            for (Place p : project.getPetriNet().places()) {
                placeMap.put((String) p.getProperty("name"), i);
                sb.append(i++);
                sb.append(":");
                sb.append(p.<String>getProperty("name"));
                sb.append("\n");
            }

            // What is knocked out - places or transitions?
            if (config.toString().contains("Transition")) {
                knockedOutString = "reactions";
                knockedOutMap = transitionMap;
            } else {
                knockedOutString = "species";
                knockedOutMap = placeMap;
            }

            sb.append("\n# ko_");
            sb.append(knockedOutString);
            sb.append("_id; ... : ko_affected_reaction; ...\n");

            int knockedOutSize, alsoKnockedOutSize;
            for (List<String> knockedOut : knockouts.keySet()) {
                knockedOutSize = knockedOut.size();
                for (i = 0; i < knockedOutSize; i++) {
                    sb.append(knockedOutMap.get(knockedOut.get(i)));
                    sb.append(";");
                }
                sb.setLength(sb.length() - 1);
                sb.append(":");

                alsoKnockedOutSize = knockouts.get(knockedOut).size();
                for (i = 0; i < alsoKnockedOutSize; i++) {
                    sb.append(transitionMap.get(knockouts.get(knockedOut).get(i)));
                    sb.append(";");
                }
                sb.setLength(sb.length() - 1);
                sb.append("\n");
            }

            printer.print(sb.toString());
            LOGGER.info("Successfully exported Knockout results");
        }
    }

    @Override
    public String filenameExtension() {
        return "txt";
    }
}
