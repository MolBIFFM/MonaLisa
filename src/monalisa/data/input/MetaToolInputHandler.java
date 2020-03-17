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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import monalisa.data.pn.Arc;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Input handler for the MetaTool format.
 * @author Jens Einloft
 * @see <a href="http://pinguin.biologie.uni-jena.de/bioinformatik/networks/metatool/metatool5.0/ecoli_networks.html">http://pinguin.biologie.uni-jena.de/bioinformatik/networks/metatool/metatool5.0/ecoli_networks.html</a>
 **/
public class MetaToolInputHandler implements InputHandler {
    private Map<String, Place> places;
    private Map<String, Transition> transitions;
    private List<Transition> reversible;
    private List<String> external;
    private int placeCounter, transitionCounter;
    private static final Logger LOGGER = LogManager.getLogger(MetaToolInputHandler.class);

    @Override
    public PetriNet load(InputStream in) throws IOException {
        LOGGER.info("Loading Petri net from MetaTool file");
        places = new HashMap<>();
        transitions = new HashMap<>();
        reversible = new ArrayList<>();
        external = new ArrayList<>();
        placeCounter = 0;
        transitionCounter = 0;

        PetriNet ret = new PetriNet();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line, flag = null;
        String[] lineParts = null, reactionPlaces, prePlaces, postPlaces, placeParts;
        int lenLineParts = 0, lenPlaces, i, weight;
        Transition transition = null, revTransition = null;
        Place place = null;
        Boolean isReversible;

        while(true) {
            line = reader.readLine();
            if(line == null)
                break;
            if(line.startsWith("-")) {
                flag = line.substring(1);
                continue;
            }
            else if((!line.startsWith("-") && flag == null) || line.isEmpty())
                continue;
            if(flag.equalsIgnoreCase("ENZREV")) {
                lineParts = line.split("\\s+");
                lenLineParts = lineParts.length;
                for(i = 0; i < lenLineParts; i++) {
                    reversible.add(findTransition(lineParts[i], ret));
                }
            }
            else if(flag.equalsIgnoreCase("METEXT")) {
                lineParts = line.split("\\s+");
                lenLineParts = lineParts.length;
                for(i = 0; i < lenLineParts; i++) {
                    external.add(lineParts[i]);
                }
            }
            else if(flag.equalsIgnoreCase("CAT")) {
                lineParts = line.split("\\s+:\\s+");
                transition = findTransition(lineParts[0], ret);
                isReversible = false;

                if(reversible.contains(transition)) {
                    revTransition = findTransition(transition.getProperty("name")+"_rev", ret);
                    isReversible = true;
                }

                reactionPlaces = lineParts[1].split("\\s+=\\s+");
                if(line.contains("="))
                    prePlaces = reactionPlaces[0].split("\\s+\\+\\s+");
                else
                    prePlaces = null;
                if(reactionPlaces.length > 1)
                    postPlaces = reactionPlaces[1].split("\\s+\\+\\s+");
                else if(prePlaces == null)
                    postPlaces = reactionPlaces[0].split("\\s+\\+\\s+");
                else
                    postPlaces = null;

                if(prePlaces != null) {
                    lenPlaces = prePlaces.length;
                    for(i = 0; i < lenPlaces; i++) {
                        placeParts = prePlaces[i].split("\\s+");
                        if(placeParts.length == 2) {
                            if(external.contains(placeParts[1]))
                                continue;
                            weight = new Integer(placeParts[0]);
                            place = findPlace(placeParts[1], ret);
                        }
                        else {
                            if(external.contains(placeParts[0]))
                                continue;
                            weight = 1;
                            place = findPlace(placeParts[0], ret);
                        }
                        ret.addArc(place, transition, new Arc(transition, place, weight));
                        if(isReversible)
                            ret.addArc(revTransition, place, new Arc(transition, place, weight));
                    }
                }

                if(postPlaces != null) {
                    lenPlaces = postPlaces.length;
                    for(i = 0; i < lenPlaces; i++) {
                        placeParts = postPlaces[i].split("\\s+");
                        if(placeParts.length == 1 || (placeParts.length == 2 && placeParts[placeParts.length-1].equals("."))) {
                            if(external.contains(placeParts[0]))
                                continue;
                            weight = 1;
                            place = findPlace(placeParts[0], ret);
                        } else {
                            if(external.contains(placeParts[1]))
                                continue;
                            weight = new Integer(placeParts[0]);
                            place = findPlace(placeParts[1], ret);
                        }
                        ret.addArc(transition, place, new Arc(transition, place, weight));
                        if(isReversible)
                            ret.addArc(place, revTransition, new Arc(transition, place, weight));
                    }
                }
            }
        }
        LOGGER.info("Successfully loaded Petri net from MetaTool file");
        return ret;
    }

    @Override
    public boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file is in MetaTool format");
        return "dat".equalsIgnoreCase(FileUtils.getExtension(file)) || "meta".equalsIgnoreCase(FileUtils.getExtension(file));
    }

    private Place findPlace(String placeName, PetriNet petriNet) {
        Place place = places.get(placeName);

        if (place == null) {
            LOGGER.debug("Creating new place with name '" + placeName + "'");
            place = new Place(++placeCounter);
            places.put(placeName, place);
            place.putProperty("name", placeName);
            petriNet.addPlace(place);
            LOGGER.debug("Successfully created new place with name '" + placeName + "'");
        }
        return place;
    }

    private Transition findTransition(String transitionName, PetriNet petriNet) {
        Transition transition = transitions.get(transitionName);

        if (transition == null) {
            LOGGER.debug("Creating new transition with name '" + transitionName + "'");
            transition = new Transition(++transitionCounter);
            transitions.put(transitionName, transition);
            transition.putProperty("name", transitionName);
            petriNet.addTransition(transition);
            LOGGER.debug("Successfully created new place with name '" + transitionName + "'");
        }
        return transition;
    }

    @Override
    public String getDescription() {
        return "MetaTool (DAT)";
    }

}
