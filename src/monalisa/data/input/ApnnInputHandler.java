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

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handler to read APNN files
 * http://ls4-www.cs.tu-dortmund.de/APNN-TOOLBOX/grammars/apnn.html
 *
 * @author Jens Einloft
 */
public class ApnnInputHandler implements InputHandler {

    private Map<String, Place> places;
    private Map<String, Transition> transitions;
    private int placeCounter;
    private int transitionCounter;
    private static final Logger LOGGER = LogManager.getLogger(ApnnInputHandler.class);

    @Override
    public PetriNet load(InputStream in, File file) throws IOException {
        LOGGER.info("Loading Petri net from .apnn file");
        places = new HashMap<>();
        transitions = new HashMap<>();
        placeCounter = 0;
        transitionCounter = 0;

        PetriNet pn = new PetriNet();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            String tmp, from, to;
            int weight = 0;

            while (true) {
                line = reader.readLine();
                System.out.println(line);
                if (line == null) {
                    break;
                }
                if (line.equals("")) {
                    continue;
                }

                if (line.startsWith("\\beginnet") || line.startsWith("\\inputnet")) {
                    pn.putProperty("name", line.substring(line.indexOf("{") + 1, line.indexOf("}")));
                }

                if (line.startsWith("\\place")) {
                    tmp = line.substring(line.indexOf("\\name"), line.length());
                    findPlace(line.substring(line.indexOf("{") + 1, line.indexOf("}")), tmp.subSequence(tmp.indexOf("{") + 1, tmp.indexOf("}")).toString(), pn);
                }

                if (line.startsWith("\\transition")) {
                    tmp = line.substring(line.indexOf("\\name"), line.length());
                    findTransition(line.substring(line.indexOf("{") + 1, line.indexOf("}")), tmp.subSequence(tmp.indexOf("{") + 1, tmp.indexOf("}")).toString(), pn);
                }

                if (line.startsWith("\\arc")) {
                    tmp = line.substring(line.indexOf("\\from"), line.length());
                    from = tmp.substring(tmp.indexOf("{") + 1, tmp.indexOf("}"));

                    tmp = line.substring(line.indexOf("\\to"), line.length());
                    to = tmp.substring(tmp.indexOf("{") + 1, tmp.indexOf("}"));

                    tmp = line.substring(line.indexOf("\\weight"), line.length());
                    weight = new Integer(tmp.substring(tmp.indexOf("{") + 1, tmp.indexOf("}")));

                    if (places.containsKey(from)) {
                        pn.addArc(places.get(from), transitions.get(to), weight);
                    } else {
                        pn.addArc(transitions.get(from), places.get(to), weight);
                    }
                }

            }
        }
        LOGGER.info("Successfully loaded Petri net from .apnn file");
        return pn;
    }

    private void findPlace(String id, String placeName, PetriNet petriNet) {
        Place place = places.get(id);

        if (place == null) {
            LOGGER.debug("Creating new place with id '" + id + "' and name '" + placeName + "'");
            place = new Place(++placeCounter);
            places.put(id, place);
            place.putProperty("name", placeName);
            petriNet.addPlace(place);
            LOGGER.debug("Successfully created new place with id '" + id + "' and name '" + placeName + "'");
        }
    }

    private void findTransition(String id, String transitionName, PetriNet petriNet) {
        Transition transition = transitions.get(transitionName);

        if (transition == null) {
            LOGGER.debug("Creating new transition with id '" + id + "' and name '" + transitionName + "'");
            transition = new Transition(++transitionCounter);
            transitions.put(id, transition);
            transition.putProperty("name", transitionName);
            petriNet.addTransition(transition);
            LOGGER.debug("Successfully created new transition with id '" + id + "' and name '" + transitionName + "'");
        }
    }

    @Override
    public boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file is in APNN format");
        return ("apnn".equalsIgnoreCase(FileUtils.getExtension(file)));
    }

    @Override
    public String getDescription() {
        return "APNN";
    }
}
