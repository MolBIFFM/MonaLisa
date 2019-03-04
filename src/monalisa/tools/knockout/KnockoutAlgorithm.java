/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.tools.knockout;

import monalisa.data.pn.PetriNet;
import monalisa.data.pn.UniquePetriNetEntity;

import monalisa.tools.ErrorLog;
import monalisa.tools.ProgressEvent;
import monalisa.tools.ProgressListener;
import monalisa.tools.tinv.TInvariantCalculationFailedException;
import monalisa.tools.tinv.TInvariantCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.TInvariant;
import monalisa.data.pn.Transition;

public abstract class KnockoutAlgorithm {
    
    private final PetriNetFacade pn;
    private final Map<List<String>, List<String>> results;
    
    KnockoutAlgorithm(PetriNetFacade pn) {
        this.results = new HashMap<>();
        this.pn = pn;
    }
    
    public void run(ProgressListener callback, ErrorLog log) throws InterruptedException, TInvariantCalculationFailedException {
        TInvariantCalculator calculator = null;
        
        int instance = 0;
        List<String> originalTransitions = new ArrayList<>();
        Map<String, Integer> tmpMap;
        List<String> knockedOut;
        List<String> alsoKnockedOut;

        for(Transition t : pn.transitions())
            originalTransitions.add((String) t.getProperty("name"));

        // Knock-out
        while(hasNextKnockOutNetwork()) {
            calculator = new TInvariantCalculator(getNextKnockOutNetwork(), log);
            knockedOut = new ArrayList<>();
            alsoKnockedOut = new ArrayList<>();
            tmpMap = new HashMap<>();
            for(UniquePetriNetEntity e : getKnockoutEntities())
                knockedOut.add((String) e.getProperty("name"));
            for(TInvariant tinv : calculator.tinvariants(log)) {
                for(Transition t : tinv) {
                    if(tinv.factor(t) > 0) {
                        tmpMap.put((String) t.getProperty("name"), 1);
                    }
                }
            }

            for(String name : originalTransitions) {
                if(!tmpMap.containsKey(name) && !knockedOut.contains(name))
                    alsoKnockedOut.add(name);
            }
            results.put(knockedOut, alsoKnockedOut);
            
            int percent = (int) ((double) ++instance / getTotalKnockouts() * 100);
            callback.progressUpdated(new ProgressEvent(this, percent));            
        }
    }
    
    public Map<List<String>, List<String>> getResults() {
        return Collections.unmodifiableMap(results);
    }
    
    protected PetriNetFacade getPetriNetFacade() {
        return pn;
    }
    
    protected abstract int getTotalKnockouts();
    
    protected abstract PetriNetFacade getNextKnockOutNetwork();
    
    protected abstract boolean hasNextKnockOutNetwork();
    
    protected abstract List<UniquePetriNetEntity> getKnockoutEntities();
}
