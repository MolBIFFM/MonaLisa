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
import java.util.ArrayList;
import java.util.List;

import monalisa.util.FileUtils;
import monalisa.data.pn.TInvariant;
import monalisa.data.pn.TInvariantBuilder;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Transition;

import monalisa.results.Result;
import monalisa.results.TInvariants;


/**
 * @author Jens Einloft
 */
public final class ResTInputHandler implements TInputHandler {
  
    @Override
    public boolean isKnownFile(File file) throws IOException {
        return ("res".equalsIgnoreCase(FileUtils.getExtension(file)));        
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result load(InputStream in, PetriNet petriNet) throws IOException {
        
        List<TInvariant> invariants = new ArrayList<>();
        TInvariantBuilder invBuilder = new TInvariantBuilder(new PetriNetFacade(petriNet));
        List<String> badTransitions = new ArrayList<>();
        Transition transition;
        String[] line_parts, name_parts;
        String name;
        int tid, count, linecounter = 0;

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        while ((line = reader.readLine()) != null) {
            if(linecounter > 3) {
                if(line.contains("@") || line.isEmpty() ) {
                    invariants.add(invBuilder.buildAndClear());
                    System.out.println("T Invariant File loaded");
                    break;
                }

                // Strip whole String
                line = line.replaceAll(" ", "");
                line_parts = line.split("\\|");

                // Start of a new T Invariant
                if(!line_parts[0].equalsIgnoreCase("")) {
                    if(!invBuilder.isEmpty()) {
                        invariants.add(invBuilder.buildAndClear());
                    }
                    invBuilder.setId(Integer.parseInt(line_parts[0]));
                }
                
                name_parts = line_parts[1].split("\\.");
                tid = Integer.parseInt(name_parts[0]);
                name = name_parts[1].split(":")[0];
                transition = petriNet.findTransition(tid);
                if(transition == null)
                if(!transition.getProperty("name").equals(name)) {
                    if(!badTransitions.contains(name)) {
                        badTransitions.add(name);
                        System.out.println("Warning: Transition with ID "+tid+" has name "
                                           +transition.getProperty("name")+". Name in T Invariant file are "+name+".");
                        System.out.println("Wrong T Invariant File or different declaration.");
                    }
                }
                count = Integer.parseInt(((line_parts[1].split(":")[1]).split(","))[0]);
                invBuilder.add(transition, count);
            }

            linecounter++;
        }

        Result results = new TInvariants(invariants);
        return results;
    }

}
