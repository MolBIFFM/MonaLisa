/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.pinv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import monalisa.data.input.PInvParser;
import monalisa.data.output.PinvCalcOutputHandler;
import monalisa.data.pn.PInvariantBuilder;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.results.PInvariants;
import monalisa.tools.ErrorLog;
import monalisa.util.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public final class PInvariantCalculator {
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
    private PInvariants pinvariants = null;
    private Map<Integer, Integer> placeIds;
    private static final Logger LOGGER = LogManager.getLogger(PInvariantCalculator.class);

    public PInvariantCalculator(PetriNetFacade petriNet, ErrorLog log) throws InterruptedException, PInvariantCalculationFailedException {
        LOGGER.info("Initializing PInvariantCalculator");
        this.petriNet = petriNet;
        File tempDir = FileUtils.getTempDir();
        try {
            LOGGER.debug("Creating temporary .pnt to export Petri net to");
            pntFile = File.createTempFile("monalisa", ".pnt", tempDir);
        } catch (IOException ex) {
            log.log(ex.getLocalizedMessage(), ErrorLog.Severity.ERROR);
            LOGGER.error("Caught IOException while trying to create temporary .pnt to export Petri net to: ", ex);
            throw new PInvariantCalculationFailedException(ex);
        }
        pntFile.deleteOnExit();

        // The `tinv` tool ignores the IDs of places and transitions. Rather,
        // it simply increments a counter for them, starting at zero. To
        // account for that, and to re-establish a mapping between the tinv
        // output and our Petri net, we perform a little bookkeeping ...

        placeIds = new HashMap<>();
        int pid = 0;
        for (Place place : petriNet.places())
            placeIds.put(place.id(), pid++);

        Map<Integer, Integer> transitionIds = new HashMap<>();
        int tid = 0;
        for (Transition transition : petriNet.transitions())
            transitionIds.put(transition.id(), tid++);

        PinvCalcOutputHandler outHandler = new PinvCalcOutputHandler(placeIds, transitionIds);
        try {
            LOGGER.debug("Trying to export Petri net to temporary .pnt file for P-Invariant calculation");
            outHandler.save(petriNet, new FileOutputStream(pntFile));
        } catch (FileNotFoundException ex) {
            log.log(ex.getLocalizedMessage(), ErrorLog.Severity.ERROR);
            LOGGER.error("Caught FileNotFoundException while trying to export Petri net to temporary .pnt file for P-Invariant calculation: ", ex);
            throw new PInvariantCalculationFailedException(ex);
        }

        try {
            LOGGER.debug("Computing P-Invariants based on temporary .pnt file");
            computePinvariants(pntFile, tempDir);
        } catch (ExtractResourceException ex) {
            log.log("#PInvariantExtractResourceFailed", ErrorLog.Severity.ERROR);
            LOGGER.error("Caught ExtractResourceException while trying to find external tool location");
            throw new PInvariantCalculationFailedException(ex);
        } catch (InvokeProcessException ex) {
            log.log("#PInvariantInvokeProcessFailed", ErrorLog.Severity.ERROR);
            LOGGER.error("Caught InvokeProcessException while trying to start process for P-Invariant computation");
            throw new PInvariantCalculationFailedException(ex);
        }
    }

    public PInvariants pinvariants(ErrorLog log) throws PInvariantCalculationFailedException {
        if (pinvariants == null) {
            LOGGER.debug("Importing P-Invariants calculated by external tool");
            File invFile = new File(pntFile.getAbsolutePath().replaceAll("\\.pnt$", ".inv"));
            PInvariantBuilder invariantBuilder = new PInvariantBuilder(petriNet);
            PInvParser invParser;
            try {
                invParser = new PInvParser(invariantBuilder, invFile, invertMap(placeIds));
                LOGGER.debug("Successfully imported P-Invariants calculated by external tool");
            } catch (IOException ex) {
                log.log("#InvParserFailed", ErrorLog.Severity.ERROR);
                LOGGER.error("Caught IOException while tring to parse P-Invariants calculated by external tool");
                throw new PInvariantCalculationFailedException(ex);
            }

            pinvariants = new PInvariants(invParser.invariants());
        }

        return pinvariants;
    }

    private void computePinvariants(File input, File output) throws
            ExtractResourceException,
            InvokeProcessException,
            InterruptedException {
        File toolFile = null;
        try {
            LOGGER.debug("Getting external tool location based on OS");
            if( System.getProperty("os.name").toLowerCase().indexOf("nix") >= 0
             || System.getProperty("os.name").toLowerCase().indexOf("nux") >= 0 ) {
                LOGGER.debug("OS determined to be Unix");
                toolFile = FileUtils.extractResource("tinv_unix", "monalisa", "bin");
            }
            else if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                LOGGER.debug("OS determined to be Windows");
                toolFile = FileUtils.extractResource("tinv_win.exe", "monalisa", "bin");
            }
            else if(System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
                LOGGER.debug("OS determined to be MAC");
                toolFile = FileUtils.extractResource("tinv_macos.exe", "monalisa", "bin");
            }
        } catch (IOException e) {
            throw new ExtractResourceException(e);
        }
        toolFile.deleteOnExit();
        toolFile.setExecutable(true);

        ProcessBuilder pb = new ProcessBuilder(toolFile.getAbsolutePath(), input.getAbsolutePath()).inheritIO();
        pb.directory(output);

        try {
            LOGGER.debug("Starting actual process to compute P-Invariants");
            Process p = pb.start();
            p.waitFor();
            LOGGER.debug("Successfully computed P-Invariants");
        } catch (IOException e) {
            throw new InvokeProcessException(e);
        }
        finally {
            LOGGER.debug("Marking output files for deletion");
            // Mark output files for deletion.
            String baseName = input.getAbsolutePath().replaceAll("\\..*$", "");
            String[] extensions = { ".inv" };

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
