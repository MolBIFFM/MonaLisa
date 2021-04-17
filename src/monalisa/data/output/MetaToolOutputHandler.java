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
import monalisa.data.pn.Arc;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.addons.netviewer.NetViewer;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import monalisa.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Output handler for MetaTool format
 *
 * @author Jens Einloft
 */
public class MetaToolOutputHandler implements OutputHandler {

    private static final ResourceManager resources = ResourceManager.instance();
    private static final StringResources strings = resources.getDefaultStrings();
    private static final Logger LOGGER = LogManager.getLogger(MetaToolOutputHandler.class);

    public void save(FileOutputStream fos, PetriNet pn, File file, NetViewer netViewer) {
        LOGGER.info("Exporting Petri net to MetaTool format");
        try (PrintStream ps = new PrintStream(fos)) {
            ps.println("Generated with MonaLisa Version " + strings.get("CurrentVersion"));

            ps.println("-ENZREV\n");

            ps.println("-ENZIRREV");

            for (Transition t : pn.transitions()) {
                ps.print(t.getProperty("name") + " ");
            }
            ps.println("\n");

            ps.println("-METINT");

            for (Place p : pn.places()) {
                ps.print(p.getProperty("name") + " ");
            }
            ps.println();

            ps.println("-METEXT\n");

            ps.println("-CAT");
            String weight = "";
            Arc arc;
            int i, len;
            for (Transition t : pn.transitions()) {
                ps.print(t.getProperty("name") + " : ");

                i = 0;
                len = t.inputs().size();
                for (Place p : t.inputs()) {
                    i++;
                    arc = pn.getArc(p, t);
                    if (arc.weight() == 1) {
                        weight = "";
                    } else {
                        weight = "" + arc.weight() + " ";
                    }
                    ps.print(weight + p.getProperty("name") + " ");
                    if (i < len) {
                        ps.print("+ ");
                    }
                }

                ps.print("= ");

                i = 0;
                len = t.outputs().size();
                for (Place p : t.outputs()) {
                    i++;
                    arc = pn.getArc(t, p);
                    if (arc.weight() == 1) {
                        weight = "";
                    } else {
                        weight = "" + arc.weight() + " ";
                    }
                    ps.print(weight + p.getProperty("name") + " ");
                    if (i < len) {
                        ps.print("+ ");
                    }
                }
                ps.println(".");
            }
            LOGGER.info("Successfully exported Petri net to MetaTool format");
        }
    }

    public boolean isKnownFile(File file) throws IOException {
        LOGGER.debug("Checking whether file is in MetaTool format");
        return "dat".equalsIgnoreCase(FileUtils.getExtension(file)) || "meta".equalsIgnoreCase(FileUtils.getExtension(file));
    }

    public File checkFileNameForExtension(File file) {
        if (!"dat".equalsIgnoreCase(FileUtils.getExtension(file))) {
            file = new File(file.getAbsolutePath() + ".xml");
        }
        return file;
    }

    public String getExtension() {
        return "dat";
    }

    public String getDescription() {
        return "MetaTool";
    }
}
