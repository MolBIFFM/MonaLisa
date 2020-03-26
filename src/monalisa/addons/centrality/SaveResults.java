/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.centrality;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Saves all calculated centrality values for places and transitions
 * @author Lilya Mirzoyan
 */
public class SaveResults {

    private static final Logger LOGGER = LogManager.getLogger(SaveResults.class);

    /**
     *
     * @param file
     * @param cc
     * @param ecc
     * @param bc
     * @param ec
     * @param pnf
     * @throws java.io.IOException
     */
    public static void saveResults(File file, ClosenessCentrality cc,
            EccentricityCentrality ecc, BetweennessCentrality bc,
            EigenvectorCentrality ec, PetriNetFacade pnf) throws IOException {
        LOGGER.info("Saving results of calculated centrality values");
        Writer writer = null;
        StringBuilder sb = new StringBuilder();

        sb.append("\"name\",\"closeness\",\"teccentricity\",\"betweenness\",\"eigenvector\"");
        sb.append(System.getProperty("line.separator"));
        
        // Centralities for places, need to be adjusted for new export
        Collection<Place> places = pnf.places();
        for (Place p : places) {
            sb.append(p.toString()).append(","); // Name
            sb.append(cc.rankingPlaces.get(p.id()).toString()).append(",");
            sb.append(ecc.rankingPlaces.get(p.id()).toString()).append(",");
            sb.append(bc.rankingPlaces.get(p.id()).toString()).append(",");
            sb.append(ec.rankingPlaces.get(p.id()).toString());
            sb.append(System.getProperty("line.separator"));
        }
        // Removed "if(placeTable.getValueAt(row, column)!= null)" functionality, prefer controlled error over erroneous output

        // Centralities for transitions, need to be adjusted for new export
        Collection<Transition> transitions = pnf.transitions();        
        for (Transition t : transitions) {
            sb.append(t.toString()).append(","); // Name
            sb.append(cc.rankingTransitions.get(t.id()).toString()).append(",");
            sb.append(ecc.rankingTransitions.get(t.id()).toString()).append(",");
            sb.append(bc.rankingTransitions.get(t.id()).toString()).append(",");
            sb.append(ec.rankingTransitions.get(t.id()).toString());
            sb.append(System.getProperty("line.separator"));
        }
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
            writer.write(sb.toString());
            LOGGER.info("Successfully saved calculated centrality values");
        } catch (IOException ex) {
            LOGGER.error("Issue during saving of calculated centrality values", ex);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }            } catch (IOException ex) {
                LOGGER.error("Issue while closing writer", ex);
            }
        }
    }
}
