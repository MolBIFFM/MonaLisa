/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.tools.tinv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import monalisa.addons.netviewer.NetViewer;
import monalisa.data.input.InvParser;
import monalisa.data.output.InvCalcOutputHandler;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.InvariantBuilder;
import monalisa.data.pn.Transition;
import monalisa.results.MauritiusMap;
import monalisa.results.TInvariants;
import monalisa.tools.ErrorLog;
import monalisa.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class TInvariantCalculator {

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
    
    private NetViewer netViewer;
    private final PetriNetFacade petriNet;
    private File pntFile = null;
    private TInvariants tinvariants = null;
    private MauritiusMap postScriptSource = null;
    private boolean hasPostScriptSource = false;
    private Map<Integer, Integer> transitionIds;
    private static final Logger LOGGER = LogManager.getLogger(TInvariantCalculator.class);

    public TInvariantCalculator(PetriNetFacade petriNet) {
        this.petriNet = petriNet;
    }

    public TInvariantCalculator(PetriNetFacade petriNet, ErrorLog log) throws InterruptedException, TInvariantCalculationFailedException {
        LOGGER.info("Initializing TInvariantCalculator");
        this.petriNet = petriNet;
        File tempDir = FileUtils.getTempDir();
        try {
            LOGGER.debug("Creating temporary .pnt to export Petri net to");
            pntFile = File.createTempFile("monalisa", ".pnt", tempDir);
        } catch (IOException ex) {
            log.log(ex.getLocalizedMessage(), ErrorLog.Severity.ERROR);
            LOGGER.error("Caught IOException while trying to create temporary .pnt to export Petri net to: ", ex);
            throw new TInvariantCalculationFailedException(ex);
        }
        pntFile.deleteOnExit();

        // The `tinv` tool ignores the IDs of places and transitions. Rather,
        // it simply increments a counter for them, starting at zero. To
        // account for that, and to re-establish a mapping between the tinv
        // output and our Petri net, we perform a little bookkeeping ...
        Map<Integer, Integer> placeIds = new HashMap<>();
        int pid = 0;
        for (Place place : petriNet.places()) {
            placeIds.put(place.id(), pid++);
        }

        transitionIds = new HashMap<>();
        int tid = 0;
        for (Transition transition : petriNet.transitions()) {
            transitionIds.put(transition.id(), tid++);
        }

        InvCalcOutputHandler outHandler = new InvCalcOutputHandler(placeIds, transitionIds, "TI");
        try {
            LOGGER.debug("Trying to export Petri net to temporary .pnt file for T-Invariant calculation");
            outHandler.save(petriNet, new FileOutputStream(pntFile), pntFile, netViewer);
        } catch (FileNotFoundException ex) {
            log.log(ex.getLocalizedMessage(), ErrorLog.Severity.ERROR);
            LOGGER.error("Caught FileNotFoundException while trying to export Petri net to temporary .pnt file for T-Invariant calculation: ", ex);
            throw new TInvariantCalculationFailedException(ex);
        }

        try {
            LOGGER.debug("Computing T-Invariants based on temporary .pnt file");
            computeTinvariants(pntFile, tempDir);
        } catch (ExtractResourceException ex) {
            log.log("#TInvariantExtractResourceFailed", ErrorLog.Severity.ERROR);
            LOGGER.error("Caught ExtractResourceException while trying to find external tool location");
            throw new TInvariantCalculationFailedException(ex);
        } catch (InvokeProcessException ex) {
            log.log("#TInvariantInvokeProcessFailed", ErrorLog.Severity.ERROR);
            LOGGER.error("Caught InvokeProcessException while trying to start process for T-Invariant computation");
            throw new TInvariantCalculationFailedException(ex);
        }
    }

    public TInvariants tinvariants(ErrorLog log) throws TInvariantCalculationFailedException {
        if (tinvariants == null) {
            LOGGER.debug("Importing T-Invariants calculated by external tool");
            File invFile = new File(pntFile.getAbsolutePath().replaceAll("\\.pnt$", ".inv"));
            InvariantBuilder invariantBuilder = new InvariantBuilder(petriNet, "TI");
            InvParser invParser;
            try {
                invParser = new InvParser(invariantBuilder, invFile, invertMap(transitionIds), "TI");
                LOGGER.debug("Successfully imported T-Invariants calculated by external tool");
            } catch (IOException ex) {
                log.log("#InvParserFailed", ErrorLog.Severity.ERROR);
                LOGGER.error("Caught IOException while tring to parse T-Invariants calculated by external tool");
                throw new TInvariantCalculationFailedException(ex);
            }

            tinvariants = new TInvariants(invParser.invariants());
        }

        return tinvariants;
    }

    public MauritiusMap postScriptSource(ErrorLog log) {
        if (!hasPostScriptSource) {
            LOGGER.debug("Getting postScriptSource for MauritiusMap");
            File psFile = new File(pntFile.getAbsolutePath().replaceAll("\\.pnt$", ".ps"));
            String psCode = null;
            try {
                psCode = FileUtils.read(psFile);
            } catch (FileNotFoundException e) {
                log.log("#TInvariantPostScriptFileNotReadable", ErrorLog.Severity.WARNING);
                LOGGER.error("Caught FileNotFoundException while trying to get postScriptSoure for MauritiusMap");
            }

            if (psCode != null) {
                postScriptSource = new MauritiusMap(psCode);
            }
            hasPostScriptSource = true;
            LOGGER.debug("Successfully got postScriptSource for MauritiusMap");
        }

        return postScriptSource;
    }

    private void computeTinvariants(File input, File output) throws ExtractResourceException, InvokeProcessException, InterruptedException {
        File toolFile = null;
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("nix") || os.contains("nux")) {
                LOGGER.debug("OS determined to be Unix");
                toolFile = FileUtils.extractResource("manatee", "monalisa", "bin");
            } else if (os.contains("win")) {
                LOGGER.debug("OS determined to be Windows");
                toolFile = FileUtils.extractResource("manatee.exe", "monalisa", "bin");
            } else if (os.contains("mac")) {
                LOGGER.debug("OS determined to be MAC");
                toolFile = FileUtils.extractResource("tinv_macos.exe", "monalisa", "bin");
            } else {
                LOGGER.warn("No valid operating system found. Starting linux version!");
                toolFile = FileUtils.extractResource("manatee", "monalisa", "bin");
            }
        } catch (IOException e) {
            throw new ExtractResourceException(e);
        }
        toolFile.deleteOnExit();
        toolFile.setExecutable(true);

        String[] input_commands = new String[3];

        input_commands[0] = toolFile.getAbsolutePath();
        input_commands[1] = input.getAbsolutePath();
        input_commands[2] = ("TI");

        ProcessBuilder pb = new ProcessBuilder(input_commands).inheritIO();
        pb.directory(output);

        try {
            LOGGER.debug("Starting actual process to compute T-Invariants");
            Process p = pb.start();
            p.waitFor();
            LOGGER.debug("Successfully computed T-Invariants");
        } catch (IOException e) {
            throw new InvokeProcessException(e);
        } finally {
            LOGGER.debug("Marking output files for deletion");
            // Mark output files for deletion.
            String baseName = input.getAbsolutePath().replaceAll("\\..*$", "");
            String[] extensions = {".inv"};

            for (String ext : extensions) {
                File file = new File(baseName + ext);
                if (file.exists()) {
                    file.deleteOnExit();
                }
            }
            LOGGER.debug("Successfully marked output files for deletion");
        }
    }

    private final <T> Map<T, T> invertMap(Map<T, T> source) {
        Map<T, T> inverted = new HashMap<>();

        for (Map.Entry<T, T> entry : source.entrySet()) {
            inverted.put(entry.getValue(), entry.getKey());
        }

        return inverted;
    }
}
