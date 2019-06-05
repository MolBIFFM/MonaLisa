/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.minv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import monalisa.data.input.InvParser;
import monalisa.data.output.InvCalcOutputHandler;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.InvariantBuilder;
import monalisa.data.pn.Transition;
import monalisa.results.MInvariants;
import monalisa.tools.ErrorLog;
import monalisa.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author daniel
 */

public final class MInvariantCalculator {
    @SuppressWarnings("serial")
    private class ExtractResourceException extends Exception {
        public ExtractResourceException(Exception cause) {
            super(cause);
        }
    }

    @SuppressWarnings("serial")
    private class InvokeProcessException extends Exception {
        public InvokeProcessException(Exception cause) {
            super(cause);
        }
    }

    private final PetriNetFacade petriNet;
    private File pntFile = null;
    private MInvariants minvariants = null;
    private boolean hasPostScriptSource = false;
    private Map<Integer, Integer> transitionIds;
    private static final Logger LOGGER = LogManager.getLogger(MInvariantCalculator.class);

    public MInvariantCalculator(PetriNetFacade petriNet) {
       this.petriNet = petriNet;
    }

    public MInvariantCalculator(PetriNetFacade petriNet, ErrorLog log) throws InterruptedException, MInvariantCalculationFailedException {
        LOGGER.info("Initializing MInvariantCalculator");
        this.petriNet = petriNet;
        File tempDir = FileUtils.getTempDir();
        try {
            LOGGER.debug("Creating temporary .pnt to export Petri net to");
            pntFile = File.createTempFile("monalisa", ".pnt", tempDir);
        } catch (IOException ex) {
            log.log(ex.getLocalizedMessage(), ErrorLog.Severity.ERROR);
            LOGGER.error("Caught IOException while trying to create temporary .pnt to export Petri net to: ", ex);
            throw new MInvariantCalculationFailedException(ex);
        }
        pntFile.deleteOnExit();

        // The `minv` tool ignores the IDs of places and transitions. Rather,
        // it simply increments a counter for them, starting at zero. To
        // account for that, and to re-establish a mapping between the tinv
        // output and our Petri net, we perform a little bookkeeping ...
        
        Map<Integer, Integer> placeIds = new HashMap<>();
        int pid = 0;
        for (Place place : petriNet.places())
            placeIds.put(place.id(), pid++);

        transitionIds = new HashMap<>();
        int tid = 0;
        for (Transition transition : petriNet.transitions())
            transitionIds.put(transition.id(), tid++);

        InvCalcOutputHandler outHandler = new InvCalcOutputHandler(placeIds, transitionIds,"MI");
        try {
            LOGGER.debug("Trying to export Petri net to temporary .pnt file for M-Invariant calculation");
            outHandler.save(petriNet, new FileOutputStream(pntFile));
        } catch (FileNotFoundException ex) {
            log.log(ex.getLocalizedMessage(), ErrorLog.Severity.ERROR);
            LOGGER.error("Caught FileNotFoundException while trying to export Petri net to temporary .pnt file for M-Invariant calculation: ", ex);
            throw new MInvariantCalculationFailedException(ex);
        }

        try {
            LOGGER.debug("Computing M-Invariants based on temporary .pnt file");
            computeMinvariants(pntFile, tempDir);
        } catch (ExtractResourceException ex) {
            log.log("#MInvariantExtractResourceFailed", ErrorLog.Severity.ERROR);
            LOGGER.error("Caught ExtractResourceException while trying to find external tool location");
            throw new MInvariantCalculationFailedException(ex);
        } catch (InvokeProcessException ex) {
            log.log("#MInvariantInvokeProcessFailed", ErrorLog.Severity.ERROR);
            LOGGER.error("Caught InvokeProcessException while trying to start process for M-Invariant computation");
            throw new MInvariantCalculationFailedException(ex);
        }
    }

    public MInvariants minvariants(ErrorLog log) throws MInvariantCalculationFailedException {
        if (minvariants == null) {
            LOGGER.debug("Importing M-Invariants calculated by external tool");
            File invFile = new File(pntFile.getAbsolutePath().replaceAll("\\.pnt$", ".man"));
            if(invFile.canRead()){
                InvariantBuilder invariantBuilder = new InvariantBuilder(petriNet,"MI");
                InvParser invParser;

                try {
                    invParser = new InvParser(invariantBuilder, invFile, invertMap(transitionIds), "MI");
                    LOGGER.debug("Successfully imported M-Invariants calculated by external tool");
                } catch (IOException ex) {
                    log.log("#InvParserFailed", ErrorLog.Severity.ERROR);
                    LOGGER.error("Caught IOException while tring to parse M-Invariants calculated by external tool");
                    throw new MInvariantCalculationFailedException(ex);
                }

                minvariants = new MInvariants(invParser.invariants());
            }
        }

        return minvariants;
    }


    private void computeMinvariants(File input, File output) throws ExtractResourceException, InvokeProcessException, InterruptedException {
        File toolFile = null;
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if(os.contains("nix") || os.contains("nux")) {
                LOGGER.debug("OS determined to be Unix");
                toolFile = FileUtils.extractResource("manatee_2", "monalisa", "bin");
            }
            else if(os.contains("win")) {
                LOGGER.debug("OS determined to be Windows");
                toolFile = FileUtils.extractResource("manatee.exe", "monalisa", "bin");
            }
        } catch (IOException e) {
            throw new ExtractResourceException(e);
        }
        toolFile.deleteOnExit();
        toolFile.setExecutable(true);
        
        String[] input_commands = new String[3];
        
        input_commands[0] = toolFile.getAbsolutePath();
        input_commands[1] = input.getAbsolutePath();
        input_commands[2] = ("MI");
        
        ProcessBuilder pb = new ProcessBuilder(input_commands).inheritIO();
        pb.directory(output);
        

        try {
            LOGGER.debug("Starting actual process to compute M-Invariants");
            Process p = pb.start();
            p.waitFor();
            LOGGER.debug("Successfully computed M-Invariants");
        } catch (IOException e) {
            throw new InvokeProcessException(e);
        }
        finally {
            LOGGER.debug("Marking output files for deletion");
            // Mark output files for deletion.
            String baseName = input.getAbsolutePath().replaceAll("\\..*$", "");
            String[] extensions = { ".man" };

            for (String ext : extensions) {
                File file = new File(baseName + ext);
                if (file.exists())
                    file.deleteOnExit();
            }
            
            LOGGER.debug("Successfully marked output files for deletion");
        }
    }

    private final <T> Map<T, T> invertMap(Map<T, T> source) {
        Map<T, T> inverted = new HashMap<>();

        for (Map.Entry<T, T> entry : source.entrySet())
            inverted.put(entry.getValue(), entry.getKey());

        return inverted;
    }
}

