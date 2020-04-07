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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import monalisa.data.pn.Arc;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Input handler for the PNT format. Format specifications may be found at
 * <a href="http://www2.informatik.hu-berlin.de/lehrstuehle/automaten/ina/node14.html#SECTION00534100000000000000">www2.informatik.hu-berlin.de</a>.
 *
 * @author Konrad Rudolph - modified by Jens Einloft
 */
public final class PntInputHandler implements InputHandler {

    private final Map<Integer, Place> places = new HashMap<>();
    private final Map<Integer, Transition> transitions = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger(PntInputHandler.class);

    @Override
    public PetriNet load(InputStream in) throws IOException {
        LOGGER.info("Loading Petri net from .pnt file");
        places.clear();
        transitions.clear();

        PetriNet ret = new PetriNet();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        // Skip the header line, but save the name of the Petri net.
        Pattern whitespace = Pattern.compile("\\s");
        String header = reader.readLine();
        String[] headerParts = whitespace.split(header);
        String idAndNamePart = headerParts[headerParts.length - 1];

        int id = 0;
        String name = null;
        if (!idAndNamePart.contains(":")) {
            id = Integer.parseInt(idAndNamePart);
        } else {
            int indexOfDoublePoint = idAndNamePart.indexOf(":");
            id = Integer.parseInt(idAndNamePart.substring(0, indexOfDoublePoint));
            name = idAndNamePart.substring(indexOfDoublePoint + 1, idAndNamePart.length());
        }

        ret.putProperty("id", id);
        if (name != null) {
            ret.putProperty("name", name);
        }

        // Net structure section.
        String line, token;
        int placeId, weight = 0, transitionId;;
        Long tokens = 0L;
        Scanner scanner = null;
        Transition transition;
        Boolean endOfInArcs = false, noInput;
        while (!(line = reader.readLine()).equals("@")) {
            scanner = new Scanner(line);
            placeId = scanner.nextInt();
            Place place = findPlace(placeId, ret);

            tokens = scanner.nextLong();
            ret.setTokens(place, tokens);

            noInput = false;
            if (scanner.hasNext(",")) {
                scanner.next();
                noInput = true;
            }

            // List of input arcs.
            if (!noInput) {
                endOfInArcs = false;
                while (scanner.hasNext()) {
                    token = scanner.next();
                    if (token.contains(":")) {
                        transitionId = Integer.parseInt(token.substring(0, token.indexOf(":")));
                        token = scanner.next();
                        if (token.contains(",")) {
                            weight = Integer.parseInt(token.substring(0, token.indexOf(",")));
                            endOfInArcs = true;
                        } else {
                            weight = Integer.parseInt(token);
                        }
                        transition = findTransition(transitionId, ret);
                        ret.addArc(transition, place, new Arc(place, transition, weight));
                    } else {
                        if (token.length() >= 1) {
                            if (token.contains(",")) {
                                transitionId = Integer.parseInt(token.substring(0, token.indexOf(",")));
                                endOfInArcs = true;
                            } else {
                                transitionId = Integer.parseInt(token);
                            }
                            transition = findTransition(transitionId, ret);
                            ret.addArc(transition, place);
                        }
                    }

                    if (endOfInArcs) {
                        break;
                    }
                }
            }

            // List of output arcs.
            while (scanner.hasNext()) {
                token = scanner.next();
                if (token.contains(":")) {
                    transitionId = Integer.parseInt(token.substring(0, token.indexOf(":")));
                    weight = Integer.parseInt(scanner.next());
                    transition = findTransition(transitionId, ret);
                    ret.addArc(place, transition, new Arc(transition, place, weight));
                } else {
                    transitionId = Integer.parseInt(token);
                    transition = findTransition(transitionId, ret);
                    ret.addArc(place, transition);
                }
            }
        }

        // Skip header line.
        reader.readLine();

        // Place data section.
        int capacity, time, priority;
        MatchResult idMatch;
        Place place;
        String placeName;
        while (!(line = reader.readLine()).equals("@")) {
            scanner = new Scanner(line);
            scanner.next("(\\d+)\\s*:");
            idMatch = scanner.match();
            placeId = Integer.parseInt(idMatch.group(1));
            place = findPlace(placeId, ret);
            placeName = scanner.next();
            capacity = scanner.hasNext("oo") && scanner.next("oo") != null ? -1 : scanner.nextInt();
            time = scanner.nextInt();
            place.putProperty("name", placeName);
            place.putProperty("capacity", capacity);
            place.putProperty("time", time);
        }

        // Skip header line.
        reader.readLine();

        // Transition data section.
        String transitionName;
        while (!(line = reader.readLine()).equals("@")) {
            scanner = new Scanner(line);
            scanner.next("(\\d+)\\s*:");
            idMatch = scanner.match();
            transitionId = Integer.parseInt(idMatch.group(1));
            transition = findTransition(transitionId, ret);
            transitionName = scanner.next();
            priority = scanner.nextInt();
            time = scanner.nextInt();
            transition.putProperty("name", transitionName);
            transition.putProperty("priority", priority);
            transition.putProperty("time", time);
        }
        LOGGER.info("Successfully loaded Petri net from .pnt file");
        return ret;
    }

    private Place findPlace(int placeId, PetriNet petriNet) {
        Place place = places.get(placeId);

        if (place == null) {
            LOGGER.debug("Creating new place with placeID '" + Integer.toString(placeId) + "'");
            place = new Place(placeId);
            places.put(placeId, place);
            petriNet.addPlace(place);
            LOGGER.debug("Successfully created new place with placeID '" + Integer.toString(placeId) + "'");
        }
        return place;
    }

    private Transition findTransition(int transitionId, PetriNet petriNet) {
        Transition transition = transitions.get(transitionId);

        if (transition == null) {
            LOGGER.debug("Creating new transition with transitionID '" + Integer.toString(transitionId) + "'");
            transition = new Transition(transitionId);
            transitions.put(transitionId, transition);
            petriNet.addTransition(transition);
            LOGGER.debug("Successfully created new transition with transitionID '" + Integer.toString(transitionId) + "'");
        }
        return transition;
    }

    @Override
    public boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file is in PNT format");
        return "pnt".equalsIgnoreCase(FileUtils.getExtension(file));
    }

    @Override
    public String getDescription() {
        return "PNT";
    }

}
