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

import monalisa.data.input.TInvParser;
import monalisa.data.output.TinvCalcOutputHandler;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.TInvariantBuilder;
import monalisa.data.pn.Transition;
import monalisa.results.MauritiusMap;
import monalisa.results.TInvariants;
import monalisa.tools.ErrorLog;
import monalisa.util.FileUtils;

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

    private final PetriNetFacade petriNet;
    private File pntFile = null;
    private TInvariants tinvariants = null;
    private MauritiusMap postScriptSource = null;
    private boolean hasPostScriptSource = false;
    private Map<Integer, Integer> transitionIds;

    public TInvariantCalculator(PetriNetFacade petriNet) {
       this.petriNet = petriNet;
    }

    public TInvariantCalculator(PetriNetFacade petriNet, ErrorLog log) throws InterruptedException, TInvariantCalculationFailedException {
        this.petriNet = petriNet;
        File tempDir = FileUtils.getTempDir();
        try {
            pntFile = File.createTempFile("monalisa", ".pnt", tempDir);
        } catch (IOException ex) {
            log.log(ex.getLocalizedMessage(), ErrorLog.Severity.ERROR);
            ex.printStackTrace();
            throw new TInvariantCalculationFailedException(ex);
        }
        pntFile.deleteOnExit();

        // The `tinv` tool ignores the IDs of places and transitions. Rather,
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

        TinvCalcOutputHandler outHandler = new TinvCalcOutputHandler(placeIds, transitionIds);
        try {
            outHandler.save(petriNet, new FileOutputStream(pntFile));
        } catch (FileNotFoundException ex) {
            log.log(ex.getLocalizedMessage(), ErrorLog.Severity.ERROR);
            ex.printStackTrace();
            throw new TInvariantCalculationFailedException(ex);
        }

        try {
            computeTinvariants(pntFile, tempDir);
        } catch (ExtractResourceException ex) {
            log.log("#TInvariantExtractResourceFailed", ErrorLog.Severity.ERROR);
            ex.printStackTrace();
            throw new TInvariantCalculationFailedException(ex);
        } catch (InvokeProcessException ex) {
            log.log("#TInvariantInvokeProcessFailed", ErrorLog.Severity.ERROR);
            ex.printStackTrace();
            throw new TInvariantCalculationFailedException(ex);
        }
    }

    public TInvariants tinvariants(ErrorLog log) throws TInvariantCalculationFailedException {
        if (tinvariants == null) {
            File invFile = new File(pntFile.getAbsolutePath().replaceAll("\\.pnt$", ".inv"));
            TInvariantBuilder invariantBuilder = new TInvariantBuilder(petriNet);
            TInvParser invParser;
            try {
                invParser = new TInvParser(invariantBuilder, invFile, invertMap(transitionIds));
            } catch (IOException ex) {
                log.log("#InvParserFailed", ErrorLog.Severity.ERROR);
                System.err.println("InvParser failed");
                ex.printStackTrace();
                throw new TInvariantCalculationFailedException(ex);
            }

            tinvariants = new TInvariants(invParser.invariants());
        }

        return tinvariants;
    }

    public MauritiusMap postScriptSource(ErrorLog log) {
        if (!hasPostScriptSource) {
            File psFile = new File(pntFile.getAbsolutePath().replaceAll("\\.pnt$", ".ps"));
            String psCode = null;
            try {
                psCode = FileUtils.read(psFile);
            } catch (FileNotFoundException e) {
                log.log("#TInvariantPostScriptFileNotReadable", ErrorLog.Severity.WARNING);
                e.printStackTrace();
            }

            if (psCode != null)
                postScriptSource = new MauritiusMap(psCode);
            hasPostScriptSource = true;
        }

        return postScriptSource;
    }

    private void computeTinvariants(File input, File output) throws ExtractResourceException, InvokeProcessException, InterruptedException {
        File toolFile = null;
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if(os.contains("nix") || os.contains("nux")) {
                toolFile = FileUtils.extractResource("tinv_unix", "monalisa", "bin");
            }
            else if(os.contains("win")) {
                toolFile = FileUtils.extractResource("tinv_win.exe", "monalisa", "bin");
            }
            else if(os.contains("mac")) {
                toolFile = FileUtils.extractResource("tinv_macos.exe", "monalisa", "bin");
            }
            else{
                System.out.println("No valid operating system found. Starting linux version!");
                toolFile = FileUtils.extractResource("tinv_unix", "monalisa", "bin");
            }
        } catch (IOException e) {
            throw new ExtractResourceException(e);
        }
        toolFile.deleteOnExit();
        toolFile.setExecutable(true);

        ProcessBuilder pb = new ProcessBuilder(toolFile.getAbsolutePath(), input.getAbsolutePath()).inheritIO();
        pb.directory(output);

        try {
            Process p = pb.start();
            p.waitFor();
        } catch (IOException e) {
            throw new InvokeProcessException(e);
        }
        finally {
            // Mark output files for deletion.
            String baseName = input.getAbsolutePath().replaceAll("\\..*$", "");
            String[] extensions = { ".inv" };

            for (String ext : extensions) {
                File file = new File(baseName + ext);
                if (file.exists())
                    file.deleteOnExit();
            }
        }
    }

    private final <T> Map<T, T> invertMap(Map<T, T> source) {
        Map<T, T> inverted = new HashMap<>();

        for (Map.Entry<T, T> entry : source.entrySet())
            inverted.put(entry.getValue(), entry.getKey());

        return inverted;
    }
}
