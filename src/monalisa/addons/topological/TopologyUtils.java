/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.topological;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Contains some utility function for topology independent from UI
 * @author Marcel Gehrmann
 */
public class TopologyUtils {

    private static final Logger LOGGER = LogManager.getLogger(TopologyUtils.class);

    private Map<Integer, Double> freqPlacesAll;
    private Map<Integer, Double> freqPlacesIn;    
    private Map<Integer, Double> freqPlacesOut;
    private Map<Integer, Double> freqTransitionsAll;    
    private Map<Integer, Double> freqTransitionsIn;
    private Map<Integer, Double> freqTransitionsOut;
    
    private Map<Integer, Integer> ndPlacesAll;
    private Map<Integer, Integer> ndPlacesIn;
    private Map<Integer, Integer> ndPlacesOut;
    private Map<Integer, Integer> ndTransitionsAll;
    private Map<Integer, Integer> ndTransitionsIn;    
    private Map<Integer, Integer> ndTransitionsOut;

    public TopologyUtils(){
        
    }

    /**
     * Export of the node degree statistics.
     * @param freqMap frequency map to export
     * @param file name of export file
     */
    public void writeCSVtoFile(Map<Integer, Double> freqMap, File file) {
        LOGGER.debug("Writing vertex degree distribution to file '" + file.getName() + "'");
        List<Integer> listOfDegrees = new ArrayList<>(freqMap.keySet());
        Collections.sort(listOfDegrees);
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
            writer.write("p\tp(k)\n");
            for (Integer i : listOfDegrees) {
                writer.write(i + "\t" + freqMap.get(i) + "\n");
            }
            LOGGER.debug("Successfully wrote vertex degree distribution to file '" + file.getName() + "'");
        } catch (IOException ex) {
            LOGGER.error("Issue while writing vertex degree distribution to file '" + file.getName() + "': ", ex);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ex) {
                LOGGER.error("Issue while closing writer :", ex);
            }
        }
    }

    /**
     * Returns a frequency map.
     * @param mapname which frequency map should be returned
     * @return
     */
    public Map<Integer, Double> getFreqMap(String mapname) {
        switch (mapname) {
            case "PlacesAll":
                return freqPlacesAll;
            case "PlacesIn":
                return freqPlacesIn;
            case "PlacesOut":
                return freqPlacesOut;
            case "TransitionsAll":
                return freqTransitionsAll;
            case "TransitionsIn":
                return freqTransitionsIn;
            case "TransitionsOut":
                return freqTransitionsOut;
            default:
                return null;
        }
    }

    /**
     * Returns an nd map.
     * @param mapname which nd map should be returned
     * @return
     */
    public Map<Integer, Integer> getNdMap(String mapname) {
        switch (mapname) {
            case "PlacesAll":
                return ndPlacesAll;
            case "PlacesIn":
                return ndPlacesIn;
            case "PlacesOut":
                return ndPlacesOut;
            case "TransitionsAll":
                return ndTransitionsAll;
            case "TransitionsIn":
                return ndTransitionsIn;
            case "TransitionsOut":
                return ndTransitionsOut;
            default:
                return null;
        }
    }

    /**
     * Resets all frequency maps.
     */
    public void resetFreqMaps(){
        freqPlacesAll = new HashMap<>();
        freqPlacesIn = new HashMap<>();
        freqPlacesOut = new HashMap<>();
        freqTransitionsAll = new HashMap<>();
        freqTransitionsIn = new HashMap<>();
        freqTransitionsOut = new HashMap<>();
    }

    /**
     * Resets all nd maps.
     */
    public void resetNdMaps(){
        ndPlacesAll = new HashMap<>();
        ndPlacesIn = new HashMap<>();
        ndPlacesOut = new HashMap<>();
        ndTransitionsAll = new HashMap<>();
        ndTransitionsIn = new HashMap<>();
        ndTransitionsOut = new HashMap<>();
    }

    /**
     * Calculates the p(k) values
     * @param ndMap
     * @param freqMap
     * @param elementCounter
     */
    private void calcFreq(Map<Integer, Integer> ndMap, Map<Integer, Double> freqMap, double elementCounter) {
        LOGGER.debug("Calculating frequency");
        List<Integer> listOfDegrees = new ArrayList<>(ndMap.keySet());
        Collections.sort(listOfDegrees);
        for (Integer i : listOfDegrees) {
            freqMap.put(i, (double) ndMap.get(i) / elementCounter);
        }
        LOGGER.debug("Successfully calculated frequency");
    }

    /**
     * Actual computation of the degrees
     */
    void computeDegrees(PetriNetFacade pnf) {
        resetNdMaps();
        resetFreqMaps();
        Integer all;
        Integer in;
        Integer out;
        for (Place p : pnf.places()) {
            in = p.inputs().size();
            out = p.outputs().size();
            all = p.inputs().size() + p.outputs().size();
            if (!ndPlacesAll.containsKey(all)) {
                ndPlacesAll.put(all, 0);
            }
           ndPlacesAll.put(all, ndPlacesAll.get(all) + 1);
            if (!ndPlacesIn.containsKey(in)) {
                ndPlacesIn.put(in, 0);
            }
            ndPlacesIn.put(in, ndPlacesIn.get(in) + 1);
            if (!ndPlacesOut.containsKey(out)) {
                ndPlacesOut.put(out, 0);
            }
            ndPlacesOut.put(out, ndPlacesOut.get(out) + 1);
        }
        calcFreq(ndPlacesAll, freqPlacesAll, (double) pnf.places().size());
        calcFreq(ndPlacesIn, freqPlacesIn, (double) pnf.places().size());
        calcFreq(ndPlacesOut, freqPlacesOut, (double) pnf.places().size());
        for (Transition t : pnf.transitions()) {
            in = t.inputs().size();
            out = t.outputs().size();
            all = t.inputs().size() + t.outputs().size();
            if (!ndTransitionsAll.containsKey(all)) {
                ndTransitionsAll.put(all, 0);
            }
            ndTransitionsAll.put(all, ndTransitionsAll.get(all) + 1);
            if (!ndTransitionsIn.containsKey(in)) {
                ndTransitionsIn.put(in, 0);
            }
            ndTransitionsIn.put(in, ndTransitionsIn.get(in) + 1);
            if (!ndTransitionsOut.containsKey(out)) {
                ndTransitionsOut.put(out, 0);
            }
            ndTransitionsOut.put(out, ndTransitionsOut.get(out) + 1);
        }
        calcFreq(ndTransitionsAll, freqTransitionsAll, (double) pnf.transitions().size());
        calcFreq(ndTransitionsIn, freqTransitionsIn, (double) pnf.transitions().size());
        calcFreq(ndTransitionsOut, freqTransitionsOut, (double) pnf.transitions().size());
    }
}