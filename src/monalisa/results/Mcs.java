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
import java.util.Set;
import monalisa.Project;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jens Einloft
 */
public class Mcs implements Result {
    static final long serialVersionUID = 7539023529715751381L;

    private final List<Set<Transition>> mcs;
    private static final Logger LOGGER = LogManager.getLogger(Mcs.class);

    public Mcs(List<Set<Transition>> mcs) {
        this.mcs = mcs;
    }

    @Override
    public String filenameExtension() {
        return "mcs";
    }

    @Override
    public void export(File path, Configuration config, Project project) throws IOException {
        try (PrintWriter printer = new PrintWriter(path)) {
            LOGGER.info("Exporting MCS results");
            Map<Transition,Integer> transitionMap = new HashMap<>();
            StringBuilder sb = new StringBuilder();

            // List of all transitions
            sb.append("# reaction_id:name\n");
            int i = 1;
            for(Transition t : project.getPetriNet().transitions()) {
                transitionMap.put(t, i);
                sb.append(i++);
                sb.append(":");
                sb.append(t.<String>getProperty("name"));
                sb.append("\n");
            }

            sb.append("\n# mcs_id:reaction_id; ...\n");

            i = 1;
            for(Set<Transition> mc : mcs) {
                sb.append(i++);
                sb.append(":");

                for(Transition t : mc) {
                    sb.append(transitionMap.get(t));
                    sb.append(";");
                }
                sb.setLength(sb.length() - 1);
                sb.append("\n");
            }

            printer.print(sb.toString());
            LOGGER.info("Successfully exported MCS results");
        }
    }

    /**
     * Return the number of stored minimal cut sets
     * @return
     */
    public int size() {
        return mcs.size();
    }

    /**
     * Returns the List of stored minimal cut sets
     * @return
     */
    public List<Set<Transition>> getMCS() {
        return this.mcs;
    }

}
