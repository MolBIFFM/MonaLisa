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
import javax.swing.JTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Saves all calculated centrality values for places and transitions
 * @author Lilya Mirzoyan
 */
public class SaveResults {

    private static final Logger LOGGER = LogManager.getLogger(SaveResults.class);

    public static void saveResults(File file, JTable placeTable, JTable transitionTable) throws IOException {
        LOGGER.info("Saving results of calculated centrality values");
        Writer writer = null;
        StringBuilder sb = new StringBuilder();

        sb.append("\"name\",\"closeness\",\"teccentricity\",\"betweenness\",\"eigenvector\"");
        sb.append(System.getProperty("line.separator"));

        for(int row = 0; row < placeTable.getRowCount(); row++) {
            for(int column = 0; column < placeTable.getColumnCount(); column++){
                if(placeTable.getValueAt(row, column)!= null){
                    sb.append(placeTable.getValueAt(row, column).toString());
                    sb.append(",");
                }
            }
            sb.append(System.getProperty("line.separator"));
        }

        for(int row = 0; row < transitionTable.getRowCount(); row++) {
            for(int column = 0; column < transitionTable.getColumnCount(); column++){
                if(transitionTable.getValueAt(row, column)!= null){
                    sb.append(transitionTable.getValueAt(row, column).toString());
                    sb.append(",");
                }
            }
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
                writer.close();
            } catch (IOException ex) {
                LOGGER.error("Issue while closing writer", ex);
            }
        }
    }
}
