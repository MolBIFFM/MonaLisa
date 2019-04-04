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
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author jJens Einloft
 */
public class PlainOutputHandler implements OutputHandler {

    private static final Logger LOGGER = LogManager.getLogger(PlainOutputHandler.class);

    public void save(FileOutputStream fos, PetriNet pn) {
        LOGGER.info("Export Petri net in plain format");
        try (PrintStream ps = new PrintStream(fos)) {
            int i, weight, nbr;
            String name;
            for(Transition t : pn.transitions()) {
                name = (String) t.getProperty("name");
                ps.print(name+": ");

                i = 1;
                nbr = t.inputs().size();
                for(Place p : t.inputs()) {
                    name = (String) p.getProperty("name");
                    weight = pn.getArc(p, t).weight();
                    if(weight > 1)
                        ps.print(weight+"*");
                    ps.print(name);
                    if(i < nbr)
                        ps.print(" + ");
                    i++;
                }

                ps.print(" -> ");

                i = 1;
                nbr = t.outputs().size();
                for(Place p : t.outputs()) {
                    name = (String) p.getProperty("name");
                    weight = pn.getArc(t, p).weight();
                    if(weight > 1)
                        ps.print(weight+"*");
                    ps.print(name);
                    if(i < nbr)
                        ps.print(" + ");
                    i++;
                }

                ps.print("\n");
            }
        LOGGER.info("Successfully exported Petri net in plain format");
        }
    }

    public boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file is in plain format");
        return ("txt".equalsIgnoreCase(FileUtils.getExtension(file)));
    }

    public File checkFileNameForExtension(File file) {
        if(!"txt".equalsIgnoreCase(FileUtils.getExtension(file)))
            file = new File(file.getAbsolutePath()+".txt");
        return file;
    }


    public String getExtension() {
        return "txt";
    }

    public String getDescription() {
        return "Reaction List";
    }

}
